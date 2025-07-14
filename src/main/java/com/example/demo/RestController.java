package com.example.demo;

import com.example.demo.service.FacturaService;
import com.example.demo.service.Usuarioimpl;
import com.example.demo.service.mesaimpl;
import com.example.demo.service.pedidoimpl;
import com.example.demo.service.platoimpl;
import com.example.demo.service.reservaimpl;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.dao.RolRepository;
import com.example.demo.dao.UsuarioRepository;
import com.example.demo.dao.pedidorepository;
import com.example.demo.dao.reservarepository;
import com.example.demo.entity.Mesa;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.Plato;
import com.example.demo.entity.Reserva;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.security.jwt.jwtFilter;
import com.example.demo.security.jwt.jwtUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class RestController {

    @Autowired
    private RolRepository roldao;
    @Autowired
    private Usuarioimpl usuarioimpl;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private pedidorepository pedidorepository;
    @Autowired
    public jwtFilter jwtFilter;
    @Autowired
    public jwtUtil jwtUtil;
    @Autowired
    private platoimpl platodao;
    @Autowired
    private reservaimpl reservadao;
    @Autowired
    private reservarepository reservarepository;
    @Autowired
    private mesaimpl mesadao;
        @Autowired
    private FacturaService facturaService;

    @Autowired
    private pedidoimpl pedidodao;

    @GetMapping("/index")
    public String comienzo(Model modelo, HttpSession session, HttpServletResponse response) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        String nombre = (String) session.getAttribute("username");
        modelo.addAttribute("nombre", nombre);
        return "index";
    }

    @GetMapping("/hola")
    public String hola(Model modelo, HttpServletResponse response, HttpSession session) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));

        List<Usuario> traerusuarios = usuarioimpl.traerusuarios();
        modelo.addAttribute("empleados", traerusuarios);

        return "hola";
    }

    @GetMapping("/login_admin")
    public String login(Model modelo, HttpServletResponse response, HttpSession session) {

        if (session.getAttribute("token") != null) {
            return "redirect:/index";
        }
        // Añadir el token al encabezado de la respuesta
        response.addHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        return "login";
    }

    @GetMapping("/carrito")
    public String carrito(Model modelo, HttpServletRequest request, HttpServletResponse response,
            HttpSession session) {
        String email = "";
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        if (session.getAttribute("token") != null) {
            email = jwtUtil.extractUsername((String) session.getAttribute("token"));
        }
        modelo.addAttribute("email", email);
        List<Plato> platos = platodao.traerplatos();
        modelo.addAttribute("platos", platos);
        return "carrito";
    }

    @GetMapping("/regform")
    public String register(Model modelo) {
        return "regform";
    }

    @GetMapping("/prueba")
    public String prueba(Model modelo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        return "prueba";
    }

    @GetMapping("/carta")
    public String carta(Model modelo, HttpSession session) {
        ArrayList<Plato> traerPlatos = platodao.traerplatos();
        modelo.addAttribute("platos", traerPlatos);
        String nombre = (String) session.getAttribute("username");
        modelo.addAttribute("nombre", nombre);
        return "carta";
    }

    @GetMapping("/chef")
    public String chef(Model modelo, HttpSession session) {
        String nombre = (String) session.getAttribute("username");
        modelo.addAttribute("nombre", nombre);
        return "chef";
    }

    @GetMapping("/menu_login")
    public String menu(Model modelo, HttpServletResponse response, HttpSession session) {
        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }
        return "menu_login";
    }

    @GetMapping("/platos")
    public String platos(Model modelo, HttpServletResponse response, HttpSession session) {

        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }

        ArrayList<Plato> traerPlatos = platodao.traerplatos();

        modelo.addAttribute("platos", traerPlatos);
        return "platos";
    }

    @GetMapping("/contacto")
    public String contacto(Model modelo, HttpServletResponse response, HttpSession session) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        String nombre = (String) session.getAttribute("username");
        modelo.addAttribute("nombre", nombre);
        return "contacto";
    }

    @GetMapping("/reserva")
    public String reserva(Model modelo, HttpServletResponse response, HttpSession session) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        return "reserva";
    }

    @GetMapping("/mesas")
    public String mesa(Model modelo, HttpServletResponse response, HttpSession session) {
        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }
        ArrayList<Mesa> traermesas = (ArrayList<Mesa>) mesadao.traerMesas();
        modelo.addAttribute("mesas", traermesas);
        return "mesa";
    }

