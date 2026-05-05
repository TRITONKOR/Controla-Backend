package org.tritonkor.controlabackend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.tritonkor.controlabackend.user.dto.UserStatusResponse;
import org.tritonkor.controlabackend.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з email " + email + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public Boolean isUserApproved(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getIsApproved() && user.getIsActive())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public UserStatusResponse getUserStatus(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new UserStatusResponse(
                        user.getId(),
                        user.getRole(),
                        user.getIsApproved(),
                        user.getIsActive()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }


}
