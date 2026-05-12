package org.tritonkor.controlabackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tritonkor.controlabackend.admin.dto.UpdateUserRoleRequest;
import org.tritonkor.controlabackend.user.dto.UserDetailedResponse;
import org.tritonkor.controlabackend.user.dto.UserStatusResponse;
import org.tritonkor.controlabackend.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserStatusResponse> updateUserRole(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(userId, request.role()));
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<UserStatusResponse> approveUser(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.approveUser(userId));
    }

}
