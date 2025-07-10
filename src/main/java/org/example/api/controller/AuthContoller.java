package org.example.api.controller;

import jakarta.validation.Valid;
import org.example.dto.JwtResponse;
import org.example.dto.LoginRequest;
import org.example.dto.MessageResponse;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.security.JwtUtils;
import org.example.security.UserPrincipal;
import org.example.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthContoller {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthContoller(AuthenticationManager authenticationManager, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Błąd logowania: " + e.getMessage()));
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            try {
                userService.findByLogin(registerRequest.getLogin());
                return ResponseEntity.badRequest().body(new MessageResponse("Błąd: Login jest już zajęty!"));
            } catch (UsernameNotFoundException e) {

            }
            String role = registerRequest.getRoles() != null ? registerRequest.getRoles().toString().toLowerCase() : "user";

            User user = userService.createUser(
                    registerRequest.getLogin(),registerRequest.getPassword(),role
            );
            return ResponseEntity.ok(new MessageResponse("Użytkownik został zarejestrowany pomyślnie!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Bład resjestracji " + e.getMessage()));
        }
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null) {
            return  ResponseEntity.badRequest().body(new MessageResponse("Brak autoryzacji!"));
        }
        String username = authentication.getName();
        User user = userService.findByLogin(username);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Użytkownik nie znaleziony!"));
        }
        return  ResponseEntity.ok(user);
    }
}
