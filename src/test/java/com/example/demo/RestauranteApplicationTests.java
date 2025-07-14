package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demo.dao.platorepository;
import com.example.demo.entity.Plato;
import com.example.demo.service.platoimpl;

// Los siguientes imports ahora deberían funcionar sin errores
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@SpringBootTest
class RestauranteApplicationTests {

    @Autowired
    private platoimpl platodao;

    @MockBean
    private platorepository platorepo;

    // Esta línea ahora debería ser reconocida por el compilador
    @MockBean
    private S3Client s3client;

    @Test
    void contextLoads() {}

    @Test
    void testGuardarConImagenValida() throws S3Exception, AwsServiceException, SdkClientException, IOException {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "cevichediana.jpg", "image/jpeg", "Imagen de prueba".getBytes());
        String nombre = "Ceviche";
        String descripcion = "Ceviche fresco de pescado";
        String precio = "23.0";

        when(platorepo.save(any(Plato.class))).thenReturn(new Plato());

        ResponseEntity<String> response = platodao.guardar(imagen, nombre, descripcion, precio, true);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Se ha guardado correctamente el plato", response.getBody());
    }

    @Test
    void testGuardarSinImagen() throws IOException {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "", "image/jpeg", new byte[0]);
        String nombre = "Ceviche";
        String descripcion = "Ceviche fresco de pescado";
        String precio = "23.0";

        ResponseEntity<String> response = platodao.guardar(imagen, nombre, descripcion, precio, true);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No se ha seleccionado imagen", response.getBody());
    }
}
