package org.tritonkor.controlabackend.employee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.department.entity.Department;
import org.tritonkor.controlabackend.user.entity.User;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends AuditableEntity {

    private String firstName;
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = true)
    private User user;

    public Employee(String firstName, String lastName, Department department, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.user = user;
    }
}
