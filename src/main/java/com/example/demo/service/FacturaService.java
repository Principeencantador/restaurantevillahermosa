package com.example.demo.service;

import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import java.io.IOException;

/**
 * Servicio para generación y envío de facturas en PDF.
 * Define el contrato para las implementaciones de la factura.
 */
public interface FacturaService {

    /**
     * Genera la representación en bytes de un PDF de factura para un pedido específico.
     * Puede lanzar excepciones relacionadas con la creación del documento (DocumentException),
     * operaciones de entrada/salida (IOException), o la generación del código QR (WriterException).
     *
     * @param pedidoId ID del pedido para el cual generar la factura.
     * @return Un arreglo de bytes que representa el archivo PDF.
     * @throws DocumentException si hay un error con el documento iText.
     * @throws IOException si ocurre un error de entrada/salida.
     * @throws WriterException si hay un error al generar el código QR.
     */
    byte[] generarFacturaPdf(Long pedidoId) throws DocumentException, IOException, WriterException;

    /**
     * Envía la factura generada por correo electrónico al cliente del pedido.
     *
     * @param pedidoId ID del pedido cuya factura se enviará.
     */
    void enviarFacturaPorCorreo(Long pedidoId);
}
