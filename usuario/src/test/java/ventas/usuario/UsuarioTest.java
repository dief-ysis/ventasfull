package ventas.usuario;

import ventas.usuario.controller.UsuarioController;
import ventas.usuario.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate; 
import org.springframework.boot.test.web.server.LocalServerPort; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
public class UsuarioTest {

    @Autowired
    private UsuarioController usuarioController; 

    @LocalServerPort
    private int port; 

    @Autowired
    private TestRestTemplate restTemplate; 

    @Test
    void contextLoads() {
        System.out.println("Cargando el contexto de Spring Boot para las pruebas...");
        System.out.println("El servidor de pruebas está corriendo en el puerto: " + port);
        assertThat(usuarioController).isNotNull(); 
    }

    @Test
    void shouldReturnListOfUsers() {
        ResponseEntity<Usuario[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/usuarios",
                Usuario[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    void shouldCreateUserAndReturnCreatedStatus() {
        Usuario newUser = new Usuario();
        newUser.setUsername("testuser" + System.currentTimeMillis()); 
        newUser.setPassword("password123");
        newUser.setEmail("test@example.com");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setAddress("123 Test St");
        newUser.setPhone("555-1234");
        newUser.setEnabled(true);
        newUser.setRole("ROLE_USER");

        ResponseEntity<Usuario> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/usuarios",
                newUser,
                Usuario.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo(newUser.getUsername());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        ResponseEntity<Usuario> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/usuarios/99999", 
                Usuario.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}