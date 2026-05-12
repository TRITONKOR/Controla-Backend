package org.tritonkor.controlabackend.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tritonkor.controlabackend.project.dto.ProjectResponse;
import org.tritonkor.controlabackend.project.service.ProjectService;
import org.tritonkor.controlabackend.user.dto.UpdateUserRequest;
import org.tritonkor.controlabackend.user.dto.UserDetailedResponse;
import org.tritonkor.controlabackend.user.dto.UserStatusResponse;
import org.tritonkor.controlabackend.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDetailedResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<UserStatusResponse> getUserStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserStatus(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailedResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @GetMapping("/{userId}/projects")
    public List<ProjectResponse> getEmployeeProjects(@PathVariable UUID userId) {
        return projectService.getUserProjects(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDetailedResponse> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PatchMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDetailedResponse> uploadAvatar(
            @PathVariable UUID userId,
            @RequestParam("avatar") MultipartFile avatar) {
        return ResponseEntity.ok(userService.updateAvatar(userId, avatar));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}
