package com.example.demo.controller;

import com.example.demo.entity.Reserva;
import com.example.demo.service.reservaimpl;
import com.example.demo.dao.UsuarioRepository;
import com.example.demo.dao.reservarepository;
import com.example.demo.entity.Usuario;
import com.example.demo.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@CrossOrigin(origins = "https://restaurantevillahermosa.netlify.app/")
@RequestMapping("/reserva")
public class reservaController {

    @Autowired
    private reservaimpl reservadao;

    @Autowired
    private reservarepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @PersistenceContext
    private EntityManager em;

    // --- EXISTENTES ---

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> reMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(email);
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }
        int nromesaint = Integer.parseInt(reMap.get("mesa").toString());
        LocalDate fecha = LocalDate.parse(String.valueOf(reMap.get("dia")));
        LocalTime horaInicio = LocalTime.parse(String.valueOf(reMap.get("hora_inicio")));
        LocalTime horaFin = LocalTime.parse(String.valueOf(reMap.get("hora_fin")));
        int cantidad = Integer.parseInt(reMap.get("cantidad").toString());
        LocalDateTime fechaHoraInicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fechaHoraFin    = LocalDateTime.of(fecha, horaFin);

        return reservadao.crear(
            fechaHoraInicio,
            fechaHoraFin,
            nromesaint,
            String.valueOf(reMap.get("name")),
            String.valueOf(reMap.get("lastname")),
            String.valueOf(reMap.get("correo")),
            String.valueOf(reMap.get("numero")),
            usuario.getId(),
            cantidad
        );
    }

    @PostMapping("/eliminar")
    public ResponseEntity<String> eliminar(@RequestBody Map<String, String> m) {
        return reservadao.eliminar(Integer.parseInt(m.get("id_reserva")));
    }

    @PostMapping("/actualizarEstado")
    public ResponseEntity<String> actualizarEstado(@RequestBody Map<String, Integer> request) {
        return reservadao.actualizarEstado(request);
    }

    @GetMapping("/crearreporte")
    public void crearPdf(HttpServletResponse resp) throws Exception {
        byte[] pdf = reservadao.crearReportePdf(resp).toByteArray();
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=reservas.pdf");
        resp.getOutputStream().write(pdf);
    }

    @GetMapping("/excelreservas")
    public void crearExcel(HttpServletResponse resp) throws IOException {
        byte[] xlsx = reservadao.crearReporteExcel(resp).toByteArray();
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=reservas.xlsx");
        resp.getOutputStream().write(xlsx);
    }

    @PostMapping("/disponibilidad")
    public ResponseEntity<Integer> disponibilidad(@RequestBody Map<String, String> body) {
        LocalDate date = LocalDate.parse(body.get("dia"));
        LocalDateTime inicio = LocalDateTime.of(date, LocalTime.parse(body.get("hora_inicio")));
        LocalDateTime fin    = LocalDateTime.of(date, LocalTime.parse(body.get("hora_fin")));
        int total = reservaRepository.contarPersonasEnRango(inicio, fin);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/misreservas")
    public ResponseEntity<?> misReservas(@RequestHeader("Authorization") String authHeader) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByCorreo(email);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            }
            var reservas = reservaRepository.findReservabyusuario(usuario.getId());
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar reservas: " + e.getMessage());
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<?> listarReservasAdmin(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(reservaRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar reservas admin: " + e.getMessage());
        }
    }

    @PostMapping("/cancelar")
    public ResponseEntity<String> cancelarReserva(@RequestBody Map<String, String> body) {
        return reservadao.cancelar(
            Integer.valueOf(body.get("idReserva")),
            body.get("motivo")
        );
    }

    @PostMapping("/comentar")
    public ResponseEntity<String> comentarReserva(@RequestBody Map<String, String> body) {
        return reservadao.comentar(
            Integer.parseInt(body.get("idReserva")),
            body.get("comentario")
        );
    }

    // --- NUEVOS: sustituyen a ReservaQuickController ---

    @PostMapping("/diaria")
    public ResponseEntity<List<Reserva>> reservasDelDia(@RequestBody Map<String, String> body) {
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        TypedQuery<Reserva> q = em.createNamedQuery("Reserva.findbyestado", Reserva.class);
        q.setParameter("fecha", fecha);
        return ResponseEntity.ok(q.getResultList());
    }

    @GetMapping("/atendidas")
    public ResponseEntity<List<Reserva>> reservAsAtendidas() {
        TypedQuery<Reserva> q = em.createNamedQuery("Reserva.atendidos", Reserva.class);
        return ResponseEntity.ok(q.getResultList());
    }

    @PostMapping("/ocupadas")
    public ResponseEntity<List<String>> horasOcupadas(@RequestBody Map<String, String> body) {
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.atTime(23, 59);

        TypedQuery<Reserva> q = em.createQuery(
            "SELECT r FROM Reserva r WHERE r.fechaHora BETWEEN :inicio AND :fin", Reserva.class
        );
        q.setParameter("inicio", inicio);
        q.setParameter("fin", fin);
        List<Reserva> rs = q.getResultList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        List<String> slots = rs.stream()
            .flatMap(r -> {
                LocalDateTime s = r.getFechaHora();
                LocalDateTime e = r.getFechaHoraFin();
                List<String> ls = new ArrayList<>();
                for (LocalDateTime t = s; !t.isAfter(e.minusMinutes(1)); t = t.plusMinutes(15)) {
                    ls.add(t.toLocalTime().format(fmt));
                }
                return ls.stream();
            })
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        return ResponseEntity.ok(slots);
    }
}
