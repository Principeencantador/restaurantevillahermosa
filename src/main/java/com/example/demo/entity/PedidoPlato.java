package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

@Entity
@Table(name = "pedido_plato")
@NamedQuery(
    name  = "PedidoPlato.eliminarPedidoByPlatoid",
    query = "DELETE FROM PedidoPlato p WHERE p.plato.id_plato = :id_plato"
)
@NamedQuery(
    name  = "PedidoPlato.encontrarpedidosbyplatoid",
    query = "SELECT p FROM PedidoPlato p WHERE p.plato.id_plato = :id_plato"
)
@NamedQuery(
    name  = "PedidoPlato.encontrarpedidoplatoporplatoyid",
    query = "SELECT pp FROM PedidoPlato pp WHERE pp.plato.id_plato = :id_plato AND pp.idPedidoPlato = :id_pedidoplato"
)
@NamedQuery(
    name  = "PedidoPlato.findByPedidoId",
    query = "SELECT pp FROM PedidoPlato pp WHERE pp.pedido.id_pedido = :id_plato"
)
@DynamicInsert
@DynamicUpdate
public class PedidoPlato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido_plato")
    private int idPedidoPlato;

    @ManyToOne
    @JoinColumn(
        name = "id_pedido",
        referencedColumnName = "id_pedido",
        foreignKey = @ForeignKey(name = "pedido_id")
    )
    @JsonBackReference  // rompe el ciclo de serialización hacia Pedido
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(
        name = "id_plato",
        referencedColumnName = "id_plato",
        foreignKey = @ForeignKey(name = "plato_id")
    )
    private Plato plato;
@Column(name = "motivo_cancelacion", length = 255)
private String motivoCancelacion;

@Column(name = "estado", nullable = false, length = 20)
private String estado = "PENDIENTE"; // El "DEFAULT" se establece aquí en Java

    @Column(name = "cantidad")
    private int cantidad;

    // Getters y setters

    public int getIdPedidoPlato() {
        return idPedidoPlato;
    }

    public void setIdPedidoPlato(int idPedidoPlato) {
        this.idPedidoPlato = idPedidoPlato;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
