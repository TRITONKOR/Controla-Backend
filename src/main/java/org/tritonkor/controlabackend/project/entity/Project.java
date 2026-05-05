package org.tritonkor.controlabackend.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.task.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Employee owner;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(columnDefinition = "integer default 0")
    private int costs;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_assignees",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> assignees = new HashSet<>();

    public Project(String title, String description, int costs, LocalDateTime deadline, Employee owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.costs = costs;
        this.deadline = deadline;
        this.status = ProjectStatus.ACTIVE;
    }
}
