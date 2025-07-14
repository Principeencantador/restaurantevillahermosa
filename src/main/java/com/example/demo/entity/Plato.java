package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "plato")
public class Plato {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id_plato")
private Integer id_plato;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio")
    private double precio;

    @Column(name = "foto", nullable = true)
    private String foto;

        @Column(name = "stock")
private boolean stock = true;

@Column(name = "estado", length = 255)
private String estado = "pendiente"; // El "DEFAULT" se establece aquí en Java


}
