package com.example.demo.entity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ForeignKey;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "rol_usuario")
@Data
public class rol_usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_rolusuario")
    private int id_rolusuario;

    @ManyToOne
    @JoinColumn(name = "rol_id", referencedColumnName = "id_rol", foreignKey = @ForeignKey(name = "id_rol"))
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id_usuario", foreignKey = @ForeignKey(name = "id_usuario"))
    private Usuario usuario;
}
