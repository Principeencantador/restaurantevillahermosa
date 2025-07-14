package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.platorepository;
import com.example.demo.entity.Plato;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class platoimpl implements platoservice {

    private final platorepository platodao;
    private final Path uploadDir;

    public platoimpl(
        platorepository platodao,
        @Value("${file.upload-dir:uploads}") String uploadDir
    ) {
        this.platodao = platodao;
        // carpeta "uploads" a nivel de raíz de la app, con fallback "uploads"
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Directorio de subida listo: {}", uploadDir);
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de subida: {}", uploadDir, e);
        }
    }

    @Override
    public ArrayList<Plato> traerplatos() {
        return new ArrayList<>(platodao.findAll());
    }

    @Override
    public ResponseEntity<String> guardar(
            MultipartFile imagen,
            String nombre,
            String descripcion,
            String precio,
            Boolean stock
    ) {
        if (imagen == null || imagen.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body("No se ha seleccionado imagen");
        }

        String ct = imagen.getContentType();
        if (!"image/jpeg".equals(ct) && !"image/png".equals(ct)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Formato de imagen no válido (JPG o PNG)");
        }

        try {
            String original = imagen.getOriginalFilename();
            String fileName = original == null
                ? Long.toString(System.currentTimeMillis())
                : original.trim().replaceAll("\\s+", "_");
            Path target = uploadDir.resolve(fileName);

            log.info("Guardando imagen en {}", target);
            Files.copy(imagen.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Plato p = new Plato();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(Double.parseDouble(precio));
            p.setStock(stock);
            p.setFoto(fileName);
            platodao.save(p);

            return ResponseEntity.ok("Se ha guardado correctamente el plato");
        } catch (IOException e) {
            log.error("Error al guardar imagen", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al guardar la imagen: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> actualizar(
            String id,
            String nombre,
            String descripcion,
            String precio,
            MultipartFile imagen,
            Boolean stock
    ) throws IOException {
        Plato p = platodao.findById(Integer.parseInt(id));
        if (p == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Plato no encontrado");
        }

        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(Double.parseDouble(precio));
        p.setStock(stock);

        if (imagen != null && !imagen.isEmpty()) {
            String original = imagen.getOriginalFilename();
            String fileName = original == null
                ? Long.toString(System.currentTimeMillis())
                : original.trim().replaceAll("\\s+", "_");
            Path target = uploadDir.resolve(fileName);

            log.info("Actualizando imagen en {}", target);
            Files.copy(imagen.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            p.setFoto(fileName);
        }

        platodao.save(p);
        return ResponseEntity.ok("Plato actualizado correctamente");
    }

    @Override
    public ResponseEntity<String> actualizar(
            String id,
            String nombre,
            String descripcion,
            String precio,
            MultipartFile imagen,
            Boolean stock,
            HttpSession session
    ) throws IOException {
        return actualizar(id, nombre, descripcion, precio, imagen, stock);
    }

    @Override
    public ResponseEntity<String> eliminar(String id) {
        Plato p = platodao.findById(Integer.parseInt(id));
        if (p == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Plato no encontrado");
        }
        platodao.delete(p);
        return ResponseEntity.ok("Plato eliminado correctamente");
    }

    @Override
    public ResponseEntity<String> actualizarStock(int id, boolean stock) {
        Plato p = platodao.findById(id);
        if (p == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Plato no encontrado");
        }
        p.setStock(stock);
        platodao.save(p);
        return ResponseEntity.ok("Stock actualizado");
    }
}
