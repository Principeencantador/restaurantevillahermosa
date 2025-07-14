package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Mesa;

@Service
public interface mesaservice {

    public List<Mesa> mostrarmesalibre(int capacidad, LocalDateTime fecha);

    public ResponseEntity<String> crearmesa(int capacidad, int numero_mesas);

    public ResponseEntity<String> eliminar(String id);

    public ResponseEntity<String> actualizar(int id, int capacidad, int numero_mesa);
 
    List<Mesa> traerMesas();
}
