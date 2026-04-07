// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import com.wearemobi.auth.component.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    // 1. ¿Hay un Bearer Token en el encabezado?
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      final String userEmail =
          jwtService.extractClaims(jwt).getSubject(); // O jwtService.extractUsername(jwt)

      // 2. Si hay email y no estamos ya autenticados...
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // Creamos la autenticación de Spring basada en los datos del JWT
        // Nota: Por ahora pasamos roles vacíos o los extraemos del JWT si los tienes
        var authToken =
            new UsernamePasswordAuthenticationToken(userEmail, null, Collections.emptyList());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 🚀 ¡EL MOMENTO CLAVE!: Seteamos la identidad en el contexto
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (Exception e) {
      // Si el token es inválido o expiró, no hacemos nada (el filtro siguiente dará 403)
      logger.error("Could not set user authentication in security context", e);
    }

    filterChain.doFilter(request, response);
  }
}
