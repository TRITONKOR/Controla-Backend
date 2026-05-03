package org.tritonkor.controlabackend.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.tritonkor.controlabackend.auth.dto.AuthResponse;
import org.tritonkor.controlabackend.auth.dto.LoginRequest;
import org.tritonkor.controlabackend.auth.dto.RegisterRequest;
import org.tritonkor.controlabackend.auth.service.JwtService;
import org.tritonkor.controlabackend.auth.service.TokenBlacklistService;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.tritonkor.controlabackend.user.entity.Role;
import org.tritonkor.controlabackend.user.entity.User;
import org.tritonkor.controlabackend.user.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return buildAuthResponse(accessToken, user);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing");
        }

        try {
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
            }

            String email = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

            if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            tokenBlacklistService.blacklist(refreshToken, jwtService.extractExpiration(refreshToken));

            Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, newRefreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/api/auth");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return buildAuthResponse(newAccessToken, user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                tokenBlacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));
            } catch (Exception ignored) {}
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                tokenBlacklistService.blacklist(refreshToken, jwtService.extractExpiration(refreshToken));
            } catch (Exception ignored) {}
        }

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setHashPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.PENDING);
        user.setIsApproved(false);
        user.setIsActive(true);
        user = userRepository.save(user);

        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setDepartment(null);
        employee.setUser(user);

        employeeRepository.save(employee);

        return ResponseEntity.ok().build();
    }

    private AuthResponse buildAuthResponse(String accessToken, User user) {
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Employee profile not found"));

        String avatarBase64 = user.getAvatar() == null
                ? null
                : Base64.getEncoder().encodeToString(user.getAvatar());

        return new AuthResponse(
                accessToken,
                new AuthResponse.AuthUser(
                        user.getId(),
                        user.getEmail(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        user.getRole().name(),
                        avatarBase64,
                        user.getIsActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                )
        );
    }
}
