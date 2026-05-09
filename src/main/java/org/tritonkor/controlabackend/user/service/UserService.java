package org.tritonkor.controlabackend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.tritonkor.controlabackend.department.entity.Department;
import org.tritonkor.controlabackend.department.repository.DepartmentRepository;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.user.dto.UpdateUserRequest;
import org.tritonkor.controlabackend.user.dto.UserDetailedResponse;
import org.tritonkor.controlabackend.user.dto.UserStatusResponse;
import org.tritonkor.controlabackend.user.entity.User;
import org.tritonkor.controlabackend.user.repository.UserRepository;

import java.util.Base64;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з email " + email + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public Boolean isUserApproved(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getIsApproved() && user.getIsActive())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (user.getAvatar() == null) {
            user.setAvatar(new byte[0]);
        }

        return new UserDetailedResponse(
                employee.getFirstName(),
                employee.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive(),
                Base64.getEncoder().encodeToString(user.getAvatar()
        ));
    }

    @Transactional(readOnly = true)
    public UserStatusResponse getUserStatus(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new UserStatusResponse(
                        user.getId(),
                        user.getRole(),
                        user.getIsApproved(),
                        user.getIsActive()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public UserDetailedResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (request.firstName() != null) employee.setFirstName(request.firstName());
        if (request.lastName() != null) employee.setLastName(request.lastName());
        if (request.position() != null) employee.setPosition(request.position());

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            employee.setDepartment(department);
        }

        employeeRepository.save(employee);
        userRepository.save(user);

        String avatarBase64 = user.getAvatar() != null
                ? Base64.getEncoder().encodeToString(user.getAvatar())
                : null;

        return new UserDetailedResponse(
                employee.getFirstName(),
                employee.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive(),
                avatarBase64
        );
    }

    @Transactional
    public UserDetailedResponse updateAvatar(UUID userId, MultipartFile avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try {
            user.setAvatar(avatar.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read avatar file");
        }

        userRepository.save(user);

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));


        String avatarBase64 = user.getAvatar() != null
                ? Base64.getEncoder().encodeToString(user.getAvatar())
                : null;

        return new UserDetailedResponse(
                employee.getFirstName(),
                employee.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive(),
                avatarBase64
        );
    }

}
