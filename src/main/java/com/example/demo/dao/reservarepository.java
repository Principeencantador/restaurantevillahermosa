package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.entity.Reserva;

@Repository
public interface reservarepository extends JpaRepository<Reserva, Integer> {

  @Query("SELECT r FROM Reserva r WHERE r.usuario.id = :idUsuario")
List<Reserva> findReservabyusuario(@Param("idUsuario") int idUsuario);


    List<Reserva> findbyestado(@Param("fecha") LocalDate fecha);

    List<Reserva> atendidos();

 @Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM Reserva r WHERE r.fecha = :fechaHora AND (r.estado = 'pendiente' OR r.estado = 'confirmado')")
int contarPersonasPorHora(@Param("fechaHora") LocalDateTime fechaHora);
// En tu repository deberías tener un método que reciba dos LocalDateTime

@Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM Reserva r WHERE r.fechaHora >= :fechaHoraInicio AND r.fechaHora < :fechaHoraFin AND r.estado IN ('pendiente', 'confirmado')")
int contarPersonasEnRango(@Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
                          @Param("fechaHoraFin") LocalDateTime fechaHoraFin);

                           List<Reserva> findByEstadoInAndFechaHoraFinBefore(List<String> estados, LocalDateTime fechaHoraFin);
}
