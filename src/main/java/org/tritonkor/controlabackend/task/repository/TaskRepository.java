package org.tritonkor.controlabackend.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.task.entity.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByTitle(String title);

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssigneesContaining(Employee employee);
}
