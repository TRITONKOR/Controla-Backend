package org.tritonkor.controlabackend.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.employee.entity.Employee;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);
}
