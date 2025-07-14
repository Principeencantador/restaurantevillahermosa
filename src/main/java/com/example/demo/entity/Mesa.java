package com.example.demo.entity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@NamedQuery(name = "Mesa.mesalibre", query = "SELECT m FROM Mesa m WHERE m.capacidadDeMesa >= :capacidad AND m.idMesa NOT IN (SELECT r.mesa.idMesa FROM Reserva r WHERE r.fecha = :fecha)")
@NamedQuery(name = "Mesa.mesanro", query = "SELECT m FROM Mesa m Where m.nroMesa=:nro_mesa")
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    private int idMesa;

    @NotNull
    @Column(name = "numero_mesa")
    private int nroMesa;

    @NotNull
    @Column(name = "capacidad")
    private int capacidadDeMesa;

}
