// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import com.wearemobi.auth.component.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
          HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    // 1. Verify if Bearer token exists in the Authorization header
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      final String userEmail = jwtService.extractClaims(jwt).getSubject();

      // [FRANKY-DEBUG]: Extracting identity DNA from JWT for user: {}
      logger.debug("Processing authentication for: " + userEmail);

      // 2. Proceed if email is present and security context is not already authenticated
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // 🏺 Robin's Note: Extracting roles from the 'roles' claim in the JWT
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwtService.extractClaims(jwt).get("roles");

        // Map roles to Spring GrantedAuthorities
        var authorities = roles == null ?
                Collections.<SimpleGrantedAuthority>emptyList() :
                roles.stream().map(SimpleGrantedAuthority::new).toList();

        // [FRANKY-DEBUG]: Authorities found: {} -> AUUUU!
        logger.debug("Authorities mapped: " + authorities);

        // Create authentication token with discovered authorities
        var authToken =
                new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 🚀 Set the identity and roles in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (Exception e) {
      // If token is invalid or expired, do nothing; the filter chain will handle the 403 response
      logger.error("Could not set user authentication in security context", e);
    }

    filterChain.doFilter(request, response);
  }
}
