
package com.example.demo.service;

import com.example.demo.dao.LoginResponse;
import com.example.demo.entity.Usuario;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface UsuarioService {
    public ResponseEntity<String> actualizar(Map<String, String> requesmap);

    public List<Usuario> traerusuarios();

    public String signup(Map<String, String> requesmap);

    ResponseEntity<LoginResponse> login(Map<String, String> requestMap, HttpSession session, HttpServletResponse response);

    public ResponseEntity<String> eliminarUsuario(int idUsuario);
}
