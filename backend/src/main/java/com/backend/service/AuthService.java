package com.backend.service;

import com.backend.dto.LoginRequest;
import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
import com.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest registerRequest) {
        User newUser = new User();
        newUser.setUsername(registerRequest.username());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));
        newUser.setEmail(registerRequest.email());
        newUser.setRole(registerRequest.role());
        User savedUser = userRepository.save(newUser);
        log.info("new user :{}", savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                registerRequest.username(),
                registerRequest.password()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "User registered successfully with username: " + savedUser.getUsername();
    }

    public String login(LoginRequest loginRequest) {
        log.info("login request :{}", loginRequest.username());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", loginRequest.username(), e.getMessage());
            return "Invalid username or password";
        }

        if (authentication.isAuthenticated()) {
            log.info("User {} logged in successfully", loginRequest.username());
        } else {
            log.warn("Failed login attempt for user {}", loginRequest.username());
            return "Invalid username or password";
        }

        String jwt = jwtUtil.generateToken(loginRequest.username());
        log.info("Generated JWT for user {}: {}", loginRequest.username(), jwt);


        return "Login successful. JWT token: " + jwt;
    }


}
