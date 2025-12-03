package com.example.demo.service;

import com.example.demo.dao.LoginResponse;
import com.example.demo.dao.RolRepository;
import com.example.demo.dao.UsuarioRepository;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.security.UsuarioDetailsService;
import com.example.demo.security.jwt.jwtFilter;
import com.example.demo.security.jwt.jwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // ✅ IMPORTANTE
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Usuarioimpl implements UsuarioService {

    @Autowired
    private RolRepository roldao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private jwtFilter jwtFilter;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private jwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository usuariodao;

    // ✅ Se leen desde application.properties
    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.fromName:VillaHermosa}")
    private String fromName;

    @Override
    public List<Usuario> traerusuarios() {
        return usuariodao.findAll();
    }

    @Override
    public String signup(Map<String, String> requestMap) {
        if (!validatesignup(requestMap)) {
            return "No se ha podido registrar";
        }

        Usuario existing = usuariodao.findByCorreo(requestMap.get("email"));
        if (Objects.nonNull(existing)) {
            return "El usuario ya existe";
        }

        String verificationCode = generateVerificationCode();

        Usuario nuevo = traerusuario(requestMap);
        nuevo.setVerificationCode(verificationCode);

        usuariodao.save(nuevo);

        // ✅ aquí explotaba por no tener From
        enviarCodigoVerificacion(nuevo.getEmail(), verificationCode);

        return "Se ha registrado correctamente";
    }

    private boolean validatesignup(Map<String, String> m) {
        return m.containsKey("nombre")
                && m.containsKey("email")
                && m.containsKey("password")
                && m.containsKey("telefono")
                && m.containsKey("dni");
    }

    private Usuario traerusuario(Map<String, String> m) {
        Rol rol = roldao.findByNombre(m.get("rol"));
        if (rol == null) {
            rol = roldao.findByNombre("USER");
            if (rol == null) {
                Rol r = new Rol();
                r.setNombre("USER");
                rol = roldao.save(r);
            }
        }

        String rawPwd = m.get("password");
        String encodedPwd = passwordEncoder.encode(rawPwd);

        Usuario u = new Usuario();
        u.setNombre(m.get("nombre"));
        u.setEmail(m.get("email"));
        u.setContrasena(encodedPwd);
        u.setTelefono(m.get("telefono"));
        u.setDni(m.get("dni"));
        u.setRol(rol);
        u.setStatus("false");
        return u;
    }

    private void enviarCodigoVerificacion(String email, String codigo) {
        SimpleMailMessage msg = new SimpleMailMessage();

        // ✅ CLAVE: SendGrid exige From
        msg.setFrom(fromEmail);
        msg.setReplyTo(fromEmail);

        msg.setTo(email);
        msg.setSubject("Código de verificación");
        msg.setText("Tu código de verificación es: " + codigo);

        mailSender.send(msg);
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    @Override
    public ResponseEntity<LoginResponse> login(Map<String, String> requestMap,
                                               HttpSession session,
                                               HttpServletResponse response) {
        try {
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            Usuario user = usuariodao.findByCorreo(email);
            if (user == null) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            if (!passwordEncoder.matches(password, user.getContrasena())) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            if (!"true".equalsIgnoreCase(user.getStatus())) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            String token = jwtUtil.generateToken(user.getEmail(), user);

            session.setAttribute("token", token);
            response.setHeader("Authorization", "Bearer " + token);

            LoginResponse loginResponse = new LoginResponse(token, user.getRol().getNombre());
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> actualizar(Map<String, String> requestMap) {
        String idStr = requestMap.get("id_usuario");
        if (idStr == null || idStr.isBlank()) {
            return new ResponseEntity<>("El ID del usuario es requerido", HttpStatus.BAD_REQUEST);
        }
        try {
            int id = Integer.parseInt(idStr);
            Usuario u = usuariodao.findById(id).orElse(null);
            if (u == null) {
                return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }
            if (requestMap.containsKey("nombre")) u.setNombre(requestMap.get("nombre"));
            if (requestMap.containsKey("email")) u.setEmail(requestMap.get("email"));
            if (requestMap.containsKey("telefono")) u.setTelefono(requestMap.get("telefono"));
            if (requestMap.containsKey("password")) u.setContrasena(passwordEncoder.encode(requestMap.get("password")));
            if (requestMap.containsKey("dni")) u.setDni(requestMap.get("dni"));

            usuariodao.save(u);
            return new ResponseEntity<>("Usuario actualizado correctamente", HttpStatus.OK);

        } catch (NumberFormatException e) {
            return new ResponseEntity<>("ID del usuario inválido", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al actualizar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> eliminarUsuario(int idUsuario) {
        Usuario u = usuariodao.findById(idUsuario).orElse(null);
        if (u != null) {
            usuariodao.delete(u);
            return new ResponseEntity<>("Usuario eliminado correctamente", HttpStatus.OK);
        }
        return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
    }
}
