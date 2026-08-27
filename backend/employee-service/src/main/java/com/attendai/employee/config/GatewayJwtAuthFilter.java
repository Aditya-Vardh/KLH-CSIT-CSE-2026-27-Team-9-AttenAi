package com.attendai.employee.config;

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

/**
 * Trusts the headers forwarded by the API Gateway after it has already validated the JWT.
 * Builds a SecurityContext from X-Auth-User-Email and X-Auth-User-Role so that
 * @PreAuthorize role checks work on every endpoint without re-parsing the JWT.
 */
@Component
public class GatewayJwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String email = request.getHeader("X-Auth-User-Email");
        String role  = request.getHeader("X-Auth-User-Role");

        if (email != null && !email.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Prefix role with ROLE_ if not already prefixed (gateway sends e.g. "ADMIN")
            String authority = (role != null && !role.isBlank())
                    ? (role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    : "ROLE_EMPLOYEE";

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email, null,
                            List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
