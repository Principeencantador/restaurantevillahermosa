// src/main/java/com/example/demo/service/reservaimpl.java
package com.example.demo.service;

import com.example.demo.dao.UsuarioRepository;
import com.example.demo.dao.mesarepository;
import com.example.demo.dao.reservarepository;
import com.example.demo.entity.Mesa;
import com.example.demo.entity.Reserva;
import com.example.demo.entity.Usuario;

// --- IMPORTS PARA PDF (iText) ---
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

// --- IMPORTS PARA GRÁFICO (JFreeChart) ---
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

// --- IMPORTS PARA EXCEL (Apache POI) ---
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// --- IMPORTS DE SPRING, JAVA Y SERVLET ---
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class reservaimpl implements reservaservice {

    private static final Logger log = LoggerFactory.getLogger(reservaimpl.class);
    private final reservarepository reservaRepo;
    private final mesarepository mesaRepo;
    private final UsuarioRepository usuarioRepo;

    @Autowired
    public reservaimpl(reservarepository reservaRepo, mesarepository mesaRepo, UsuarioRepository usuarioRepo) {
        this.reservaRepo = reservaRepo;
        this.mesaRepo = mesaRepo;
        this.usuarioRepo = usuarioRepo;
    }
    
    static class ReportFooter extends PdfPageEventHelper {
        private final com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY);
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(1);
            try {
                footer.setWidths(new int[]{24});
                footer.setTotalWidth(document.right() - document.left());
                footer.setLockedWidth(true);
                PdfPCell cell = new PdfPCell(new Phrase(String.format("Página %d", writer.getPageNumber()), footerFont));
                cell.setBorder(Rectangle.TOP);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(cell);
                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottom(), writer.getDirectContent());
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }

    @Override
    public ResponseEntity<String> crear(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, int nroMesa, String nombre, String apellido, String correo, String telefono, int idUsuario, int cantidad) {
        Mesa mesa = mesaRepo.mesanro(nroMesa);
        Usuario usuario = usuarioRepo.findById(idUsuario).orElse(null);
        if (mesa == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa no encontrada");
        if (usuario == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        int ocupados = reservaRepo.contarPersonasEnRango(fechaHoraInicio, fechaHoraFin);
        if (ocupados + cantidad > 35) return ResponseEntity.badRequest().body("No hay disponibilidad en ese rango");

        List<Reserva> misReservas = reservaRepo.findReservabyusuario(idUsuario);
        long reservasPendientes = misReservas.stream().filter(r -> "pendiente".equals(r.getEstado())).count();
        if (reservasPendientes >= 5) return ResponseEntity.badRequest().body("Ya tienes 5 reservas pendientes.");

        if (fechaHoraInicio.isBefore(LocalDateTime.now(ZoneId.of("America/Lima")))) return ResponseEntity.badRequest().body("La fecha ya pasó");

        boolean solapa = misReservas.stream().anyMatch(r -> r.getMesa().getNroMesa() == nroMesa && !r.getFechaHora().isAfter(fechaHoraFin) && !fechaHoraInicio.isAfter(r.getFechaHoraFin()));
        if (solapa) return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una reserva en ese rango para esta mesa");

        Reserva nueva = new Reserva();
        nueva.setUsuario(usuario);
        nueva.setNombre(nombre);
        nueva.setApellido(apellido);
        nueva.setCorreo(correo);
        nueva.setTelefono(telefono);
        nueva.setMesa(mesa);
        nueva.setFechaHora(fechaHoraInicio);
        nueva.setFechaHoraFin(fechaHoraFin);
        nueva.setCantidad(cantidad);
        nueva.setEstado("pendiente");
        reservaRepo.save(nueva);

        return ResponseEntity.ok("Reserva creada correctamente");
    }

    @Override
    public List<Reserva> traerreserva(LocalDate fecha) { return reservaRepo.findbyestado(fecha); }

    @Override
    public List<Reserva> atendido() { return reservaRepo.atendidos(); }

    @Override
    @Scheduled(fixedRate = 60000)
    public void actualizarEstadoReserva() {
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
        List<Reserva> reservas = reservaRepo.findByEstadoInAndFechaHoraFinBefore(List.of("pendiente", "confirmado"), ahora);
        for (Reserva reserva : reservas) {
            if (!"cancelado".equals(reserva.getEstado())) {
                reserva.setEstado("exitoso");
                reservaRepo.save(reserva);
            }
        }
    }

    @Override
    public ResponseEntity<String> eliminar(int idreserva) {
        return reservaRepo.findById(idreserva).map(r -> {
            reservaRepo.delete(r);
            return ResponseEntity.ok("Reserva eliminada");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada"));
    }

    @Override
    public ResponseEntity<String> actualizarEstado(Map<String, Integer> request) {
        return reservaRepo.findById(request.get("idReserva")).map(r -> {
            r.setEstado("atendido");
            reservaRepo.save(r);
            return ResponseEntity.ok("Estado cambiado a atendido");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada"));
    }

    @Override
    public ByteArrayOutputStream crearReportePdf(HttpServletResponse response) throws DocumentException, IOException {
        List<Reserva> todas = reservaRepo.findAll();
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new ReportFooter());
        document.open();

        com.itextpdf.text.Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 51, 102));
        com.itextpdf.text.Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        com.itextpdf.text.Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        com.itextpdf.text.Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        DateTimeFormatter dtfReporte = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // --- INICIO CÓDIGO AGREGADO ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 4}); 

        // Celda para el logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            Image logo = Image.getInstance("src/main/resources/static/img/logoVillaHermosa.jpeg");
            logo.scaleToFit(100, 100); // Ajustado para que no sea tan grande
            logoCell.addElement(logo);
        } catch (Exception e) {
            log.warn("No se encontró el logo. Usando texto de fallback.");
            // Usamos una fuente más pequeña para el fallback
            com.itextpdf.text.Font fontFallback = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 51, 102));
            logoCell.addElement(new Paragraph("VILLAHERMOSA SAC", fontFallback));
        }
        headerTable.addCell(logoCell);
        
        // Celda para el título y fecha
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph titulo = new Paragraph("REPORTE GENERAL DE RESERVAS", fontTitulo);
        titulo.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(titulo);

        Paragraph fechaGen = new Paragraph("Generado el: " + LocalDateTime.now().format(dtfReporte), FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
        fechaGen.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(fechaGen);
        
        headerTable.addCell(titleCell);
        document.add(headerTable);
        document.add(Chunk.NEWLINE);
        // --- FIN CÓDIGO AGREGADO ---

        document.add(new Paragraph("Resumen del Mes Actual", fontSubtitulo));
        document.add(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        document.add(new Paragraph(" "));

        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        long totalMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual).count();
        long completadasMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual && "exitoso".equalsIgnoreCase(r.getEstado())).count();
        long canceladasMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual && "cancelado".equalsIgnoreCase(r.getEstado())).count();
        long pendientesMes = totalMes - completadasMes - canceladasMes;

        PdfPTable tablaResumen = new PdfPTable(new float[]{1.5f, 2f});
        tablaResumen.setWidthPercentage(100);
        tablaResumen.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            Image chartImage = crearGraficoTorta(completadasMes, canceladasMes, pendientesMes);
            PdfPCell cellGrafico = new PdfPCell(chartImage, true);
            cellGrafico.setBorder(Rectangle.NO_BORDER);
            cellGrafico.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaResumen.addCell(cellGrafico);
        } catch (Exception e) {
            PdfPCell errorCell = new PdfPCell(new Phrase("Error al generar gráfico: " + e.getMessage(), fontCelda));
            errorCell.setBorder(Rectangle.NO_BORDER);
            tablaResumen.addCell(errorCell);
        }

        Paragraph textoResumen = new Paragraph();
        textoResumen.setLeading(14f);
        textoResumen.add(new Chunk("Total de Reservas del Mes: " + totalMes + "\n\n", fontSubtitulo));
        textoResumen.add(new Chunk("• Completadas: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)\n", completadasMes, (totalMes > 0 ? (completadasMes * 100.0 / totalMes) : 0)), fontCelda));
        textoResumen.add(new Chunk("• Canceladas: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)\n", canceladasMes, (totalMes > 0 ? (canceladasMes * 100.0 / totalMes) : 0)), fontCelda));
        textoResumen.add(new Chunk("• Pendientes: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)", pendientesMes, (totalMes > 0 ? (pendientesMes * 100.0 / totalMes) : 0)), fontCelda));

        PdfPCell celdaTextoResumen = new PdfPCell(textoResumen);
        celdaTextoResumen.setBorder(Rectangle.NO_BORDER);
        celdaTextoResumen.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaTextoResumen.setPaddingLeft(10);
        tablaResumen.addCell(celdaTextoResumen);
        document.add(tablaResumen);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Detalle de Todas las Reservas", fontSubtitulo));
        document.add(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        document.add(new Paragraph(" "));

        PdfPTable tablaDatos = new PdfPTable(new float[]{0.5f, 2.5f, 2.5f, 0.8f, 1.5f, 1.5f, 1.2f, 2f});
        tablaDatos.setWidthPercentage(100);

        BaseColor colorHeader = new BaseColor(0, 51, 102);
        Stream.of("N°", "Cliente", "Correo", "Mesa", "Inicio", "Fin", "Estado", "Comentarios").forEach(h -> {
            PdfPCell c = new PdfPCell(new Phrase(h, fontHeaderTabla));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(colorHeader);
            c.setPadding(5);
            c.setBorderColor(BaseColor.GRAY);
            tablaDatos.addCell(c);
        });

        int cnt = 1;
        DateTimeFormatter dtfCelda = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        for (Reserva r : todas) {
            BaseColor colorFila = (cnt % 2 == 1) ? BaseColor.WHITE : new BaseColor(245, 245, 245);
            addStyledCell(tablaDatos, String.valueOf(cnt++), fontCelda, colorFila, Element.ALIGN_CENTER);
            addStyledCell(tablaDatos, r.getNombre() + " " + r.getApellido(), fontCelda, colorFila, Element.ALIGN_LEFT);
            addStyledCell(tablaDatos, r.getCorreo(), fontCelda, colorFila, Element.ALIGN_LEFT);
            addStyledCell(tablaDatos, String.valueOf(r.getMesa().getNroMesa()), fontCelda, colorFila, Element.ALIGN_CENTER);
            addStyledCell(tablaDatos, r.getFechaHora().format(dtfCelda), fontCelda, colorFila, Element.ALIGN_CENTER);
            addStyledCell(tablaDatos, r.getFechaHoraFin().format(dtfCelda), fontCelda, colorFila, Element.ALIGN_CENTER);
            addStyledCell(tablaDatos, r.getEstado(), fontCelda, colorFila, Element.ALIGN_CENTER);
            String motivo = r.getMotivoCancelacion() != null ? r.getMotivoCancelacion() : (r.getComentario() != null ? r.getComentario() : "");
            addStyledCell(tablaDatos, motivo, fontCelda, colorFila, Element.ALIGN_LEFT);
        }
        document.add(tablaDatos);
        document.close();
        return baos;
    }

    private Image crearGraficoTorta(long completadas, long canceladas, long pendientes) throws IOException, DocumentException {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (completadas > 0) dataset.setValue("Completadas", completadas);
        if (canceladas > 0) dataset.setValue("Canceladas", canceladas);
        if (pendientes > 0) dataset.setValue("Pendientes", pendientes);
        if (dataset.getItemCount() == 0) throw new IOException("No hay datos para generar el gráfico.");

        JFreeChart pieChart = ChartFactory.createPieChart(null, dataset, false, true, false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
        
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")));
        plot.setSectionPaint("Completadas", new Color(70, 179, 107));
        plot.setSectionPaint("Canceladas", new Color(217, 83, 79));
        plot.setSectionPaint("Pendientes", new Color(240, 173, 78));
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));

        BufferedImage chartImage = pieChart.createBufferedImage(400, 250, null);
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(chartImage, "png", chartBaos);
        
        return Image.getInstance(chartBaos.toByteArray());
    }

    private void addStyledCell(PdfPTable table, String text, com.itextpdf.text.Font font, BaseColor bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    @Override
    public ByteArrayOutputStream crearReporteExcel(HttpServletResponse response) throws IOException {
        List<Reserva> todas = reservaRepo.findAll();
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Reporte de Reservas");

        // --- Estilos para Excel ---
        CellStyle titleStyle = wb.createCellStyle();
        XSSFFont fTitle = wb.createFont();
        fTitle.setBold(true); fTitle.setFontHeightInPoints((short) 16);
        titleStyle.setFont(fTitle);

        CellStyle headerStyle = wb.createCellStyle();
        XSSFFont fHeader = wb.createFont();
        fHeader.setBold(true);
        headerStyle.setFont(fHeader);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle boldStyle = wb.createCellStyle();
        XSSFFont fBold = wb.createFont(); fBold.setBold(true);
        boldStyle.setFont(fBold);

        // --- Título y Fecha ---
        Row r0 = sheet.createRow(0);
        Cell c0 = r0.createCell(0);
        c0.setCellValue("REPORTE GENERAL DE RESERVAS");
        c0.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
        
        Row r1 = sheet.createRow(1);
        Cell c1 = r1.createCell(0);
        c1.setCellValue("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

        // --- Cabecera de la tabla ---
        Row header = sheet.createRow(3);
        String[] headers = {"N°", "Cliente", "Correo", "Teléfono", "Mesa", "Fecha Inicio", "Fecha Fin", "Estado", "Comentarios"};
        for (int i = 0; i < headers.length; i++) {
          Cell ch = header.createCell(i);
          ch.setCellValue(headers[i]);
          ch.setCellStyle(headerStyle);
        }
    
        // --- Datos de las reservas ---
        int fila = 4;
        int cnt = 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Reserva r : todas) {
          Row row = sheet.createRow(fila++);
          row.createCell(0).setCellValue(cnt++);
          row.createCell(1).setCellValue(r.getNombre() + " " + r.getApellido());
          row.createCell(2).setCellValue(r.getCorreo());
          row.createCell(3).setCellValue(r.getTelefono());
          row.createCell(4).setCellValue(r.getMesa().getNroMesa());
          row.createCell(5).setCellValue(r.getFechaHora().format(dtf));
          row.createCell(6).setCellValue(r.getFechaHoraFin().format(dtf));
          row.createCell(7).setCellValue(r.getEstado());
          String mot = r.getMotivoCancelacion() != null ? r.getMotivoCancelacion() : (r.getComentario() != null ? r.getComentario() : "");
          row.createCell(8).setCellValue(mot);
        }
        
        // --- MEJORA: Sección de Resumen Estadístico en Excel ---
        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        long totalMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual).count();
        long completadasMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual && "exitoso".equalsIgnoreCase(r.getEstado())).count();
        long canceladasMes = todas.stream().filter(r -> r.getFechaHora().getMonthValue() == mesActual && "cancelado".equalsIgnoreCase(r.getEstado())).count();
        long pendientesMes = totalMes - completadasMes - canceladasMes;

        int filaResumen = fila + 2;
        Row tituloResumen = sheet.createRow(filaResumen++);
        Cell cellTituloResumen = tituloResumen.createCell(0);
        cellTituloResumen.setCellValue("Resumen del Mes Actual");
        cellTituloResumen.setCellStyle(boldStyle);

        Row totalRow = sheet.createRow(filaResumen++);
        totalRow.createCell(0).setCellValue("Total de Reservas del Mes:");
        totalRow.createCell(1).setCellValue(totalMes);

        Row completadasRow = sheet.createRow(filaResumen++);
        completadasRow.createCell(0).setCellValue("• Completadas:");
        completadasRow.createCell(1).setCellValue(String.format("%d (%.1f%%)", completadasMes, (totalMes > 0 ? (completadasMes * 100.0 / totalMes) : 0)));

        Row canceladasRow = sheet.createRow(filaResumen++);
        canceladasRow.createCell(0).setCellValue("• Canceladas:");
        canceladasRow.createCell(1).setCellValue(String.format("%d (%.1f%%)", canceladasMes, (totalMes > 0 ? (canceladasMes * 100.0 / totalMes) : 0)));

        Row pendientesRow = sheet.createRow(filaResumen++);
        pendientesRow.createCell(0).setCellValue("• Pendientes:");
        pendientesRow.createCell(1).setCellValue(String.format("%d (%.1f%%)", pendientesMes, (totalMes > 0 ? (pendientesMes * 100.0 / totalMes) : 0)));

        // --- Auto-ajustar columnas ---
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out;
    }
    
    @Override
    public ResponseEntity<String> cancelar(int idReserva, String motivo) {
      return reservaRepo.findById(idReserva).map(r -> {
          r.setEstado("cancelado");
          r.setMotivoCancelacion(motivo);
          reservaRepo.save(r);
          return ResponseEntity.ok("Reserva cancelada con motivo: " + motivo);
      }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada"));
    }
    
    @Override
    public ResponseEntity<String> comentar(int idReserva, String comentario) {
        return reservaRepo.findById(idReserva).map(r -> {
            r.setComentario(comentario);
            reservaRepo.save(r);
            return ResponseEntity.ok("Comentario guardado");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada"));
    }
}
