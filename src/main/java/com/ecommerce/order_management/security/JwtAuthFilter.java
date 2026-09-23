package com.ecommerce.order_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter :
 * - lit le header Authorization
 * - valide le JWT
 * - injecte l'utilisateur dans le SecurityContext
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) Lire le header Authorization
        String authHeader = request.getHeader("Authorization");

        // Pas de token → continuer sans auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Extraire le token
        String token = authHeader.substring(7).trim();
        String username;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // Token invalide → ignorer et continuer
            filterChain.doFilter(request, response);
            return;
        }

        // 3) Authentification si pas déjà faite
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Vérifier token
            if (jwtUtil.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Injecter dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 4) Continuer la requête
        filterChain.doFilter(request, response);
    }
}