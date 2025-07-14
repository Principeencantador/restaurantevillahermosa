package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Pedido;
import com.itextpdf.text.DocumentException;
import com.stripe.exception.StripeException;

@Service
public interface pedidoservice {

    public ResponseEntity<String> guardarPedido(Map<String, Object> params, LocalDateTime dateTime);

    public List<Pedido> traerPedidobycorreo(String id);

    public ResponseEntity<String> agregarplatos(Map<String, Object> params);

    public ResponseEntity<String> eliminarplatopedido(int id_plato, int id_pedidoplato);

    public ResponseEntity<String> actualizarEstado(Map<String, String> request);

    public ResponseEntity rembolso(Map<String, String> requesmap, String secretkey) throws StripeException;

        List<Pedido> listarPedidosPendientes();

    List<Pedido> listarPedidosAtendidos();
        ByteArrayOutputStream crearReportePedidosPdf() throws DocumentException, IOException;


}
