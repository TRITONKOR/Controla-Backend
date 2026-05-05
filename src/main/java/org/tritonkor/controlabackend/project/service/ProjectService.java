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
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.user.entity.Role;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final EmployeeService employeeService;

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

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

