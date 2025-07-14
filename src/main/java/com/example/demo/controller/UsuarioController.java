
package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.LoginResponse;
import com.example.demo.dao.UsuarioRepository;
import com.example.demo.entity.Usuario;
import com.example.demo.security.jwt.jwtUtil;
import com.example.demo.service.Usuarioimpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/empleado")
public class UsuarioController {

    @Autowired
    private jwtUtil jwtUtil;
    @Autowired
    private Usuarioimpl usuarioimpl;

    @Autowired
    private UsuarioRepository usuariodao;


@PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(
            @RequestBody Map<String, String> requestMap   // ← se llama requestMap
    ) {
        try {
            // requestMap.get("dni") ya estará disponible si llega en el JSON
            String resultado = usuarioimpl.signup(requestMap);
            if ("El usuario ya existe".equals(resultado)) {
                return new ResponseEntity<>(resultado, HttpStatus.CONFLICT);
            } else if ("Se ha registrado correctamente".equals(resultado)) {
                return new ResponseEntity<>(resultado, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No se ha podido registrar",
                                          HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error al registrar el usuario",
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<String> verificarCodigo(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String codigo = requestBody.get("code");

        Usuario usuario = usuariodao.findByCorreo(email);
        if (usuario != null && usuario.getVerificationCode().equals(codigo)) {
            if (usuario.getStatus().equals("false")) {
                usuario.setStatus("true");
                usuariodao.save(usuario);
                return new ResponseEntity<>("Email verificado con éxito", HttpStatus.OK);
            }
            return new ResponseEntity<>("El email ya ha sido verificado", HttpStatus.OK);
        }
        return new ResponseEntity<>("Código inválido o usuario no encontrado", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody Map<String, String> requesmap, HttpSession sesion,
            HttpServletResponse response) {
    try {
        return usuarioimpl.login(requesmap, sesion, response);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


    @PostMapping("/verificarRol")
    public ResponseEntity<String> verificarRol(HttpSession session, HttpServletResponse response) {
        // Añadir el token al encabezado de la respuesta

        response.setHeader("Authorization", "Bearer " + (String) session.getAttribute("token"));

        if (jwtUtil.extractRol((String) session.getAttribute("token")).equals("ROLE_admin")) {
            return new ResponseEntity<>("{\"mensaje\": \"Eres un admin\"}", HttpStatus.OK);
        } else if (jwtUtil.extractRol((String) session.getAttribute("token")).equals("ROLE_user")) {
            return new ResponseEntity<>("{\"mensaje\": \"Eres un usuario\"}", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("{\"mensaje\": \"Rol no reconocido\"}", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/actualizar")
    public ResponseEntity<String> Actualizar(@RequestBody Map<String, String> requesMap) {

        return usuarioimpl.actualizar(requesMap);
    }

    @DeleteMapping("/eliminar/{idUsuario}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable("idUsuario") int idUsuario) {
        return usuarioimpl.eliminarUsuario(idUsuario);
    }
public ResponseEntity<List<Usuario>> listarEmpleados() {
    try {
        List<Usuario> usuarios = usuariodao.findAll();
        return ResponseEntity.ok(usuarios);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
@GetMapping("/listar")
public ResponseEntity<?> listarEmpleados(@RequestHeader("Authorization") String authHeader) {
    try {
        // Retorna todos los usuarios
        return ResponseEntity.ok(usuariodao.findAll());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error al listar usuarios: " + e.getMessage());
    }
}
}
