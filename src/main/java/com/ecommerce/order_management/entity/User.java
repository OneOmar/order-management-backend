package com.ecommerce.order_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Entité User ; implémente UserDetails pour Spring Security.
 * Ajout de validations : email, password minimal, noms non vides.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // email valide et non vide
    @Column(unique = true, nullable = false)
    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;

    // mot de passe stocké encodé ; contrainte minimale de longueur (côté backend)
    @Column(nullable = false)
    @NotBlank(message = "Mot de passe requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    // noms facultatifs mais si fournis on évite les chaînes vides
    @NotBlank(message = "Prénom requis")
    private String firstName;

    @NotBlank(message = "Nom requis")
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // UserDetails impl.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    // Les autres méthodes restent true (pas de gestion d'expiration ici).
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked()  { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}