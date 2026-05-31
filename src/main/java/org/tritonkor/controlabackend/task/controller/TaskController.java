package org.tritonkor.controlabackend.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.task.dto.CreateTaskRequest;
import org.tritonkor.controlabackend.task.dto.TaskResponse;
import org.tritonkor.controlabackend.task.dto.TaskStatusUpdateRequest;
import org.tritonkor.controlabackend.task.dto.UpdateTaskRequest;
import org.tritonkor.controlabackend.task.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {


    private final TaskService taskService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getAllUserTasks(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/project/{projectId}")
    public List<TaskResponse> getAllTasksByProjectId(@PathVariable UUID projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        try {
            return taskService.createTask(request);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{task_id}")
    public TaskResponse updateTask(@PathVariable UUID task_id,
                                   @Valid @RequestBody UpdateTaskRequest request
    ) {
        try {
            return taskService.updateTask(task_id, request);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @PostMapping("/{taskId}/assignees/{employeeId}")
    public ResponseEntity<TaskResponse> assignEmployee(
            @PathVariable UUID taskId,
            @PathVariable UUID employeeId
    ) {
        return ResponseEntity.ok(taskService.assignEmployeeToTask(taskId, employeeId));
    }

    @DeleteMapping("/{taskId}/assignees/{employeeId}")
    public ResponseEntity<TaskResponse> removeAssignee(
            @PathVariable UUID taskId,
            @PathVariable UUID employeeId
    ) {
        return ResponseEntity.ok(taskService.removeAssignee(taskId, employeeId));
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
