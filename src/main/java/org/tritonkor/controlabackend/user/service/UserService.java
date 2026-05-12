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
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.project.repository.ProjectRepository;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.task.repository.TaskRepository;
import org.tritonkor.controlabackend.user.dto.UpdateUserRequest;
import org.tritonkor.controlabackend.user.dto.UserDetailedResponse;
import org.tritonkor.controlabackend.user.dto.UserResponse;
import org.tritonkor.controlabackend.user.dto.UserStatusResponse;
import org.tritonkor.controlabackend.user.entity.Role;
import org.tritonkor.controlabackend.user.entity.User;
import org.tritonkor.controlabackend.user.repository.UserRepository;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з email " + email + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public List<UserDetailedResponse> getAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Employee employee = employeeRepository.findByUser(user)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
                    return toDetailedResponse(user, employee);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return toDetailedResponse(user, employee);
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
    public UserStatusResponse updateUserRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(newRole);
        userRepository.save(user);

        return new UserStatusResponse(
                user.getId(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive()
        );
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

        return toDetailedResponse(user, employee);
    }

    @Transactional
    public UserStatusResponse approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setIsApproved(true);
        userRepository.save(user);

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return toStatusResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        List<Project> assignedProjects = projectRepository.findByAssigneesContaining(employee);
        for (Project project : assignedProjects) {
            project.getAssignees().remove(employee);
            projectRepository.save(project);
        }

        List<Project> ownedProjects = projectRepository.findByOwner(employee);
        projectRepository.deleteAll(ownedProjects);

        List<Task> assignedTasks = taskRepository.findByAssigneesContaining(employee);
        for (Task task : assignedTasks) {
            task.getAssignees().remove(employee);
            taskRepository.save(task);
        }

        employeeRepository.delete(employee);
        userRepository.delete(user);
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

        return toDetailedResponse(user, employee);
    }

    private UserDetailedResponse toDetailedResponse(User user, Employee employee) {
        String avatarBase64 = user.getAvatar() != null
                ? Base64.getEncoder().encodeToString(user.getAvatar())
                : null;

        return new UserDetailedResponse(
                user.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive(),
                avatarBase64,
                user.getCreatedAt()
        );
    }

    private UserStatusResponse toStatusResponse(User user) {
        return new UserStatusResponse(
                user.getId(),
                user.getRole(),
                user.getIsApproved(),
                user.getIsActive()
        );
    }
}
