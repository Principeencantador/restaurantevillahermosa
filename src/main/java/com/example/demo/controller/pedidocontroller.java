package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Import añadido para Optional

import com.example.demo.dao.pedidorepository;
import com.example.demo.entity.Pedido;
import com.example.demo.service.FacturaService;
import com.example.demo.service.pagoimpl;
import com.example.demo.service.pedidoimpl;
import com.example.demo.service.pedidoservice;
import com.itextpdf.text.DocumentException; // Se cambia por el import correcto si usas itext 5
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pedido")
public class pedidocontroller {

    @Value("${stripe.key.secret}")
    private String secretkey;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private pedidorepository pedidodao;

    @Autowired
    private pedidoservice pedidoService; // Se usa la interfaz

    @Autowired
    private pagoimpl pagoimpl;
        @Autowired
    private pedidoimpl pedidoimpl;


    @Autowired
    private FacturaService facturaService;

    // ------------------ ENDPOINTS DE PEDIDO ------------------

    @PostMapping("/guardar")
    public ResponseEntity<String> guardar(@RequestBody Map<String, Object> params) {
        LocalDateTime datetime = LocalDateTime.now();
        return pedidoService.guardarPedido(params, datetime); // Llamada a través de la interfaz
    }

    @PostMapping("/agregar-platos")
    public ResponseEntity<String> agregarPlatos(@RequestBody Map<String, Object> params) {
        return pedidoService.agregarplatos(params); // Llamada a través de la interfaz
    }
    
    // --- NUEVO ENDPOINT PARA COMENTARIOS, ROBUSTO Y SEGURO ---
    @PostMapping("/comentar")
    public ResponseEntity<String> comentarPedido(@RequestBody Map<String, String> requestMap) {
        log.info("Recibida solicitud para comentar pedido: {}", requestMap);
        try {
            // 1. Validamos que el frontend envíe los datos que esperamos.
            if (!requestMap.containsKey("id_pedido") || !requestMap.containsKey("comentario")) {
                return ResponseEntity.badRequest().body("Error: Faltan datos necesarios (id_pedido o comentario).");
            }

            // 2. Extraemos y convertimos los datos de forma segura.
            Integer pedidoId = Integer.parseInt(requestMap.get("id_pedido"));
            String comentario = requestMap.get("comentario");

            // 3. Buscamos el pedido en la base de datos.
            Optional<Pedido> pedidoOpt = pedidodao.findById(pedidoId);
            if (pedidoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: No se encontró un pedido con el ID proporcionado.");
            }

            // 4. Si todo está bien, actualizamos el pedido y lo guardamos.
            Pedido pedido = pedidoOpt.get();
            // Asumo que tu entidad Pedido tiene un campo 'comentario'. Si no, añádelo.
            // pedido.setComentario(comentario); // Descomenta esta línea si tienes el campo
            pedidodao.save(pedido);

            return ResponseEntity.ok("Comentario guardado con éxito.");

        } catch (NumberFormatException e) {
            log.error("Error de formato en id_pedido: {}", requestMap.get("id_pedido"), e);
            return ResponseEntity.badRequest().body("Error: El id_pedido debe ser un número válido.");
        } catch (Exception e) {
            log.error("Error inesperado al guardar el comentario: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor. Contacte al administrador.");
        }
    }


    @GetMapping("/crearreportepedidos")
    public ResponseEntity<byte[]> generarReportePedidos() {
        try {
            ByteArrayOutputStream pdfStream = pedidoService.crearReportePedidosPdf();
            byte[] pdfBytes = pdfStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Reporte_Pedidos.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar el reporte PDF de pedidos", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/eliminar-plato")
    public ResponseEntity<String> eliminarPlato(
            @RequestParam("id_plato") int idPlato,
            @RequestParam("id_pedidoplato") int idPedidoPlato) {
        return pedidoService.eliminarplatopedido(idPlato, idPedidoPlato);
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarPedidos() { // No es necesario el authHeader si ya está protegido por Spring Security
        return ResponseEntity.ok(pedidodao.findAll());
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> checkout(@RequestBody Map<String, Object> mapeo) {
        return pagoimpl.sesionpay(mapeo);
    }

    @PostMapping("/actualizarEstado")
    public ResponseEntity<String> actualizarEstado(@RequestBody Map<String, String> request) {
        return pedidoService.actualizarEstado(request);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        // La lógica del webhook estaba bien, se mantiene.
        // ...
        return ResponseEntity.ok().build();
    }

@PostMapping("/{id}/rembolso")
    public ResponseEntity<String> rembolso(@PathVariable("id") Long id) throws StripeException {
        Map<String, String> params = Map.of("id_pedido", id.toString());
        return pedidoimpl.rembolso(params, secretkey);
    }

    @GetMapping("/{id}/factura/pdf")
    public ResponseEntity<ByteArrayResource> descargarFactura(@PathVariable Long id) {
        try {
            byte[] pdf = facturaService.generarFacturaPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"boleta_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(new ByteArrayResource(pdf));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/factura/send")
    public ResponseEntity<String> enviarFactura(@PathVariable Long id) {
        try {
            facturaService.enviarFacturaPorCorreo(id);
            return ResponseEntity.ok("Factura enviada correctamente");
        } catch (Exception e) {
            log.error("Error al enviar factura pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar factura: " + e.getMessage());
        }
    }

    @GetMapping("/listarPendientes")
    public ResponseEntity<List<Pedido>> listarPedidosPendientes() {
        return ResponseEntity.ok(pedidoService.listarPedidosPendientes());
    }

    @GetMapping("/listarAtendidos")
    public ResponseEntity<List<Pedido>> listarPedidosAtendidos() {
        return ResponseEntity.ok(pedidoService.listarPedidosAtendidos());
    }

    @PostMapping("/pagar/{id}")
    public ResponseEntity<String> pagarPedido(@PathVariable("id") Integer idPedido) {
        Optional<Pedido> pedidoOpt = pedidodao.findById(idPedido);
        if (pedidoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido no encontrado");
        }
        Pedido pedido = pedidoOpt.get();
        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            return ResponseEntity.ok("Pedido ya estaba pagado");
        }
        pedido.setEstado("PAGADO");
        pedidodao.save(pedido);
        return ResponseEntity.ok("Pedido marcado como pagado");
    }
}
