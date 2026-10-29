
package com.clickstore.clickstore.security;

import com.clickstore.clickstore.repository.UserRoleRepository;
import com.clickstore.clickstore.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRoleRepository userRoleRepository) {
        this.jwtService = jwtService;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        System.out.println("\n========== JWT FILTER ==========");
        System.out.println("REQUEST: " + request.getMethod() + " " + path);

        String header = request.getHeader("Authorization");

        System.out.println("AUTH HEADER PRESENT: " + (header != null));

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("JWT SKIPPED: Bearer token missing");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            Claims claims = jwtService.extractClaims(token);
            String subject = claims.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("JWT subject is missing");
            }

            Long userId = Long.parseLong(subject);

            System.out.println("JWT VALID, USER ID: " + userId);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities =
                        userRoleRepository.findRoleNamesByUserId(userId)
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        "ROLE_" + role.toUpperCase(Locale.ROOT)))
                                .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                String.valueOf(userId),
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                System.out.println("AUTHORITIES: " + authorities);
            } else {
                System.out.println("AUTHENTICATION ALREADY EXISTS");
            }

        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println(
                    "JWT ERROR: " + ex.getClass().getSimpleName()
                            + " - " + ex.getMessage()
            );

            SecurityContextHolder.clearContext();
        }

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println("FINAL AUTHENTICATED: "
                + (authentication != null && authentication.isAuthenticated()));

        System.out.println("FINAL AUTHORITIES: "
                + (authentication != null
                ? authentication.getAuthorities()
                : "none"));

        try {
            filterChain.doFilter(request, response);
        } finally {
            System.out.println("FINAL HTTP STATUS: " + response.getStatus());
            System.out.println("========== END JWT FILTER ==========\n");
        }
    }
}
