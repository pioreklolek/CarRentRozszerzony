package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.dto.AddRoleRequest;
import org.example.dto.MessageResponse;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API do zarządzania użytkownikami")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz aktywnych użytkowników", description = "Zwraca listę wszystkich aktywnych użytkowników")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista aktywnych użytkowników",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<List<User>> getActiveUsers() {
        return ResponseEntity.ok(userService.findAllActiveUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or authentication.principal.username == @userServiceImpl.findById(#id).orElse(new org.example.model.User()).login")
    @Operation(summary = "Pobierz użytkownika po ID", description = "Zwraca szczegóły użytkownika o podanym ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szczegóły użytkownika",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID użytkownika", required = true, example = "1")
            @PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Aktualizuj użytkownika", description = "Aktualizuje dane użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik zaktualizowany",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID użytkownika", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nowe dane użytkownika", required = true)
            @RequestBody User userDetails) {
        try {
            User updateUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updateUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Usuń użytkownika", description = "Usuwa użytkownika z systemu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Użytkownik usunięty"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID użytkownika", required = true, example = "1")
            @PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz usuniętych użytkowników", description = "Zwraca listę usuniętych użytkowników")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista usuniętych użytkowników",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<List<User>> getDeletedUsers() {
        return ResponseEntity.ok(userService.findAllDeletedUsers());
    }

    @GetMapping("/allUsers")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz wszystkich użytkowników", description = "Zwraca listę wszystkich użytkowników (aktywnych i usuniętych)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista wszystkich użytkowników",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/addrole/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Dodaj rolę użytkownikowi", description = "Przypisuje rolę do użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rola dodana pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błąd podczas dodawania roli",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<?> addRoleToUser(
            @Parameter(description = "ID użytkownika", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Nazwa roli do dodania", required = true)
            @RequestBody AddRoleRequest request) {
        try {
            User user = userService.addRoleToUser(userId, request.getRoleName());
            return ResponseEntity.ok(new MessageResponse("Rola została dodana do użytkownika"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Błąd: " + e.getMessage()));
        }
    }

    @DeleteMapping("/removerole/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Usuń rolę użytkownikowi", description = "Usuwa rolę użytkownikowi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rola usunięta pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błąd podczas usuwania roli",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<?> removeRoleFromUser(
            @Parameter(description = "ID użytkownika", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Nazwa roli do usunięcia", required = true)
            @RequestBody AddRoleRequest request) {
        try {
            User user = userService.removeRoleFromUser(userId, request.getRoleName());
            return ResponseEntity.ok(new MessageResponse("Rola " + request.getRoleName() + " została zabrana użytkownikowi " + user.getLogin()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Błąd: " + e.getMessage()));
        }
    }
}