package com.zidio.controller;

import com.zidio.dto.AuthRequest;
import com.zidio.dto.AuthResponse;
import com.zidio.entity.User;
import com.zidio.repository.UserRepository;
import com.zidio.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) return ResponseEntity.badRequest().body("Username already taken");
        if (userRepository.existsByEmail(user.getEmail())) return ResponseEntity.badRequest().body("Email already registered");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        return userRepository.findByUsername(req.getUsername())
            .map(user -> {
                if (passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                    String token = jwtUtils.generateJwtToken(user.getUsername());
                    AuthResponse resp = new AuthResponse(token, user.getId(), user.getRole(), user.getUsername());
                    return ResponseEntity.ok(resp);
                } else return ResponseEntity.status(401).body("Invalid credentials");
            }).orElse(ResponseEntity.status(401).body("User not found"));
    }
}
