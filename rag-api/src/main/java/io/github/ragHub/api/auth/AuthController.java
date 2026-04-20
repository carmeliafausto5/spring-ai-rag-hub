package io.github.ragHub.api.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    record Credentials(String username, String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Credentials creds) {
        if (users.findByUsername(creds.username()).isPresent())
            return ResponseEntity.status(409).body(Map.of("error", "Username taken"));
        User u = new User();
        u.setUsername(creds.username());
        u.setPasswordHash(encoder.encode(creds.password()));
        users.save(u);
        return ResponseEntity.status(201).body(Map.of("token", jwtUtil.generate(u.getUsername(), u.getRole())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials creds) {
        return users.findByUsername(creds.username())
                .filter(u -> encoder.matches(creds.password(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("token", jwtUtil.generate(u.getUsername(), u.getRole()))))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}
