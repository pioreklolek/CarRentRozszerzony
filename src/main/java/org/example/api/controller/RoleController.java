package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.model.Role;
import org.example.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "API do zarządzania rolami użytkowników")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz wszystkie role", description = "Zwraca listę wszystkich ról w systemie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista ról",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz rolę po ID", description = "Zwraca szczegóły roli o podanym ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szczegóły roli",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "404", description = "Rola nie znaleziona"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagane uprawnienia administratora")
    })
    public ResponseEntity<Role> getRoleById(
            @Parameter(description = "ID roli", required = true, example = "1")
            @PathVariable Long id) {
        try {
            Role role = roleService.findById(id);
            return ResponseEntity.ok(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}