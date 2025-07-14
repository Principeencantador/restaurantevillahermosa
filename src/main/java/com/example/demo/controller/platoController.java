package com.example.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Plato;
import com.example.demo.service.platoservice;

@CrossOrigin(origins = "https://restaurantevillahermosa.netlify.app/" )
@RestController
@RequestMapping("/plato")
public class platoController {

    @Autowired
    private platoservice platoService;

    // GET público: lista de platos
    @GetMapping("/listar")
    public ResponseEntity<List<Plato>> listarPlatos() {
        try {
            List<Plato> platos = platoService.traerplatos();
            return ResponseEntity.ok(platos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // LEGADO: si necesitas este método adicional (puedes eliminar si ya no lo usas)
    @PostMapping("/traerplatos")
    public List<Plato> traerPlatos() {
        return platoService.traerplatos();
    }

    // GUARDAR PLATO
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarPlato(
        @RequestParam String nombre,
        @RequestParam String descripcion,
        @RequestParam Double precio,
        @RequestParam("foto") MultipartFile foto,
        @RequestParam Boolean stock
    ) {
        try {
            return platoService.guardar(
                foto,
                nombre,
                descripcion,
                String.valueOf(precio),
                stock
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al guardar la imagen: " + e.getMessage());
        }
    }

    // ACTUALIZAR PLATO
    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizarPlato(
        @RequestParam("id_plato") String id,
        @RequestParam("actuNombre") String nombre,
        @RequestParam("actuDescripcion") String descripcion,
        @RequestParam("actuprecio") String precio,
        @RequestParam("actufoto") MultipartFile imagen,
        @RequestParam("actustock") Boolean stock
    ) throws IOException {
        return platoService.actualizar(id, nombre, descripcion, precio, imagen, stock);
    }

    // ELIMINAR PLATO
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminar(@RequestParam("id_plato") String id) {
        return platoService.eliminar(id);
    }

    // ACTUALIZAR STOCK
    @PutMapping("/stock/{id}")
    public ResponseEntity<String> actualizarStock(
        @PathVariable("id") int id, 
        @RequestBody Map<String, Boolean> body
    ) {
        return platoService.actualizarStock(id, body.get("stock"));
    }
}
