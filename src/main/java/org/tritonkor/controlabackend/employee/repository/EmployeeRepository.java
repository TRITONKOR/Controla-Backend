package org.tritonkor.controlabackend.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
	Optional<Employee> findByUser(User user);

	Optional<Employee> findByUserEmail(String email);

	Optional<Employee> findByUserId(UUID userId);
}

