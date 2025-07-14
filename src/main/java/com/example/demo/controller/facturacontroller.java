package com.example.demo.controller;

import com.example.demo.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/factura")
public class facturacontroller {

    @Autowired
    private FacturaService facturaService;

    /**
     * Endpoint de prueba: genera y retorna la factura en PDF del pedido {id}.
     * GET /factura/prueba/{id}
     */
    @GetMapping("/prueba/{id}")
    public ResponseEntity<ByteArrayResource> pruebaFactura(@PathVariable Long id) {
        try {
            byte[] pdf = facturaService.generarFacturaPdf(id);
            ByteArrayResource resource = new ByteArrayResource(pdf);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Envía la factura por correo al cliente del pedido {id}.
     * POST /factura/send/{id}
     */
    @PostMapping("/send/{id}")
    public ResponseEntity<String> enviarFactura(@PathVariable Long id) {
        try {
            facturaService.enviarFacturaPorCorreo(id);
            return ResponseEntity.ok("Factura enviada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar factura: " + e.getMessage());
        }
    }
}
