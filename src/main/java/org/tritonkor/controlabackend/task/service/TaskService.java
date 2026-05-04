package org.tritonkor.controlabackend.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.dto.CreateTaskRequest;
import org.tritonkor.controlabackend.task.dto.TaskResponse;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.task.entity.TaskStatus;
import org.tritonkor.controlabackend.task.repository.TaskRepository;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

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
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            task.setAttachments(request.attachments().getBytes());
        } else {
            task.setAttachments(null);
        }

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
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse.ProjectShortResponse projectResponse = new TaskResponse.ProjectShortResponse(
                task.getProject().getId().toString(),
                task.getProject().getTitle()
        );

        List<TaskResponse.EmployeeShortResponse> assignees = task.getAssignees().stream()
                .map(employee -> new TaskResponse.EmployeeShortResponse(
                        employee.getId().toString(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getPosition().toString(),
                        employee.getDepartment().getTitle()
                ))
                .toList();

        String attachmentsString = null;
        if (task.getAttachments() != null) {
            attachmentsString = Base64.getEncoder().encodeToString(task.getAttachments());
        }

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                attachmentsString,
                task.getStatus().toString(),
                projectResponse,
                assignees
        );
    }

}
