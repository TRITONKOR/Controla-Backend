package org.tritonkor.controlabackend.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.project.dto.CreateProjectRequest;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.service.ProjectService;
import org.tritonkor.controlabackend.task.dto.CreateTaskRequest;
import org.tritonkor.controlabackend.task.dto.TaskResponse;
import org.tritonkor.controlabackend.task.dto.TaskStatusUpdateRequest;
import org.tritonkor.controlabackend.task.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/project/{projectId}")
    public List<TaskResponse> getAllTasksByProjectId(@PathVariable UUID projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            return taskService.createTask(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody TaskStatusUpdateRequest request) {
        try {
            return taskService.updateTaskStatus(taskId, request.status().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
