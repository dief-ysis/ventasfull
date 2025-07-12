package ventas.usuario;

import ventas.usuario.controller.UsuarioController;
import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import ventas.usuario.service.UserService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase
public class UsuarioTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private static Faker faker;
    private static Random random;
    private static AtomicInteger usernameCounter = new AtomicInteger(1);

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        usernameCounter.set(1);
        if (faker == null) {
            faker = new Faker();
            random = new Random();
        }
    }

    private Usuario createRandomUser() {
    Usuario user = new Usuario();
    user.setUsername("user_test_" + System.currentTimeMillis() + "_" + usernameCounter.getAndIncrement());
    user.setPassword("TestPass123!"); // Contraseña válida
    user.setEmail("test" + usernameCounter.get() + "@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setAddress("Test Address");
    user.setPhone("+56912345678");
    user.setEnabled(true);
    user.setRole("ROLE_USER");
    
    return user;
}

    @Test
    @Order(1)
    @DisplayName("1. El contexto de Spring Boot se carga correctamente")
    void contextLoads() {
        System.out.println("Cargando el contexto de Spring Boot para las pruebas...");
        System.out.println("El servidor de pruebas está corriendo en el puerto: " + port);
        assertThat(restTemplate).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("2. Verificar que la base de datos está vacía inicialmente")
    void databaseIsEmptyInitially() {
        assertThat(userRepository.count()).isEqualTo(0);
        
        ResponseEntity<List<Usuario>> response = restTemplate.exchange(
            "/api/usuarios",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Usuario>>() {}
        );
        
        assertThat(response.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Se pueden crear al menos 10 registros de usuarios")
    void shouldCreateMultipleUsers() {
        for (int i = 0; i < 10; i++) {
            Usuario newUser = new Usuario();
            newUser.setUsername("testuser_" + i);
            newUser.setPassword("ValidPass123!");
            newUser.setEmail("test" + i + "@example.com");
            newUser.setFirstName("Test");
            newUser.setLastName("User");
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/usuarios",
                newUser,
                Map.class
            );
            
            System.out.println("Intento " + (i+1) + " - Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
            
            assertThat(response.getStatusCode())
                .as("Falló al crear usuario: " + newUser.getUsername())
                .isEqualTo(HttpStatus.CREATED);
        }
        
        // Verificación final directa en repositorio
        assertThat(userRepository.count()).isEqualTo(10);
    }

    @Test
    @Order(4)
    @DisplayName("4. Se puede obtener un usuario por ID")
    void shouldReturnUserById() {
        // Configurar datos de prueba
        Usuario testUser = new Usuario();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        
        // Guardar usuario
        Usuario savedUser = userRepository.save(testUser);
        
        // Hacer petición GET
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/usuarios/" + savedUser.getId(),
            Map.class
        );
        
        // Verificar código de estado primero
        assertThat(response.getStatusCode())
            .as("El código de estado debería ser 200 OK")
            .isEqualTo(HttpStatus.OK);
        
        // Verificar cuerpo de respuesta
        assertThat(response.getBody())
            .as("El cuerpo de la respuesta no debe ser nulo")
            .isNotNull();
        
        // Verificar contenido
        assertThat(response.getBody().get("id"))
            .as("El ID del usuario no coincide")
            .isEqualTo(savedUser.getId().toString()); 
        
        assertThat(response.getBody().get("username"))
            .as("El username no coincide")
            .isEqualTo("testuser");
    }

    @Test
    @Order(5)
    @DisplayName("5. Retorna 404 Not Found si el usuario no existe")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        ResponseEntity<Usuario> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/usuarios/99999",
                Usuario.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    @DisplayName("6. Se pueden modificar 3 registros en columnas diferentes")
    void shouldUpdateThreeUsers() {
        Usuario user1 = userService.createUser(createRandomUser());
        Usuario user2 = userService.createUser(createRandomUser());
        Usuario user3 = userService.createUser(createRandomUser());

        String updatedUsername1 = "updatedUser1_" + System.currentTimeMillis();
        user1.setUsername(updatedUsername1);
        user1.setEmail("updated1@example.com");
        restTemplate.put(
                "http://localhost:" + port + "/api/usuarios/" + user1.getId(),
                user1
        );
        Usuario fetchedUser1 = userService.getUserById(user1.getId()).orElseThrow();
        assertThat(fetchedUser1.getUsername()).isEqualTo(updatedUsername1);
        assertThat(fetchedUser1.getEmail()).isEqualTo("updated1@example.com");

        String updatedFirstName2 = "UpdatedFN2";
        user2.setFirstName(updatedFirstName2);
        user2.setLastName("UpdatedLN2");
        user2.setPhone("987-654-3210");
        restTemplate.put(
                "http://localhost:" + port + "/api/usuarios/" + user2.getId(),
                user2
        );
        Usuario fetchedUser2 = userService.getUserById(user2.getId()).orElseThrow();
        assertThat(fetchedUser2.getFirstName()).isEqualTo(updatedFirstName2);
        assertThat(fetchedUser2.getLastName()).isEqualTo("UpdatedLN2");
        assertThat(fetchedUser2.getPhone()).isEqualTo("987-654-3210");

        user3.setAddress("New Address 3");
        user3.setRole("ROLE_ADMIN");
        user3.setEnabled(false);
        restTemplate.put(
                "http://localhost:" + port + "/api/usuarios/" + user3.getId(),
                user3
        );
        Usuario fetchedUser3 = userService.getUserById(user3.getId()).orElseThrow();
        assertThat(fetchedUser3.getAddress()).isEqualTo("New Address 3");
        assertThat(fetchedUser3.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(fetchedUser3.isEnabled()).isFalse();
    }

    @Test
    @Order(7)
    @DisplayName("7. Se pueden eliminar al menos 3 registros diferentes")
    void shouldDeleteThreeUsers() {
        Usuario userToDelete1 = userService.createUser(createRandomUser());
        Usuario userToDelete2 = userService.createUser(createRandomUser());
        Usuario userToDelete3 = userService.createUser(createRandomUser());

        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete1.getId());
        assertThat(userService.getUserById(userToDelete1.getId())).isEmpty();

        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete2.getId());
        assertThat(userService.getUserById(userToDelete2.getId())).isEmpty();

        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete3.getId());
        assertThat(userService.getUserById(userToDelete3.getId())).isEmpty();

        ResponseEntity<?> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios/99999",
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(8)
    @DisplayName("8. Verificar endpoints con parámetros de consulta (filtros)")
    void shouldFilterUsersByQueryParams() {
        // Limpiar datos
        userRepository.deleteAll();

        // 1. Probar caso sin resultados (debe devolver 204)
        ResponseEntity<List<Map<String, Object>>> responseNoContent = restTemplate.exchange(
                "/api/usuarios?username=nonexistentUser",
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        assertThat(responseNoContent.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        // 2. Crear usuario de prueba
        Usuario testUser = new Usuario();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        userService.createUser(testUser);

        // 3. Probar filtro que devuelve resultados (debe devolver 200)
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/usuarios?username=testuser",
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(1);
    }
}