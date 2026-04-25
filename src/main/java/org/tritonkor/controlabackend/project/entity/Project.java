package org.tritonkor.controlabackend.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.task.entity.Task;
import org.tritonkor.controlabackend.employee.entity.Employee;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Employee owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    public Project(String title, Employee owner) {
        this.title = title;
        this.owner = owner;
    }
}
