package com.ecommerce.order_management.service;

import com.ecommerce.order_management.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * Contrat pour la couche service utilisateur.
 * Étend UserDetailsService pour intégrer Spring Security (loadUserByUsername).
 * Implémentations : UserServiceImpl.
 */
public interface UserService extends UserDetailsService {

    User findById(Long id);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> listAll();

    /**
     * Inscription : valide l'input, encode le mot de passe, assigne ROLE_USER par défaut.
     */
    User register(User user);

    /**
     * Mise à jour partielle ou complète.
     */
    User update(Long id, User updated);

    void deleteById(Long id);
}