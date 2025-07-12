package ventas.usuario;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ventas.usuario.model.Usuario;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static Usuario testUser;

    @BeforeAll
    static void setup() {
        testUser = new Usuario();
        testUser.setUsername("testuser_" + System.currentTimeMillis());
        testUser.setPassword("ValidPass123!");
        testUser.setEmail("test_" + System.currentTimeMillis() + "@example.com"); // Email único y válido
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setRole("ROLE_USER");
    }

    @Test
    @Order(1)
    @DisplayName("1. Crear usuario - Éxito")
    void shouldCreateUserSuccessfully() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/usuarios",
            testUser,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKeys("id", "username", "email");
    }

    @Test
    @Order(2)
    @DisplayName("2. Obtener usuario por ID - Éxito")
    void shouldGetUserByIdSuccessfully() {
        // Crear usuario primero
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            "/api/usuarios",
            testUser,
            Map.class
        );
        
        // Verificar que la creación fue exitosa
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().get("id")).isNotNull();
        
        Long userId = Long.valueOf(createResponse.getBody().get("id").toString());
        
        // Obtener el usuario
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/usuarios/" + userId,
            Map.class
        );
        
        // Verificaciones más robustas
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .isNotNull()
            .containsKeys("id", "username", "email", "firstName", "lastName", "enabled", "role");
        
        assertThat(response.getBody().get("username")).isEqualTo(testUser.getUsername());
    }

    @Test
    @Order(3)
    @DisplayName("3. Actualizar usuario - Éxito")
    void shouldUpdateUserSuccessfully() {
        // Crear usuario primero
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            "/api/usuarios",
            testUser,
            Map.class
        );
        
        Long userId = Long.valueOf(createResponse.getBody().get("id").toString());
        
        // Datos de actualización
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("username", "updateduser");
        updateData.put("firstName", "Updated");
        updateData.put("lastName", "Name");
        
        // Actualizar usuario
        restTemplate.put(
            "/api/usuarios/" + userId,
            updateData
        );
        
        // Verificar cambios
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
            "/api/usuarios/" + userId,
            Map.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("username")).isEqualTo("updateduser");
        assertThat(getResponse.getBody().get("firstName")).isEqualTo("Updated");
        assertThat(getResponse.getBody().get("lastName")).isEqualTo("Name");
    }

    @Test
    @Order(4)
    @DisplayName("4. Eliminar usuario - Éxito")
    void shouldDeleteUserSuccessfully() {
        // Crear usuario primero
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            "/api/usuarios",
            testUser,
            Map.class
        );
        
        Long userId = Long.valueOf(createResponse.getBody().get("id").toString());
        
        // Eliminar usuario
        restTemplate.delete("/api/usuarios/" + userId);
        
        // Verificar que ya no existe
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
            "/api/usuarios/" + userId,
            Map.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(5)
    @DisplayName("5. Crear usuario - Error contraseña corta")
    void shouldFailWhenPasswordTooShort() {
        Usuario invalidUser = new Usuario();
        invalidUser.setUsername("invaliduser");
        invalidUser.setPassword("short");
        invalidUser.setEmail("invalid@example.com");
        invalidUser.setFirstName("Invalid");
        invalidUser.setLastName("User");
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/usuarios",
            invalidUser,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
    }
}