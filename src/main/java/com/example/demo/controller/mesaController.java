package com.example.demo.controller;

import com.example.demo.entity.Mesa;
import com.example.demo.service.mesaservice;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/mesa")
public class mesaController {

    @Autowired
    private mesaservice mesaService;  // Inyecta solo la interfaz

@PostMapping("/traer")
@ResponseBody
public String obtenerMesaLibre(@RequestBody Map<String, String> request) {
    try {
        // VALIDAR que todos los campos existan
        String capacidadStr = request.get("capacidad");
        String fechaStr = request.get("fecha");
        String horaInicioStr = request.get("hora_inicio");
        String horaFinStr = request.get("hora_fin");

        if (capacidadStr == null || fechaStr == null || horaInicioStr == null || horaFinStr == null) {
            return "<div class='alert alert-danger'>Faltan datos obligatorios para buscar mesas.</div>";
        }

        int capacidad = Integer.parseInt(capacidadStr);
        LocalDate fecha = LocalDate.parse(fechaStr);

        // PARSEAR horas de inicio y fin
        LocalTime horaInicio, horaFin;
        try {
            horaInicio = LocalTime.parse(horaInicioStr);
            horaFin = LocalTime.parse(horaFinStr);
        } catch (Exception e) {
            return "<div class='alert alert-danger'>Formato de hora inválido. Usa formato HH:mm (ej. 12:00)</div>";
        }

        // Puedes ajustar aquí según la lógica de tu sistema: 
        // ¿buscar solo por horaInicio o por el rango de horas?
        // Por ahora, mostramos mesas disponibles para la hora de inicio
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaInicio);

        List<Mesa> mesaLibre = mesaService.mostrarmesalibre(capacidad, fechaHora);

        StringBuilder responseHtml = new StringBuilder();

        if (!mesaLibre.isEmpty()) {
            responseHtml.append("<h3>Mesas disponibles:</h3>");
            responseHtml.append("<div class='d-flex justify-content-center flex-wrap'>");
            for (Mesa mesa : mesaLibre) {
                responseHtml.append("<div class='mt-3 mb-3'>");
                responseHtml.append("<div class='circulo'>");
                responseHtml.append("<input type='radio' name='mesa' value='").append(mesa.getNroMesa())
                        .append("' id='mesa").append(mesa.getNroMesa()).append("'>");
                responseHtml.append("<label for='mesa").append(mesa.getNroMesa()).append("'>")
                        .append(mesa.getNroMesa()).append("</label>");
                responseHtml.append("</div>");
                responseHtml.append("</div>");
            }
            responseHtml.append("</div>");
        } else {
            responseHtml.append("<p>No hay mesas disponibles para esta cantidad de personas en ese horario.</p>");
        }

        return responseHtml.toString();

    } catch (Exception e) {
        return "<div class='alert alert-danger'>Error interno: " + e.getMessage() + "</div>";
    }
}


     @GetMapping("/listar")
    public ResponseEntity<List<Mesa>> listarMesas() {
        List<Mesa> mesas = mesaService.traerMesas();  // Método correctamente invocado del servicio
        return ResponseEntity.ok(mesas);
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearmesa(@RequestParam("capacidad") int capacidad,
                                            @RequestParam("numero") int nroMesa) {
        return mesaService.crearmesa(capacidad, nroMesa);
    }

    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizar(@RequestParam("idmesa") int id,
                                            @RequestParam("actucapacidad") int capacidad,
                                            @RequestParam("actunumero") int nroMesa) {
        return mesaService.actualizar(id, capacidad, nroMesa);
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminar(@RequestParam("idmesa") String id) {
        return mesaService.eliminar(id);
    }
}
