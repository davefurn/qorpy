package com.qorpy.api.security;

import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.respository.BlacklistedTokenRepository;
import com.qorpy.api.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final BlacklistedTokenRepository blacklistRepo;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT processing for public endpoints (Swagger UI, API docs, auth)
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/swagger-ui") ||
                requestPath.startsWith("/v3/api-docs") ||
                requestPath.startsWith("/swagger-resources") ||
                requestPath.startsWith("/webjars") ||
                requestPath.startsWith("/api-docs") ||
                requestPath.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        if (blacklistRepo.existsByToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been invalidated.");
            return;
        }

        userEmail = jwtUtils.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Reject if account is disabled (deactivated)
            if (!userDetails.isEnabled()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Account is deactivated");
                return;
            }

            // Validate token including tokenVersion
            boolean isValid;
            if (userDetails instanceof AdminUserDetails) {
                AdminUser adminUser = ((AdminUserDetails) userDetails).getAdminUser();
                isValid = jwtUtils.isTokenValid(jwt, userDetails.getUsername(), adminUser.getTokenVersion());
            } else {
                isValid = jwtUtils.isTokenValid(jwt, userDetails.getUsername());
            }

            if (!isValid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been invalidated");
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}