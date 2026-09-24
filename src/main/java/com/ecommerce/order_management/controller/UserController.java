package com.ecommerce.order_management.controller;

import com.ecommerce.order_management.dto.request.UserUpdateRequest;
import com.ecommerce.order_management.dto.response.UserResponseDTO;
import com.ecommerce.order_management.entity.Role;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * ADMIN ONLY
     * Retourne tous les utilisateurs (version DTO → sans password)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAll() {

        List<UserResponseDTO> users = userService.listAll()
                .stream()
                .map(this::map) // conversion User -> DTO
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * ADMIN ONLY
     * Récupérer un user par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {

        User user = userService.findById(id);

        return ResponseEntity.ok(map(user)); // conversion en DTO
    }

    /**
     * ADMIN ONLY
     * Mise à jour user (on garde encore User pour l’instant)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {

        User updatedUser = mapToEntity(request);

        User saved = userService.update(id, updatedUser);

        return ResponseEntity.ok(map(saved));
    }

    /**
     * ADMIN ONLY
     * Suppression user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // User -> DTO
    private UserResponseDTO map(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEnabled()
        );
    }

    // DTO -> User (partiel)
    private User mapToEntity(UserUpdateRequest req) {
        return User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .password(req.password())
                .role(req.role() == null ? null : Role.valueOf(req.role()))
                .build();
    }
}