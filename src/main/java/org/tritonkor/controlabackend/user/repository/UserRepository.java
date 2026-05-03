package org.tritonkor.controlabackend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tritonkor.controlabackend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
}
