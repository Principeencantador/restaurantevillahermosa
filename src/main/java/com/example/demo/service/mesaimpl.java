package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.demo.dao.mesarepository;
import com.example.demo.entity.Mesa;
import jakarta.validation.constraints.NotNull;

@Service
public class mesaimpl implements mesaservice {
    @Autowired
    private mesarepository repository;

    @Override
    public List<Mesa> mostrarmesalibre(int Capacidad, LocalDateTime fecha) {
        return repository.mesalibre(Capacidad, fecha);
    }

    @Override
    public List<Mesa> traerMesas() {
        return repository.findAll();
    }
 

    @Override
    public ResponseEntity<String> crearmesa(@NotNull int capacidad, @NotNull int numero_mesa) {
        Mesa mesa = new Mesa();
        mesa.setCapacidadDeMesa(capacidad);
        mesa.setNroMesa(numero_mesa);
        List<Mesa> mesas = repository.findAll();
        for (Mesa m : mesas) {
            if (m.getNroMesa() == numero_mesa) {
                return new ResponseEntity<String>("El numero de mesa ya existe", HttpStatus.BAD_REQUEST);
            }
        }
        if (capacidad == 0 || Objects.isNull(capacidad) || Objects.isNull(numero_mesa)) {
            return new ResponseEntity<String>("La capcidad no puede ser 0 o los datos no pueden estar vacias",
                    HttpStatus.BAD_REQUEST);
        }
        repository.save(mesa);
        return new ResponseEntity<String>("Se ha guardado correctamente", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> eliminar(String id) {
        Mesa mesa = repository.findById(Integer.parseInt(id)).orElse(null);
        if (Objects.isNull(mesa)) {
            return new ResponseEntity<>("Mesa no encontrada", HttpStatus.NOT_FOUND);
        }
        repository.delete(mesa);
        return new ResponseEntity<>("Se ha eliminado correctamente", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> actualizar(int id, int capacidad, int numero_mesa) {

        Mesa mesa = repository.findById(id).orElse(null);
        if (Objects.isNull(mesa)) {
            return new ResponseEntity<>("Mesa no encontrada", HttpStatus.NOT_FOUND);
        }
        mesa.setCapacidadDeMesa(capacidad);
        mesa.setNroMesa(numero_mesa);
        repository.save(mesa);
        return new ResponseEntity<>("Se ha actualizado correctamente", HttpStatus.OK);

    }

}
