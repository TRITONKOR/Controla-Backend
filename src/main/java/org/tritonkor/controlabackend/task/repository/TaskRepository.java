package org.tritonkor.controlabackend.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.task.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByTitle(String title);

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssigneesContaining(Employee employee);

    @Query("""
    SELECT COUNT(t) > 0
    FROM Task t
    WHERE t.project.id = :projectId
      AND t.id <> :taskId
      AND :employee MEMBER OF t.assignees
""")
    boolean hasOtherTasksInProject(
            @Param("projectId") UUID projectId,
            @Param("taskId") UUID taskId,
            @Param("employee") Employee employee
    );

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE :employee MEMBER OF t.assignees
      AND t.createdAt >= :startDate
      AND t.createdAt < :endDate
""")
    long countAssignedTasksInPeriod(
            @Param("employee") Employee employee,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE :employee MEMBER OF t.assignees
      AND t.status = :status
      AND t.createdAt >= :startDate
      AND t.createdAt < :endDate
""")
    long countCompletedTasksInPeriod(
            @Param("employee") Employee employee,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
