package org.tritonkor.controlabackend.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.project.entity.Project;
import org.tritonkor.controlabackend.employee.entity.Employee;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task extends AuditableEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> assignees = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public Task(String title, String description, String attachmentUrl, Project project) {
        this.title = title;
        this.description = description;
        this.attachmentUrl = attachmentUrl;
        this.project = project;
        this.status = TaskStatus.TO_DO;
    }
}
