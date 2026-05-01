package org.tritonkor.controlabackend.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.department.entity.Department;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}

