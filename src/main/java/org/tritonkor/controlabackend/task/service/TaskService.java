package org.tritonkor.controlabackend.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.dto.CreateTaskRequest;
import org.tritonkor.controlabackend.task.dto.TaskResponse;
import org.tritonkor.controlabackend.task.dto.UpdateTaskRequest;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.task.entity.TaskStatus;
import org.tritonkor.controlabackend.task.repository.TaskRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Task getTaskWithDetails(String title) {
        return taskRepository.findByTitle(title)
                .orElseThrow(() -> new RuntimeException("Завдання з назвою " + title + " не знайдено"));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new RuntimeException("Проект з ID " + request.projectId() + " не знайдено"));

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setProject(project);
        task.setAttachmentUrl(request.attachmentUrl());
        task.setAttachmentName(request.attachmentName());


        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(UUID task_id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(task_id)
                .orElseThrow(() -> new RuntimeException("Завдання з ID " + task_id + " не знайдено"));

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.attachmentUrl() != null) task.setAttachmentUrl(request.attachmentUrl());
        if (request.attachmentName() != null) task.setAttachmentName(request.attachmentName());

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Завдання з ID " + taskId + " не знайдено"));

        task.setStatus(TaskStatus.valueOf(newStatus.toUpperCase()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUser(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Співробітник з User ID " + userId + " не знайдено"));

        return taskRepository.findByAssigneesContaining(employee)
                .stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse assignEmployeeToTask(UUID taskId, UUID employeeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        Project project = task.getProject();

        if (task.getAssignees().contains(employee)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is already assigned to this task");
        }

        task.getAssignees().add(employee);

        if (!project.getAssignees().contains(employee)) {
            project.getAssignees().add(employee);
            projectRepository.save(project);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse removeAssignee(UUID taskId, UUID employeeId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Task not found"
                        ));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Employee not found"
                        ));

        if (!task.getAssignees().remove(employee)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee is not assigned to this task"
            );
        }

        Project project = task.getProject();

        boolean hasOtherTasks =
                taskRepository.hasOtherTasksInProject(
                        project.getId(),
                        task.getId(),
                        employee
                );

        if (!hasOtherTasks) {
            project.getAssignees().remove(employee);
        }

        return toResponse(task);
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse.ProjectShortResponse projectResponse = new TaskResponse.ProjectShortResponse(
                task.getProject().getId().toString(),
                task.getProject().getTitle()
        );

        List<TaskResponse.EmployeeShortResponse> assignees = task.getAssignees().stream()
                .map(employee -> new TaskResponse.EmployeeShortResponse(
                        employee.getId().toString(),
                        employee.getUser().getId().toString(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDepartment() != null ? employee.getDepartment().getTitle() : null
                ))
                .toList();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getAttachmentUrl(),
                task.getAttachmentName(),
                task.getStatus().toString(),
                projectResponse,
                assignees
        );
    }
}
