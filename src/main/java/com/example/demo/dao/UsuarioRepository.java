package com.example.demo.dao;

import com.example.demo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Método correcto siguiendo la convención de nombres de Spring Data JPA
    Usuario findByNombreUsuario(@Param("nombre") String nombre);

    Usuario findByCorreo(@Param("email") String email);
}
