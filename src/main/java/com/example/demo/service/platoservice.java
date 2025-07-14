package com.example.demo.service;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Plato;

public interface platoservice {

    ArrayList<Plato> traerplatos();

    ResponseEntity<String> guardar(
        MultipartFile imagen,
        String nombre,
        String descripcion,
        String precio,
        Boolean stock
    );

    ResponseEntity<String> actualizar(
        String id,
        String nombre,
        String descripcion,
        String precio,
        MultipartFile imagen,
        Boolean stock
    ) throws IOException;

    ResponseEntity<String> actualizar(
        String id,
        String nombre,
        String descripcion,
        String precio,
        MultipartFile imagen,
        Boolean stock,
        HttpSession session
    ) throws IOException;

    ResponseEntity<String> eliminar(String id);

    ResponseEntity<String> actualizarStock(int id, boolean stock);
}
