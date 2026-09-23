package com.ecommerce.order_management.service.impl;

import com.ecommerce.order_management.entity.Role;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.exception.NotFoundException;
import com.ecommerce.order_management.repository.UserRepository;
import com.ecommerce.order_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation concrète du contrat UserService.
 * - encode les mots de passe lors de l'inscription
 * - effectue validations simples (unicité email, longueur pwd)
 * - délègue les lectures/écritures au UserRepository
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé id=" + id));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé email=" + email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * Inscription : vérifie l'unicité, encode mot de passe, assigne ROLE_USER si absent.
     */
    @Override
    @Transactional
    public User register(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email requis");
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mot de passe invalide (>=8 caractères)");
        }

        // encode et persist
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, User updated) {
        User existing = findById(id);
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        if (updated.getRole() != null) existing.setRole(updated.getRole());
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Utilisateur non trouvé id=" + id);
        }
        userRepository.deleteById(id);
    }
}