package org.tritonkor.controlabackend.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tritonkor.controlabackend.employee.dto.EmployeeResponse;
import org.tritonkor.controlabackend.project.dto.CreateProjectRequest;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@RequestBody CreateProjectRequest request) {
        try {
            return projectService.createProject(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{projectId}/employees")
    public List<EmployeeResponse> getProjectAssignees(@PathVariable UUID projectId) {
        try {
            return projectService.getProjectAssignees(projectId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
