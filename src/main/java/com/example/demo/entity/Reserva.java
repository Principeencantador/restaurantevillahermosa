package com.example.demo.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@DynamicUpdate
@NamedQuery(name = "Reserva.findReservabyusuario", query = "SELECT r from Reserva r where r.estado='pendiente' AND  r.usuario.id=:id_usuario")
@NamedQuery(name = "Reserva.findbyestado", query = "SELECT r FROM Reserva r WHERE (r.estado='pendiente' OR r.estado='confirmado') AND FUNCTION('DATE', r.fecha) = :fecha ORDER BY r.fecha")
@NamedQuery(name = "Reserva.atendidos", query = "SELECT r FROM Reserva r WHERE r.estado = 'atendido' ORDER BY DATE(r.fecha), TIME(r.fecha)")
@Table(name = "reserva")
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private int idReserva;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_mesa", referencedColumnName = "id_mesa")
    private Mesa mesa;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "correo")
    private String correo;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "hora")
    private LocalTime hora;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;
     @Column(name = "motivo_cancelacion", length = 255, nullable = true)
  private String motivoCancelacion;
    
@Column(name = "cantidad")
private int cantidad;

@Column(name = "fecha_hora_fin")
private LocalDateTime fechaHoraFin;
@Column(name = "comentario", nullable = true)
private String comentario;

public String getMotivoCancelacion() { return motivoCancelacion; }
  public void   setMotivoCancelacion(String m) { this.motivoCancelacion = m; }
}
