package com.example.demo.service;

// --- IMPORTS ORIGINALES ---
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.demo.dao.UsuarioRepository;
import com.example.demo.dao.pedido_platorepository;
import com.example.demo.dao.pedidorepository;
import com.example.demo.dao.platorepository;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.PedidoPlato;
import com.example.demo.entity.Plato;
import com.example.demo.entity.Usuario;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// --- IMPORTS AÑADIDOS PARA EL REPORTE PDF ---
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class pedidoimpl implements pedidoservice {
    private static final Logger log = LoggerFactory.getLogger(pedidoimpl.class);

    @Autowired
    private pedidorepository pedidorepo;

    @Autowired
    private UsuarioRepository usuariorepo;

    @Autowired
    private platorepository platorepo;

    @Autowired
    private pedido_platorepository pedido_platodao;

    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<String> guardarPedido(Map<String, Object> params, LocalDateTime datetime) {
        try {
            String correo = (String) params.get("correo");
            Usuario usuario = usuariorepo.findByCorreo(correo);
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            List<Integer> idPlatos = (List<Integer>) params.get("id_platos");
            List<Integer> cantidades = (List<Integer>) params.get("cantidades");
            List<Plato> platos = platorepo.findAllById(idPlatos);
            if (platos.isEmpty()) {
                return ResponseEntity.badRequest().body("Platos no encontrados");
            }

            Pedido pedido = new Pedido();
            pedido.setId_usuario(usuario);
            pedido.setFecha(datetime);
            pedido.setEstado((String) params.get("estado"));

            List<PedidoPlato> pedidoPlatos = new ArrayList<>();
            for (int i = 0; i < platos.size(); i++) {
                Plato plato = platos.get(i);
                int cantidad = cantidades.get(i);

                PedidoPlato pedidoPlato = new PedidoPlato();
                pedidoPlato.setPedido(pedido);
                pedidoPlato.setPlato(plato);
                pedidoPlato.setCantidad(cantidad);
                pedidoPlatos.add(pedidoPlato);
            }
            pedido.setPedidoPlatos(pedidoPlatos);
            pedido.calcularTotal();
            pedidorepo.save(pedido);
            return ResponseEntity.ok("Pedido guardado correctamente");
        } catch (Exception e) {
            log.error("Error al guardar pedido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el pedido: " + e.getMessage());
        }
    }
    
    @Override
    public List<Pedido> listarPedidosPendientes() {
        return pedidorepo.findByEstado("ESPERA");
    }

    @Override
    public List<Pedido> listarPedidosAtendidos() {
        return pedidorepo.findByEstadoIn(Arrays.asList("atendido", "terminado"));
    }
    
    @Override
    public List<Pedido> traerPedidobycorreo(String correo) {
        List<Pedido> pedidos = pedidorepo.findPedidobyCorreo(correo);
        if (pedidos == null) {
            return new ArrayList<>();
        }
        for (Pedido pedido : pedidos) {
            pedido.calcularTotal();
            pedidorepo.save(pedido);
        }
        return pedidos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<String> agregarplatos(Map<String, Object> params) {
        Pedido pedido = pedidorepo.findById((int) params.get("id")).orElse(null);
        if (Objects.isNull(pedido)) {
            return ResponseEntity.badRequest().body("Pedido no encontrado");
        }
        List<Integer> idplatos = (List<Integer>) params.get("id_platos");
        List<Integer> cantidades = (List<Integer>) params.get("cantidades");
        if (Objects.isNull(idplatos) || idplatos.isEmpty() || Objects.isNull(cantidades) || cantidades.isEmpty()
                || idplatos.size() != cantidades.size()) {
            return ResponseEntity.badRequest().body("Datos de platos inválidos");
        }

        List<Plato> platos = platorepo.findAllById(idplatos);
        for (int i = 0; i < platos.size(); i++) {
            Plato plato = platos.get(i);
            int cantidad = cantidades.get(i);
            PedidoPlato pedidoPlato = new PedidoPlato();
            pedidoPlato.setPedido(pedido);
            pedidoPlato.setPlato(plato);
            pedidoPlato.setCantidad(cantidad);
            pedido_platodao.save(pedidoPlato);
        }

        pedido.calcularTotal();
        pedidorepo.save(pedido);
        return ResponseEntity.ok("Platos agregados al pedido correctamente");
    }

    @Override
    public ResponseEntity<String> eliminarplatopedido(int id_plato, int id_pedidoplato) {
        Plato plato = platorepo.findById(id_plato);
        if (plato == null) {
            return ResponseEntity.badRequest().body("Plato no encontrado");
        }
        PedidoPlato pedidoplato = pedido_platodao.encontrarpedidoplatoporplatoyid(plato.getId_plato(), id_pedidoplato);
        if (pedidoplato == null) {
            return ResponseEntity.badRequest().body("Pedido-Plato no encontrado");
        }
        pedido_platodao.delete(pedidoplato);

        Pedido pedido = pedidoplato.getPedido();
        pedido.calcularTotal();
        pedidorepo.save(pedido);

        List<PedidoPlato> restantes = pedido_platodao.findByPedidoId(pedido.getId_pedido());
        if (restantes.isEmpty()) {
            pedidorepo.delete(pedido);
            return ResponseEntity.ok("Plato eliminado. El pedido fue eliminado porque no tiene más platos.");
        }
        return ResponseEntity.ok("Plato eliminado del pedido correctamente");
    }

    @Override
    public ResponseEntity<String> actualizarEstado(Map<String, String> request) {
        String idString = request.get("id_pedido");
        int id = Integer.parseInt(idString);
        Pedido pedido = pedidorepo.findById(id).orElse(null);
        if (pedido == null) {
            return ResponseEntity.badRequest().body("Pedido no encontrado");
        }
        pedido.setEstado("atendido");
        pedidorepo.save(pedido);
        return ResponseEntity.ok("Pedido actualizado correctamente");
    }

    @Override
    public ResponseEntity<String> rembolso(Map<String, String> requestMap, String secretKey) {
        String idString = requestMap.get("id_pedido");
        if (idString == null) {
            return ResponseEntity.badRequest().body("Falta id_pedido en la solicitud");
        }
        int pedidoId;
        try {
            pedidoId = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("id_pedido inválido: " + idString);
        }
        Pedido pedido = pedidorepo.findById(pedidoId).orElse(null);
        if (pedido == null) {
            return ResponseEntity.badRequest().body("Pedido no encontrado: " + pedidoId);
        }
        String paymentIntentId = pedido.getId_pago();
        if (paymentIntentId == null || paymentIntentId.isEmpty()) {
            return ResponseEntity.badRequest().body("El pedido no tiene un pago registrado");
        }
        try {
            Stripe.apiKey = secretKey;
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            Refund refund = Refund.create(params);
            if ("succeeded".equals(refund.getStatus())) {
                pedido.setEstado("REEMBOLSADO");
                pedidorepo.save(pedido);
                return ResponseEntity.ok("Reembolso realizado correctamente. ID: " + refund.getId());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Reembolso con status inesperado: " + refund.getStatus());
        } catch (StripeException e) {
            log.error("Error de Stripe al reembolso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error de Stripe: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al procesar reembolso", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar reembolso: " + e.getMessage());
        }
    }

    // =======================================================================
    // ==         NUEVA FUNCIONALIDAD: REPORTE PDF DE PEDIDOS             ==
    // =======================================================================

    @Override
    public ByteArrayOutputStream crearReportePedidosPdf() throws DocumentException, IOException {
        List<Pedido> todos = pedidorepo.findAll();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 51, 102));
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        DateTimeFormatter dtfReporte = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 4});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        try {
            Image logo = Image.getInstance("src/main/resources/static/img/logoVillaHermosa.jpeg");
            logo.scaleToFit(80, 80);
            logoCell.addElement(logo);
        } catch (Exception e) {
            log.warn("Logo no encontrado. Usando texto de fallback.");
            logoCell.addElement(new Paragraph("VILLAHERMOSA", fontSubtitulo));
        }
        headerTable.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph titulo = new Paragraph("REPORTE GENERAL DE PEDIDOS", fontTitulo);
        titulo.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(titulo);
        Paragraph fechaGen = new Paragraph("Generado el: " + LocalDateTime.now().format(dtfReporte), FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
        fechaGen.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(fechaGen);
        headerTable.addCell(titleCell);
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Resumen del Mes Actual", fontSubtitulo));
        document.add(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        document.add(new Paragraph(" "));

        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        long totalMes = todos.stream().filter(p -> p.getFecha().getMonthValue() == mesActual).count();
        long completadosMes = todos.stream().filter(p -> p.getFecha().getMonthValue() == mesActual && (p.getEstado().equalsIgnoreCase("atendido") || p.getEstado().equalsIgnoreCase("terminado"))).count();
        long pendientesMes = todos.stream().filter(p -> p.getFecha().getMonthValue() == mesActual && p.getEstado().equalsIgnoreCase("ESPERA")).count();
        long canceladosMes = todos.stream().filter(p -> p.getFecha().getMonthValue() == mesActual && (p.getEstado().equalsIgnoreCase("cancelado") || p.getEstado().equalsIgnoreCase("reembolsado"))).count();

        PdfPTable tablaResumen = new PdfPTable(new float[]{1.5f, 2f});
        tablaResumen.setWidthPercentage(100);
        tablaResumen.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            Image chartImage = crearGraficoTortaPedidos(completadosMes, pendientesMes, canceladosMes);
            PdfPCell cellGrafico = new PdfPCell(chartImage, true);
            cellGrafico.setBorder(Rectangle.NO_BORDER);
            cellGrafico.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaResumen.addCell(cellGrafico);
        } catch (Exception e) {
            tablaResumen.addCell(new Phrase("Error al generar gráfico: " + e.getMessage(), fontCelda));
        }

        Paragraph textoResumen = new Paragraph();
        textoResumen.setLeading(14f);
        textoResumen.add(new Chunk("Total de Pedidos del Mes: " + totalMes + "\n\n", fontSubtitulo));
        textoResumen.add(new Chunk("• Completados: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)\n", completadosMes, (totalMes > 0 ? (completadosMes * 100.0 / totalMes) : 0)), fontCelda));
        textoResumen.add(new Chunk("• Pendientes: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)\n", pendientesMes, (totalMes > 0 ? (pendientesMes * 100.0 / totalMes) : 0)), fontCelda));
        textoResumen.add(new Chunk("• Cancelados/Reembolsados: ", fontCelda));
        textoResumen.add(new Chunk(String.format("%d (%.1f%%)", canceladosMes, (totalMes > 0 ? (canceladosMes * 100.0 / totalMes) : 0)), fontCelda));

        PdfPCell celdaTextoResumen = new PdfPCell(textoResumen);
        celdaTextoResumen.setBorder(Rectangle.NO_BORDER);
        celdaTextoResumen.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tablaResumen.addCell(celdaTextoResumen);
        document.add(tablaResumen);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Detalle de Todos los Pedidos", fontSubtitulo));
        document.add(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        document.add(new Paragraph(" "));

        PdfPTable tablaDatos = new PdfPTable(new float[]{0.4f, 1.5f, 2f, 1f, 3f, 1f});
        tablaDatos.setWidthPercentage(100);

        BaseColor colorHeader = new BaseColor(0, 51, 102);
        Stream.of("ID", "Cliente", "Fecha", "Estado", "Platos", "Total").forEach(h -> {
            PdfPCell c = new PdfPCell(new Phrase(h, fontHeaderTabla));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(colorHeader);
            c.setPadding(5);
            tablaDatos.addCell(c);
        });

        DateTimeFormatter dtfCelda = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        for (Pedido p : todos) {
            addStyledCell(tablaDatos, String.valueOf(p.getId_pedido()), fontCelda, Element.ALIGN_CENTER);
            String nombreCliente = p.getId_usuario() != null ? p.getId_usuario().getNombre() : "N/A";
            addStyledCell(tablaDatos, nombreCliente, fontCelda, Element.ALIGN_LEFT);
            addStyledCell(tablaDatos, p.getFecha().format(dtfCelda), fontCelda, Element.ALIGN_CENTER);
            addStyledCell(tablaDatos, p.getEstado(), fontCelda, Element.ALIGN_CENTER);

            String platosStr = p.getPedidoPlatos().stream()
                .map(pp -> pp.getCantidad() + "x " + pp.getPlato().getNombre())
                .collect(Collectors.joining("\n"));
            addStyledCell(tablaDatos, platosStr, fontCelda, Element.ALIGN_LEFT);
            
            addStyledCell(tablaDatos, String.format("S/ %.2f", p.getTotal()), fontCelda, Element.ALIGN_RIGHT);
        }
        document.add(tablaDatos);

        document.close();
        return baos;
    }

    private Image crearGraficoTortaPedidos(long completados, long pendientes, long cancelados) throws IOException, DocumentException {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (completados > 0) dataset.setValue("Completados", completados);
        if (pendientes > 0) dataset.setValue("Pendientes", pendientes);
        if (cancelados > 0) dataset.setValue("Cancelados", cancelados);
        if (dataset.getItemCount() == 0) throw new IOException("No hay datos para generar el gráfico de pedidos.");

        JFreeChart pieChart = ChartFactory.createPieChart(null, dataset, false, true, false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
        
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")));
        plot.setSectionPaint("Completados", new Color(40, 167, 69)); // Verde
        plot.setSectionPaint("Pendientes", new Color(255, 193, 7)); // Amarillo
        plot.setSectionPaint("Cancelados", new Color(220, 53, 69)); // Rojo
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(null);

        BufferedImage chartImage = pieChart.createBufferedImage(350, 220, null);
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(chartImage, "png", chartBaos);
        
        return Image.getInstance(chartBaos.toByteArray());
    }

    private void addStyledCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}
