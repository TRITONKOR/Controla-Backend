package org.tritonkor.controlabackend.auth.controller;

import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.tritonkor.controlabackend.auth.dto.AuthResponse;
import org.tritonkor.controlabackend.auth.dto.LoginRequest;
import org.tritonkor.controlabackend.auth.dto.LogoutRequest;
import org.tritonkor.controlabackend.auth.dto.RefreshTokenRequest;
import org.tritonkor.controlabackend.auth.dto.RegisterRequest;
import org.tritonkor.controlabackend.auth.service.JwtService;
import org.tritonkor.controlabackend.auth.service.TokenBlacklistService;
import org.tritonkor.controlabackend.employee.entity.Employee;
import org.tritonkor.controlabackend.employee.entity.Role;
import org.tritonkor.controlabackend.employee.repository.EmployeeRepository;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = employeeRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        try {
            if (tokenBlacklistService.isBlacklisted(request.refreshToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
            }

            String email = jwtService.extractUsername(request.refreshToken());
            UserDetails userDetails = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

            if (!jwtService.isRefreshTokenValid(request.refreshToken(), userDetails)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return new AuthResponse(accessToken, refreshToken, "Bearer");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public String logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LogoutRequest request) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                tokenBlacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));
            } catch (Exception ignored) {}
        }

        try {
            tokenBlacklistService.blacklist(
                    request.refreshToken(),
                    jwtService.extractExpiration(request.refreshToken())
            );
        } catch (Exception ignored) {}

        return "LOGGED_OUT";
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setHashPassword(passwordEncoder.encode(request.password()));
        employee.setDepartment(null);
        employee.setRole(Role.EMPLOYEE);

        employeeRepository.save(employee);

        return "REGISTERED";
    }
}
