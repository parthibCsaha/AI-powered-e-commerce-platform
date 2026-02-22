package com.backend.service;

import com.backend.dto.LoginRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
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

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public String register(User user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        // Encode password before saving
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(user.getRole());
        User savedUser = userRepository.save(newUser);
        log.info("new user :{}", savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "User registered successfully with id: " + savedUser.getId();
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

        return "User logged in successfully";
    }


}
