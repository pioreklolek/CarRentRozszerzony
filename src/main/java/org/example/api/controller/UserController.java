package org.example.api.controller;

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
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<User>> getActiveUsers() {
        return ResponseEntity.ok(userService.findAllActiveUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or authentication.principal.username == @userServiceImpl.findById(#id).orElse(new org.example.model.User()).login")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updateUser = userService.updateUser(id,userDetails);
            return ResponseEntity.ok(updateUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<User>> getDeletedUsers(){
        return ResponseEntity.ok(userService.findAllDeletedUsers());
    }
    @GetMapping("/allUsers")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.findAll());
    }
    @PostMapping("/addrole/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> addRoleToUser(@PathVariable Long userId, @RequestBody AddRoleRequest request) {
        try {
            User user = userService.addRoleToUser(userId,request.getRoleName());
            return ResponseEntity.ok(new MessageResponse("Rola została dodana do użytkownika"));
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(new MessageResponse("Bład:" + e.getMessage()));
        }
    }
    @DeleteMapping("/removerole/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long userId, @RequestBody AddRoleRequest request) {
        try {
             User user = userService.removeRoleFromUser(userId,request.getRoleName());
             return ResponseEntity.ok(new MessageResponse("Rola " + request.getRoleName() + " została zabrana użytkownikowi " + user.getLogin()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Błąd: " + e.getMessage()));
        }
    }
}
