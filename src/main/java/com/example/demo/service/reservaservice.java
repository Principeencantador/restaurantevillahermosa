// src/main/java/com/example/demo/service/reservaservice.java
package com.example.demo.service;

import com.example.demo.entity.Reserva;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface reservaservice {
    ResponseEntity<String> crear(
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        int nroMesa,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        int idUsuario,
        int cantidad
    );

    List<Reserva> traerreserva(LocalDate fecha);

    List<Reserva> atendido();

    void actualizarEstadoReserva();

    ResponseEntity<String> eliminar(int idreserva);

    ResponseEntity<String> actualizarEstado(Map<String,Integer> request);

    ByteArrayOutputStream crearReportePdf(HttpServletResponse response)
        throws DocumentException, IOException;

    ByteArrayOutputStream crearReporteExcel(HttpServletResponse response)
        throws IOException;

    ResponseEntity<String> cancelar(int idReserva, String motivo);

    ResponseEntity<String> comentar(int idReserva, String comentario);
}
