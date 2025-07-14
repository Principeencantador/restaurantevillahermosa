package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.rol_usuario;

@Repository
public interface rol_usuariorepository extends JpaRepository<rol_usuario, Integer> {

}
