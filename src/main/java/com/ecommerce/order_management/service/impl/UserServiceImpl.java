package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.Role;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.UserRepository;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du service utilisateur (logique métier).
 * - gère CRUD
 * - normalise email
 * - encode mot de passe
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // récupérer user par id
    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé id=" + id));
    }

    // récupérer user par email (normalisé)
    @Override
    public User findByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé email=" + email));
    }

    // vérifier existence email (normalisé)
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    // liste tous les users
    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * Inscription :
     * - validation
     * - normalisation email
     * - encodage password
     */
    @Override
    @Transactional
    public User register(User user) {

        // validation email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email requis");
        }

        // normalisation email
        String normalizedEmail = user.getEmail().trim().toLowerCase();

        // vérifier unicité
        if (existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        // validation password
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mot de passe invalide (>=8 caractères)");
        }

        // appliquer email normalisé
        user.setEmail(normalizedEmail);

        // encoder password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // rôle par défaut
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        return userRepository.save(user);
    }

    // mise à jour utilisateur
    @Override
    @Transactional
    public User update(Long id, User updated) {

        User existing = findById(id);

        if (updated.getFirstName() != null) {
            existing.setFirstName(updated.getFirstName());
        }

        if (updated.getLastName() != null) {
            existing.setLastName(updated.getLastName());
        }

        // mise à jour email (avec normalisation + unicité)
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            String normalizedEmail = updated.getEmail().trim().toLowerCase();

            if (!normalizedEmail.equals(existing.getEmail()) && existsByEmail(normalizedEmail)) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }

            existing.setEmail(normalizedEmail);
        }

        // mise à jour password (encodé)
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        // mise à jour rôle
        if (updated.getRole() != null) {
            existing.setRole(updated.getRole());
        }

        return userRepository.save(existing);
    }

    // suppression user
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Utilisateur non trouvé id=" + id);
        }
        userRepository.deleteById(id);
    }
}