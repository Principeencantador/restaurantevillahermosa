package com.example.demo.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Collection;
import java.util.Collections;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NamedQuery(name = "Usuario.findByNombreUsuario", query = "SELECT u FROM Usuario u WHERE u.nombre = :nombre")
@NamedQuery(name = "Usuario.findByCorreo", query = "SELECT u FROM Usuario u WHERE u.email = :email")
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "Usuario")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_usuario") // Make sure this matches the column name in your database
    private int id_usuario;

    @Column(name = "email")
    private String email;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "contrasena")
    private String contrasena;

    
    @Column(name = "telefono")
    private String telefono;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", referencedColumnName = "id_rol")
    private Rol rol;

    @Column(name = "status")
    private String status;

    @Column(name = "verification_code")
    private String verificationCode;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertir el rol a SimpleGrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority(rol.getNombre()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }


    public int getId() {
        return id_usuario;
    }

    @Override
    public String getUsername() {
        return email;

    }
      @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    // getter + setter
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

}
