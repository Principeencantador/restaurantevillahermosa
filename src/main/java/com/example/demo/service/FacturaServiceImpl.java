package com.example.demo.service;

import com.example.demo.dao.pedidorepository;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.PedidoPlato;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FacturaServiceImpl implements FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaServiceImpl.class);

    @Autowired
    private pedidorepository pedidoRepo;

    @Autowired
    private JavaMailSender mailSender;

    // --- Definición de fuentes y colores ---
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SUBTITLE_BOLD = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_BODY = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_BODY_BOLD = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_SMALL_ITALIC = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC);
    private static final Font FONT_COMPANY_DETAILS = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);
    private static final BaseColor TABLE_HEADER_BACKGROUND = new BaseColor(230, 230, 230);


    @Override
    public byte[] generarFacturaPdf(Long pedidoId) throws DocumentException, IOException, WriterException {
        Pedido pedido = pedidoRepo.findById(Math.toIntExact(pedidoId))
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // --- ESTRUCTURA PRINCIPAL DEL DOCUMENTO ---
        doc.add(createHeader(pedido));
        doc.add(createInvoiceDetails(pedido));
        doc.add(Chunk.NEWLINE);
        doc.add(createCustomerInfoTable(pedido));
        doc.add(Chunk.NEWLINE);
        doc.add(createItemsTable(pedido));
        doc.add(createTotalsInfoTable(pedido));
        doc.add(createCenteredQrTable(pedido));
        doc.add(createFooter());

        doc.close();
        return baos.toByteArray();
    }

    // --- Métodos de ayuda rediseñados ---

    private PdfPTable createHeader(Pedido pedido) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setPaddingBottom(15);
        try {
            Image logo = Image.getInstance("src/main/resources/static/img/logoVillaHermosa.jpeg");
            logo.scaleToFit(150, 150);
            logoCell.addElement(logo);
        } catch (Exception e) {
            log.warn("No se encontró el logo. Usando texto de fallback.");
            logoCell.addElement(new Paragraph("VILLAHERMOSA SAC", FONT_TITLE));
        }
        headerTable.addCell(logoCell);

        PdfPCell companyDetailsCell = new PdfPCell();
        companyDetailsCell.setBorder(Rectangle.NO_BORDER);
        companyDetailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        companyDetailsCell.setPaddingBottom(10);
        Paragraph companyDetails = new Paragraph("RUC: 12345678901 | AV. LAS FLORES 123 - ICA - PERÚ", FONT_COMPANY_DETAILS);
        companyDetailsCell.addElement(companyDetails);
        headerTable.addCell(companyDetailsCell);

        return headerTable;
    }
    
    private PdfPTable createInvoiceDetails(Pedido pedido) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph title = new Paragraph("BOLETA DE VENTA ELECTRÓNICA", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(title);

        Paragraph invoiceNumber = new Paragraph("N°: BE01-" + String.format("%06d", pedido.getId_pedido()), FONT_BODY_BOLD);
        invoiceNumber.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(invoiceNumber);
        
        Paragraph date = new Paragraph("Fecha: " + pedido.getFecha(), FONT_BODY);
        date.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(date);
        
        table.addCell(cell);
        return table;
    }

    // --- MÉTODO MODIFICADO ---
    private PdfPTable createCustomerInfoTable(Pedido pedido) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(10);
        cell.addElement(new Paragraph("CLIENTE", FONT_SUBTITLE_BOLD));
        cell.addElement(new Paragraph("Nombre: " + pedido.getId_usuario().getNombre(), FONT_BODY));
        
        // **SE USA EL DNI REAL DE LA BASE DE DATOS**
        String dni = pedido.getId_usuario().getDni();
        cell.addElement(new Paragraph("DNI: " + (dni != null ? dni : "No especificado"), FONT_BODY));
        
        cell.addElement(new Paragraph("Email: " + pedido.getId_usuario().getEmail(), FONT_BODY));
        table.addCell(cell);

        return table;
    }

    private PdfPTable createItemsTable(Pedido pedido) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});
        table.setSpacingAfter(15f);

        addHeaderCellToTable(table, "Descripción");
        addHeaderCellToTable(table, "Cant.");
        addHeaderCellToTable(table, "P. Unitario");
        addHeaderCellToTable(table, "Importe");

        for (PedidoPlato pp : pedido.getPedidoPlatos()) {
            table.addCell(createCell(pp.getPlato().getNombre(), Element.ALIGN_LEFT));
            table.addCell(createCell(String.valueOf(pp.getCantidad()), Element.ALIGN_CENTER));
            table.addCell(createCell(String.format("S/. %.2f", pp.getPlato().getPrecio()), Element.ALIGN_RIGHT));
            table.addCell(createCell(String.format("S/. %.2f", pp.getPlato().getPrecio() * pp.getCantidad()), Element.ALIGN_RIGHT));
        }
        return table;
    }
    
    private PdfPTable createTotalsInfoTable(Pedido pedido) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 1f});

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingRight(20);
        BigDecimal total = BigDecimal.valueOf(pedido.getTotal());
        infoCell.addElement(new Paragraph("SON: " + convertirTotal(total).toUpperCase() + " SOLES", FONT_BODY));
        infoCell.addElement(new Paragraph("Forma de pago: IZIPAY - VISA", FONT_BODY));
        table.addCell(infoCell);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.addCell(createTotalsCell("Total:", Element.ALIGN_RIGHT, FONT_BODY_BOLD));
        totalsTable.addCell(createTotalsCell(String.format("S/. %.2f", total), Element.ALIGN_RIGHT, FONT_BODY_BOLD));
        PdfPCell totalsContainer = new PdfPCell(totalsTable);
        totalsContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(totalsContainer);

        return table;
    }
    
    private PdfPTable createCenteredQrTable(Pedido pedido) throws WriterException, IOException, DocumentException {
        PdfPTable qrTable = new PdfPTable(1);
        qrTable.setWidthPercentage(100);
        qrTable.setSpacingBefore(20f);

        String qrData = "https://villahermosa.pe/consulta-cpe?id=" + pedido.getId_pedido();
        Image qrImage = generarQR(qrData, 120, 120);
        
        PdfPCell qrCell = new PdfPCell(qrImage, true);
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrTable.addCell(qrCell);

        return qrTable;
    }
    
    private Paragraph createFooter() {
        Paragraph footer = new Paragraph("Gracias por su preferencia.", FONT_SMALL_ITALIC);
        footer.setSpacingBefore(20f);
        footer.setAlignment(Element.ALIGN_CENTER);
        return footer;
    }

    private void addHeaderCellToTable(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(TABLE_HEADER_BACKGROUND);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(8);
        table.addCell(cell);
    }
    
    private PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createTotalsCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        return cell;
    }
    
    @Override
    public void enviarFacturaPorCorreo(Long pedidoId) {
        try {
            byte[] pdf = generarFacturaPdf(pedidoId);
            Pedido pedido = pedidoRepo.findById(Math.toIntExact(pedidoId))
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(pedido.getId_usuario().getEmail());
            helper.setSubject("Boleta de Venta Pedido #" + pedido.getId_pedido());
            helper.setText("Hola " + pedido.getId_usuario().getNombre() + ",\n\n"
                    + "Adjuntamos tu boleta de venta electrónica. ¡Gracias por tu compra!\n\n"
                    + "Saludos,\nEquipo Villahermosa", false);
            helper.addAttachment("boleta_" + pedido.getId_pedido() + ".pdf", new ByteArrayResource(pdf));
            mailSender.send(message);
        } catch (DocumentException | IOException | MessagingException | WriterException e) {
            log.error("No se pudo generar o enviar la boleta para el pedido {}: {}", pedidoId, e.getMessage(), e);
        }
    }

    private Image generarQR(String data, int width, int height) throws WriterException, IOException, BadElementException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix matrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, matrix.get(x, y) ? java.awt.Color.BLACK.getRGB() : java.awt.Color.WHITE.getRGB());
            }
        }
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", pngOutputStream);
        return Image.getInstance(pngOutputStream.toByteArray());
    }

    private String convertirTotal(BigDecimal total) {
        int soles = total.intValue();
        int centavos = total.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue();
        return String.format("%s con %02d/100", numeroATexto(soles), centavos);
    }

    private String numeroATexto(int num) {
        if (num < 0) return "Menos " + numeroATexto(Math.abs(num));
        if (num == 100) return "Cien";
        
        String[] unidades = {"Cero", "Uno", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete", "Ocho", "Nueve", "Diez", "Once", "Doce", "Trece", "Catorce", "Quince", "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve"};
        String[] decenas = {"", "", "Veinte", "Treinta", "Cuarenta", "Cincuenta", "Sesenta", "Setenta", "Ochenta", "Noventa"};
        
        if (num < 20) return unidades[num];
        if (num < 30) return "Veinti" + unidades[num % 10].toLowerCase();
        if (num < 100) return decenas[num / 10] + (num % 10 != 0 ? " y " + unidades[num % 10].toLowerCase() : "");
        if (num < 1000) {
            int centenas = num / 100;
            String textoCentenas = "";
            if (centenas == 1) {
                textoCentenas = (num % 100 == 0) ? "Cien" : "Ciento ";
            } else {
                textoCentenas = unidades[centenas] + "cientos ";
            }
            return textoCentenas + (num % 100 != 0 ? numeroATexto(num % 100).toLowerCase() : "");
        }
        return String.valueOf(num); 
    }
}
