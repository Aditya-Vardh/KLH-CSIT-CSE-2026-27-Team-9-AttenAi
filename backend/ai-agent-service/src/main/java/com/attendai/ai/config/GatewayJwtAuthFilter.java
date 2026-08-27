package com.attendai.ai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayJwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String email = req.getHeader("X-Auth-User-Email");
        String role  = req.getHeader("X-Auth-User-Role");
        if (email != null && !email.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String auth = (role != null && !role.isBlank())
                    ? (role.startsWith("ROLE_") ? role : "ROLE_" + role) : "ROLE_EMPLOYEE";
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(auth))));
        }
        chain.doFilter(req, res);
    }
}
