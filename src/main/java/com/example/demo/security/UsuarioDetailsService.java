package com.example.demo.security;

import com.example.demo.dao.UsuarioRepository;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import lombok.extern.slf4j.Slf4j; // ¡Asegúrate de importar Slf4j!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j // ¡Añade esta anotación para que funcionen los logs!
@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioDao;

    private Usuario userDetail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("--- UsuarioDetailsService: Buscando usuario por email: '{}'", username);
        userDetail = usuarioDao.findByCorreo(username);

        if (!Objects.isNull(userDetail)) {
            Rol rol = userDetail.getRol();
            String roleName = "ROLE_" + (rol != null ? rol.getNombre().toUpperCase() : "DEFAULT");
            
            // --- LOGS DE DEPURACIÓN ---
            log.info(">>> Usuario encontrado en BD: '{}'", userDetail.getEmail());
            log.info(">>> Contraseña (hash) en BD: (longitud {})", userDetail.getContrasena().length());
            log.info(">>> Rol para Spring Security: '{}'", roleName);
            // --------------------------

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
            
            return new User(userDetail.getEmail(), userDetail.getContrasena(), authorities);
        } else {
            log.error("!!! Usuario no encontrado en la base de datos con el email: {}", username);
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + username);
        }
    }

    public Usuario getUserDetail() {
        return userDetail;
    }
}