@GetMapping("/success")
public String success(
        @RequestParam(value = "pedidoId", required = false) Long pedidoId,
        Model modelo,
        HttpServletResponse response,
        HttpSession session) {

    // mantiene el token en cabecera como antes
    response.setHeader("Authorization", "Bearer " + session.getAttribute("token"));

    // envío automático de la boleta solo si viene el parámetro
    if (pedidoId != null) {
        facturaService.enviarFacturaPorCorreo(pedidoId);
        modelo.addAttribute("mensaje", "Boleta enviada a tu correo");
    }

    // nombre de la plantilla (asegúrate de que el archivo sea success.html)
    return "sucess";
}

    @GetMapping("/reservas")
    public String reservas(Model modelo, HttpServletResponse response, HttpSession session) {
        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }
        // Obtenemos las reservas atendidas
        List<Reserva> listareservatendida = reservadao.atendido();
        List<Reserva> allreservas = reservarepository.findAll();
        // Usamos LocalDate para obtener la fecha actual
        LocalDate fechaActual = LocalDate.now(ZoneId.of("America/Lima"));

        // Traemos las reservas filtradas por la fecha actual
        List<Reserva> listareserva = reservadao.traerreserva(fechaActual);

        modelo.addAttribute("todasReservas", allreservas);
        modelo.addAttribute("nombre", session.getAttribute("username"));
        modelo.addAttribute("reservas", listareserva);
        modelo.addAttribute("fechaactual", fechaActual);
        modelo.addAttribute("reservasatendidas", listareservatendida);
        return "tablareservas";
    }

    @GetMapping("/misreservas")
    public String misreservas(Model modelo, HttpServletResponse response, HttpSession session) {

        if (session.getAttribute("token") != null) {
            response.setHeader("Authorization", "Bearer " +
                    session.getAttribute("token"));
            Usuario usuario = usuarioRepository.findByCorreo(jwtUtil.extractUsername((String) session.getAttribute("token")));

            if (usuario == null) {
                return "redirect:/index";
            }
            List<Reserva> reservauser = reservarepository.findReservabyusuario(usuario.getId_usuario());

            modelo.addAttribute("reservas", reservauser);
        } else {

            return "redirect:/index";
        }

        return "misreservas";
    }

    @GetMapping("/verificacion")
    public String verificacion() {

        return "verificacion";
    }

    @GetMapping("/usuarios")
    public String Usuarios(Model modelo, HttpServletResponse response, HttpSession session) {
        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }
        List<Usuario> listadousuarios = usuarioRepository.findAll();
        modelo.addAttribute("usuarios", listadousuarios);
        return "tablausuario";
    }

    @PostMapping("/cerrar")
    public ResponseEntity<String> logout(HttpSession session) {
        // Eliminar el token y cerrar la sesión
        session.invalidate();
        jwtFilter.destroy();

        return new ResponseEntity<>("Sesión cerrada correctamente", HttpStatus.OK);
    }

    @GetMapping("/mispedidos")
    public String pagos(Model modelo, HttpSession session) {
        List<Plato> listaplato = platodao.traerplatos();
        if (session.getAttribute("token") == null) {
            return "redirect:/index";
        } else if (session.getAttribute("token") != null) {
            List<Pedido> pedido = pedidodao
                    .traerPedidobycorreo(jwtUtil.extractUsername((String) session.getAttribute("token")));

            for (Pedido pedido1 : pedido) {
                pedido1.calcularTotal();
            }
            modelo.addAttribute("email", jwtUtil.extractUsername((String) session.getAttribute("token")));
            modelo.addAttribute("pedidos", pedido);
            modelo.addAttribute("platos", listaplato);
        }
        return "pedidos";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model modelo, HttpServletResponse response, HttpSession session) {
        String redirect = soloadmins(modelo, response, session);
        if (redirect != null) {
            return redirect;
        }
        List<Pedido> pedido = pedidorepository.ObtenerPedidosespera();
        for (Pedido pedido1 : pedido) {
            pedido1.calcularTotal();
        }
        List<Pedido> pedidosatendidos = pedidorepository.ObtenerPedidosatendidos();
        modelo.addAttribute("pedidos", pedido);
        modelo.addAttribute("pedidosatendidos", pedidosatendidos);
        return "tablapedidos";
    }

    public String soloadmins(Model modelo, HttpServletResponse response, HttpSession session) {
        response.setHeader("Authorization", "Bearer " +
                session.getAttribute("token"));
        if (session.getAttribute("token") != null) {
            roldao.findByNombre(jwtUtil.extractRol((String) session.getAttribute("token")));
            Rol rol = roldao.findByNombre(jwtUtil.extractRol((String) session.getAttribute("token")));
            if (rol.getNombre().equals("USER")) {
                return "redirect:/index";
            }
        } else {
            return "redirect:/index";
        }
        return null;

    }

    @GetMapping("/crearreporte")
    public void generarReporte(HttpServletResponse response) throws IOException, DocumentException {
        // Simulación de generación de reporte PDF
        byte[] reportePdf = reservadao.crearReportePdf(response).toByteArray(); // Aquí generas los bytes del archivo PDF

        // Establecer las cabeceras para indicar que es un archivo PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_reservas.pdf");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_reservas.pdf");
        response.getOutputStream().write(reportePdf);
        response.getOutputStream().flush();
        ByteArrayInputStream in = new ByteArrayInputStream(reportePdf);
        FileCopyUtils.copy(in, response.getOutputStream());
        // Retornar el ResponseEntity con los datos binarios (PDF)

    }
 @GetMapping("/excelreservas")
    public void descargarExcel(HttpServletResponse response) throws IOException {
        // Y aquí al crearReporteExcel
        byte[] xlsxBytes = reservadao.crearReporteExcel(response).toByteArray();

        response.setContentType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader("Content-Disposition", "attachment; filename=reporte_reservas.xlsx");
        response.setContentLength(xlsxBytes.length);
        response.getOutputStream().write(xlsxBytes);
        response.getOutputStream().flush();
    }
    @GetMapping("/checkout")
    public String getMethodName() {
        return "checkout";
    }

}
