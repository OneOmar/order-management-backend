package com.ecommerce.order_management.security;

import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Service dédié uniquement à l'auth (évite les dépendances circulaires)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // check CRITIQUE
        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        return user;
    }
}