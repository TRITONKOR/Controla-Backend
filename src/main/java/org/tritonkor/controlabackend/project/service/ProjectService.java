package org.tritonkor.controlabackend.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.project.dto.CreateProjectRequest;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.user.entity.Role;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Employee owner = employeeRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        if (owner.getUser().getRole() != Role.MANAGER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner must be a manager");
        }

        Project project = new Project(request.title(), request.description(), request.costs(), request.deadline(), owner);
        project.setDescription(request.description());

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
}

