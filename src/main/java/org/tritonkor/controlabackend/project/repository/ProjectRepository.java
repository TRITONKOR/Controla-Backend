package org.tritonkor.controlabackend.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.project.entity.Project;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByTitle(String title);
}
