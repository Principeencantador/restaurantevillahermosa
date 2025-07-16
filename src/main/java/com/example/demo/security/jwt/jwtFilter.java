package com.example.demo.security.jwt;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Importante
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

    // --- SE ELIMINA LA LISTA DE RUTAS PÚBLICAS ---
    // private static final List<String> PUBLIC_PATHS = ...;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay cabecera o no es de tipo Bearer, pasamos al siguiente filtro.
        // Spring Security se encargará de permitir o denegar el acceso según las reglas
        // de SecurityConfig.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String username = null;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (JwtException ex) {
            log.error("Error al procesar el token JWT: {}", ex.getMessage());
        }

        // Si tenemos el username y no hay una autenticación activa en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                log.info("Token válido para el usuario '{}'. Estableciendo autenticación.", username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // Añadimos detalles de la petición a la autenticación
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuamos con la cadena de filtros
        chain.doFilter(request, response);
    }
    
    // --- SE ELIMINA EL MÉTODO isPublicPath ---
    // private boolean isPublicPath(String requestURI) { ... }
}
