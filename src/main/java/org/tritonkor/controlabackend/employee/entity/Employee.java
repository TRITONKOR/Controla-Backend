package org.tritonkor.controlabackend.employee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.department.entity.Department;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends AuditableEntity {
    @Column(unique = true, nullable = false)
    private String email;

    private String hashPassword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id",nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Employee(String email, String hashPassword, Department department, Role role) {
        this.email = email;
        this.hashPassword = hashPassword;
        this.department = department;
        this.role = role;
    }
}
