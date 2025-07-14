package com.example.demo.security;

import com.example.demo.security.jwt.jwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService empleadoDetailsService;

    @Autowired
    private jwtFilter jwtFilter;

    // --- Beans de Configuración (Estos ya estaban correctos) ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProv = new DaoAuthenticationProvider();
        authProv.setUserDetailsService(empleadoDetailsService);
        authProv.setPasswordEncoder(passwordEncoder());
        return authProv;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3600", "https://restaurantevillahermosa.netlify.app/"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // --- REGLAS DE SEGURIDAD REFACTORIZADAS, SEGURAS Y COMPLETAS ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable()) // Forma moderna de deshabilitar CSRF
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authenticationProvider(authenticationProvider())
          .authorizeHttpRequests(auth -> auth
              
              // --- 1. Endpoints Públicos (No requieren token) ---
              // Rutas para autenticación, registro y visualización pública del menú.
              .requestMatchers(
                  "/empleado/login",
                  "/empleado/registrar",
                  "/empleado/verificar",
                  "/plato/listar",
                  "/reserva/disponibilidad",
                  "/pedido/webhook", // Los webhooks de Stripe deben ser públicos
                  "/success", 
                  "/sucess",
                  "/uploads/**"         // Para ver las imágenes de los platos
              ).permitAll()

              // --- 2. Endpoints para Usuarios Autenticados (USER o ADMIN) ---
              // Acciones que un usuario logueado puede realizar sobre sus propios pedidos y reservas.
              .requestMatchers(
                  "/pedido/guardar",
                  "/pedido/listar",
                  "/pedido/comentar",
                  "/pedido/agregar-platos",
                  "/pedido/cancelar-linea",
                  "/pedido/{id}/factura/pdf",
                  "/pedido/{id}/factura/send",
                  "/pedido/create-checkout-session",
                  "/reserva/comentar",
                  "/reserva/cancelar"
              ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
              
              // --- 3. Endpoints solo para Administradores ---
              // Acciones de gestión, reportes y visualización de todos los datos.
              .requestMatchers(
                  "/admin/**", // Prefijo general para rutas de admin
                  "/reserva/crearreporte",
                  "/reserva/excelreservas",
                  "/reserva/diaria",
                  "/reserva/atendidas",
                  "/reserva/ocupadas",
                  "/pedido/listarPendientes",
                  "/pedido/listarAtendidos",
                  "/pedido/actualizarEstado",
                  "/pedido/{id}/rembolso",
                  "/pedido/crearreportepedidos"
              ).hasAuthority("ROLE_ADMIN")

              // --- 4. Regla Final: Denegar todo lo demás por defecto ---
              // Si una ruta no coincide con ninguna regla anterior, se requiere estar autenticado.
              // Esto cubre de forma segura cualquier endpoint que se nos haya olvidado.
              .anyRequest().authenticated()
          )
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
