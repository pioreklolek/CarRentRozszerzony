package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Authentication", description = "API do autoryzacji i uwierzytelniania użytkowników")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika", description = "Uwierzytelnia użytkownika i zwraca token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logowanie pomyślne",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane logowania",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> authUser(
            @Parameter(description = "Dane logowania użytkownika", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
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
    @Operation(summary = "Rejestracja nowego użytkownika", description = "Tworzy nowe konto użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rejestracja pomyślna",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błąd podczas rejestracji",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> registerUser(
            @Parameter(description = "Dane rejestracyjne użytkownika", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            try {
                userService.findByLogin(registerRequest.getLogin());
                return ResponseEntity.badRequest().body(new MessageResponse("Błąd: Login jest już zajęty!"));
            } catch (UsernameNotFoundException e) {
                // Login jest dostępny
            }
            String role = registerRequest.getRoles() != null ? registerRequest.getRoles().toString().toLowerCase() : "user";

            User user = userService.createUser(
                    registerRequest.getLogin(),
                    registerRequest.getPassword(),
                    role,
                    registerRequest.getAddress(),
                    registerRequest.getPostalCode(),
                    registerRequest.getCountry()
            );
            return ResponseEntity.ok(new MessageResponse("Użytkownik został zarejestrowany pomyślnie!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Błąd rejestracji: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Pobierz profil użytkownika", description = "Zwraca dane profilu zalogowanego użytkownika")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil użytkownika",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Brak autoryzacji lub użytkownik nie znaleziony",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> getUserProfile(
            @Parameter(hidden = true) Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Brak autoryzacji!"));
        }
        String username = authentication.getName();
        User user = userService.findByLogin(username);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Użytkownik nie znaleziony!"));
        }
        return ResponseEntity.ok(user);
    }
}