package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PedidoPlato;

@Repository
public interface pedido_platorepository extends JpaRepository<PedidoPlato, Integer> {

    @Query(name = "PedidoPlato.eliminarPedidoByPlatoid")
    public void eliminarPedidoByPlatoid(@Param("id_plato") int id_plato);

    public List<PedidoPlato> encontrarpedidosbyplatoid(@Param("id_plato") int id_plato);

    public PedidoPlato encontrarpedidoplatoporplatoyid(@Param("id_plato") int id_plato,
            @Param("id_pedidoplato") int id_pedidoplato);

    public List<PedidoPlato> findByPedidoId(@Param("id_plato") int pedidoId);

}
