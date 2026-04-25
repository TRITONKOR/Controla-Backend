package org.tritonkor.controlabackend.department.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;
import org.tritonkor.controlabackend.employee.entity.Employee;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String title;

    @OneToMany(mappedBy = "department", cascade = CascadeType.MERGE)
    private Set<Employee> employees = new HashSet<>();

    public Department(String title) {
        this.title = title;
    }
}
