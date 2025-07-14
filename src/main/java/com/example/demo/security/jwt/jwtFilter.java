package com.example.demo.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.security.UsuarioDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class jwtFilter extends OncePerRequestFilter {

  @Autowired
  private jwtUtil jwtUtil;

  @Autowired
  private UsuarioDetailsService userDetailsService;

  private static final List<String> PUBLIC_PATHS = List.of(
    "/empleado/login", "/empleado/registrar", "/empleado/verificar", "/plato/listar"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain)
      throws ServletException, IOException {

    final String requestURI = request.getRequestURI();
    if (isPublicPath(requestURI)) {
      chain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    final String token = authHeader.substring(7);
    String username;

    try {
      username = jwtUtil.extractUsername(token);
      
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        // --- LOGS DE DEPURACIÓN EN LA VALIDACIÓN ---
        log.info("--- jwtFilter: Validando Token ---");
        log.info(">>> Username extraído del Token: '{}'", username);
        log.info(">>> Username cargado de UserDetails: '{}'", userDetails.getUsername());
        boolean usernamesMatch = username.equals(userDetails.getUsername());
        log.info(">>> ¿Los usernames coinciden?: {}", usernamesMatch);
        boolean isTokenValid = jwtUtil.validateToken(token, userDetails);
        log.info(">>> ¿Resultado de jwtUtil.validateToken?: {}", isTokenValid);
        // -------------------------------------------

        if (isTokenValid) {
          log.info("+++ ¡VALIDACIÓN EXITOSA! Estableciendo autenticación para '{}'", username);
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
          log.error("--- ¡VALIDACIÓN FALLIDA! jwtUtil.validateToken devolvió false.");
        }
      }
    } catch (JwtException ex) {
      log.error("!!! Error de JWT: {}. Causa: {}", ex.getMessage(), ex.getCause());
      // Puedes añadir una respuesta de error si quieres
    }

    chain.doFilter(request, response);
  }

  private boolean isPublicPath(String requestURI) {
    return PUBLIC_PATHS.stream().anyMatch(requestURI::equals) || requestURI.startsWith("/uploads/");
  }
}
