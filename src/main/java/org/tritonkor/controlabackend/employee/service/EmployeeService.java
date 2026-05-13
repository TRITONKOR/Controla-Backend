package org.tritonkor.controlabackend.employee.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tritonkor.controlabackend.employee.dto.EmployeeResponse;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.entity.TaskStatus;
import org.tritonkor.controlabackend.task.repository.TaskRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByProject(Project project) {
        return project.getAssignees()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmployeeResponse toResponse(Employee employee) {
        UUID employeeId = employee.getId();
        List<Project> projects = projectRepository.findAll();

        long projectsCount = projects.stream()
                .filter(project ->
                        (project.getOwner() != null && Objects.equals(project.getOwner().getId(), employeeId))
                                || project.getAssignees().stream().anyMatch(assignee -> Objects.equals(assignee.getId(), employeeId))
                )
                .count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusDays(30);

        long assignedTasks = taskRepository.countAssignedTasksInPeriod(employee, oneMonthAgo, now);
        long completedTasks = taskRepository.countCompletedTasksInPeriod(employee, TaskStatus.DONE, oneMonthAgo, now);
        double productivity = assignedTasks > 0 ? (completedTasks * 100.0) / assignedTasks : 0.0;

        String avatarBase64 = employee.getUser() != null && employee.getUser().getAvatar() != null
                ? Base64.getEncoder().encodeToString(employee.getUser().getAvatar())
                : null;

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                avatarBase64,
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getTitle() : null,
                projectsCount,
                Math.round(productivity * 100.0) / 100.0,
                assignedTasks,
                completedTasks
        );
    }
}
