package org.tritonkor.controlabackend.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.Base64;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.employee.dto.EmployeeResponse;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.employee.service.EmployeeService;
import org.tritonkor.controlabackend.project.dto.CreateProjectRequest;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.dto.ReportResponse;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.entity.RiskLevel;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.task.entity.TaskStatus;
import org.tritonkor.controlabackend.task.repository.TaskRepository;
import org.tritonkor.controlabackend.user.entity.Role;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final EmployeeService employeeService;

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getProjectAssignees(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return employeeService.getEmployeesByProject(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return projectRepository.findAll()
                .stream()
                .filter(project -> project.getAssignees().contains(employee))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {

        String email = getCurrentUserEmail();

        Employee owner = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (owner.getUser().getRole() != Role.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only managers can create projects");
        }


        Project project = new Project(
                request.title(),
                request.description(),
                request.costs(),
                request.deadline(),
                owner
        );


        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        for (Task task : project.getTasks()) {
            task.getAssignees().clear();
            taskRepository.save(task);
        }

        project.getAssignees().clear();
        projectRepository.save(project);

        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public ReportResponse createProjectReport(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Map<TaskStatus, Integer> statusCount = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            statusCount.put(status, 0);
        }

        for (Task task : project.getTasks()) {
            TaskStatus status = task.getStatus() == null ? TaskStatus.TO_DO : task.getStatus();
            statusCount.put(status, statusCount.get(status) + 1);
        }

        int totalTasks = project.getTasks().size();
        int doneTasks = statusCount.get(TaskStatus.DONE);
        int inProgressTasks = statusCount.get(TaskStatus.IN_PROGRESS);
        int reviewTasks = statusCount.get(TaskStatus.REVIEW);
        int toDoTasks = statusCount.get(TaskStatus.TO_DO);

        int donePercent = toPercent(doneTasks, totalTasks);
        int reviewShare = toPercent(reviewTasks, totalTasks);
        int activeTasks = inProgressTasks + reviewTasks + toDoTasks;

        LocalDateTime deadline = project.getDeadline();
        LocalDateTime now = LocalDateTime.now();
        long daysToDeadline = deadline != null ? ChronoUnit.DAYS.between(now, deadline) : 0;
        boolean overdue = deadline != null && deadline.isBefore(now);

        RiskLevel riskLevel = calculateRiskLevel(overdue, daysToDeadline, donePercent, reviewShare, activeTasks, totalTasks);

        List<ReportResponse.StatusDistributionItem> statusDistribution = List.of(
                new ReportResponse.StatusDistributionItem(TaskStatus.TO_DO, toDoTasks, toPercent(toDoTasks, totalTasks)),
                new ReportResponse.StatusDistributionItem(TaskStatus.IN_PROGRESS, inProgressTasks, toPercent(inProgressTasks, totalTasks)),
                new ReportResponse.StatusDistributionItem(TaskStatus.REVIEW, reviewTasks, toPercent(reviewTasks, totalTasks)),
                new ReportResponse.StatusDistributionItem(TaskStatus.DONE, doneTasks, toPercent(doneTasks, totalTasks))
        );

        return new ReportResponse(
                project.getId(),
                project.getTitle(),
                totalTasks,
                doneTasks,
                inProgressTasks,
                reviewTasks,
                toDoTasks,
                donePercent,
                deadline,
                daysToDeadline,
                overdue,
                activeTasks,
                reviewShare,
                riskLevel,
                statusDistribution
        );
    }

    private int toPercent(int part, int total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((part * 100.0) / total);
    }

    private RiskLevel calculateRiskLevel(boolean overdue, long daysToDeadline, int donePercent, int reviewShare, int activeTasks, int totalTasks) {
        if (overdue && donePercent < 100) {
            return RiskLevel.HIGH;
        }

        if (daysToDeadline <= 3 && donePercent < 70) {
            return RiskLevel.HIGH;
        }

        if (reviewShare >= 40 || (daysToDeadline <= 7 && donePercent < 85)) {
            return RiskLevel.MEDIUM;
        }

        if (totalTasks > 0 && activeTasks > (totalTasks - activeTasks)) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    private ProjectResponse toResponse(Project project) {
        List<ProjectResponse.TaskShortResponse> tasks = project.getTasks()
                .stream()
                .map(this::toTaskShort)
                .toList();

        List<ProjectResponse.EmployeeShortResponse> assignees = project.getAssignees()
                .stream()
                .map(this::toEmployeeShort)
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getOwner() != null ? project.getOwner().getId() : null,
                project.getOwner() != null ? project.getOwner().getFirstName() : null,
                project.getOwner() != null ? project.getOwner().getLastName() : null,                project.getOwner() != null && project.getOwner().getUser().getAvatar() != null
                        ? Base64.getEncoder().encodeToString(project.getOwner().getUser().getAvatar())
                        : null,
                project.getStatus() != null ? project.getStatus().name() : null,
                project.getCosts(),
                project.getDeadline(),
                tasks,
                assignees
        );
    }

    private ProjectResponse.TaskShortResponse toTaskShort(Task task) {
        return new ProjectResponse.TaskShortResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? task.getStatus().name() : null
        );
    }

    private ProjectResponse.EmployeeShortResponse toEmployeeShort(Employee employee) {
        String departmentTitle = employee.getDepartment() != null
                ? employee.getDepartment().getTitle()
                : null;

        return new ProjectResponse.EmployeeShortResponse(
                employee.getId().toString(),
                employee.getUser().getId().toString(),
                employee.getFirstName(),
                employee.getLastName(),
                departmentTitle
        );
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return auth.getName();
    }
}
