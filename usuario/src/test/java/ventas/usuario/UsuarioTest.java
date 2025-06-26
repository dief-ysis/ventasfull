package ventas.usuario;

import ventas.usuario.controller.UsuarioController;
import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository; // Importar el repositorio para limpiar la BD
import ventas.usuario.service.UserService; // Importar el servicio de usuario
import net.datafaker.Faker; // Importar DataFaker
import org.junit.jupiter.api.BeforeEach; // Para limpiar la BD antes de cada test
import org.junit.jupiter.api.DisplayName; // Para nombres de test más claros
import org.junit.jupiter.api.MethodOrderer; // Para ordenar tests
import org.junit.jupiter.api.Order; // Para ordenar tests
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder; // Para ordenar tests
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference; // Para obtener listas de ResponseEntity
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpMethod; // Para PUT y DELETE
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles; // Para activar el perfil 'test'

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger; // Para generar usernames únicos
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Activa el perfil 'test' que usa H2 en memoria
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Permite ordenar los tests
public class UsuarioTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository; // Para limpiar la base de datos

    @Autowired
    private UserService userService; // Inyectar el servicio de usuario

    private static Faker faker;
    private static Random random;
    private static AtomicInteger usernameCounter = new AtomicInteger(1); // Para usernames únicos

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setup() {
        // Vaciar la base de datos antes de cada test para asegurar un estado limpio
        userRepository.deleteAll();
        // Resetear el contador de usernames para cada ejecución
        usernameCounter.set(1);
        if (faker == null) { // Inicializar Faker y Random una sola vez
            faker = new Faker();
            random = new Random();
        }
    }

    private Usuario createRandomUser() {
        Usuario user = new Usuario();
        // Aseguramos que el username sea único para evitar DataIntegrityViolationException
        user.setUsername("testuser_" + System.currentTimeMillis() + "_" + usernameCounter.getAndIncrement());
        user.setPassword(faker.internet().password());
        user.setEmail(faker.internet().emailAddress());
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setAddress(faker.address().fullAddress());
        user.setPhone(faker.phoneNumber().phoneNumber());
        user.setEnabled(random.nextBoolean());
        user.setRole(random.nextBoolean() ? "ROLE_ADMIN" : "ROLE_USER");
        return user;
    }

    @Test
    @Order(1) // Ejecutar primero para asegurar que el contexto carga
    @DisplayName("1. El contexto de Spring Boot se carga correctamente")
    void contextLoads() {
        System.out.println("Cargando el contexto de Spring Boot para las pruebas...");
        System.out.println("El servidor de pruebas está corriendo en el puerto: " + port);
        // No inyectamos UsuarioController directamente aquí para evitar un fallo si el controlador tiene problemas
        // La prueba real es que el contexto y sus beans principales (como TestRestTemplate) estén disponibles.
        assertThat(restTemplate).isNotNull();
    }

    @Test
void databaseIsEmptyInitially() {
    // ... (código existente del test) ...

    // Antes: Probablemente tenías algo como esto, que causa el error:
    // ResponseEntity<List<Usuario>> response = testRestTemplate.exchange("/api/usuarios", HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {});

    // Después: Cambia la forma en que esperas la respuesta
    ResponseEntity<CollectionModel<EntityModel<Usuario>>> response = restTemplate.exchange(
        "/api/usuarios",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<CollectionModel<EntityModel<Usuario>>>() {}
    );

    // Verifica que la respuesta sea 200 OK
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Extrae la lista de usuarios del CollectionModel de HATEOAS
    CollectionModel<EntityModel<Usuario>> userModels = response.getBody();
    assertThat(userModels).isNotNull(); // Asegúrate de que no sea nulo

    // Convierte los EntityModel<Usuario> a objetos Usuario si necesitas la lista pura
    List<Usuario> usuarios = userModels.getContent().stream()
                                    .map(EntityModel::getContent)
                                    .collect(Collectors.toList());

    // Ahora puedes verificar que la lista esté vacía
        assertThat(usuarios).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("3. Se pueden crear al menos 10 registros de usuarios")
    void shouldCreateMultipleUsers() {
        // Crear 10 usuarios
        for (int i = 0; i < 10; i++) {
            Usuario newUser = createRandomUser();
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

        // Verificar que los 10 registros están en la BD (método GET)
        ResponseEntity<List<Usuario>> responseList = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Usuario>>() {}
        );

        assertThat(responseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseList.getBody()).isNotNull();
        assertThat(responseList.getBody().size()).isEqualTo(10);
    }

    @Test
    @Order(4)
    @DisplayName("4. Se puede obtener un usuario por ID")
    void shouldReturnUserById() {
        Usuario savedUser = userService.createUser(createRandomUser()); // Crear un usuario directamente vía servicio

        ResponseEntity<Usuario> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/usuarios/" + savedUser.getId(),
                Usuario.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getUsername()).isEqualTo(savedUser.getUsername());
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
        // Crear 3 usuarios para modificar
        Usuario user1 = userService.createUser(createRandomUser());
        Usuario user2 = userService.createUser(createRandomUser());
        Usuario user3 = userService.createUser(createRandomUser());

        // Modificar usuario 1: username y email
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

        // Modificar usuario 2: firstName, lastName y phone
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

        // Modificar usuario 3: address y role, enabled
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
        // Crear 3 usuarios para eliminar
        Usuario userToDelete1 = userService.createUser(createRandomUser());
        Usuario userToDelete2 = userService.createUser(createRandomUser());
        Usuario userToDelete3 = userService.createUser(createRandomUser());

        // Eliminar usuario 1
        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete1.getId());
        assertThat(userService.getUserById(userToDelete1.getId())).isEmpty();

        // Eliminar usuario 2
        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete2.getId());
        assertThat(userService.getUserById(userToDelete2.getId())).isEmpty();

        // Eliminar usuario 3
        restTemplate.delete("http://localhost:" + port + "/api/usuarios/" + userToDelete3.getId());
        assertThat(userService.getUserById(userToDelete3.getId())).isEmpty();

        // Verificar que al intentar borrar un usuario que no existe devuelve 404
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
        // Crear usuarios específicos para filtrar
        userService.createUser(createRandomUser()); // Otro usuario base
        Usuario userByUsername = createRandomUser();
        userByUsername.setUsername("uniqueUsernameFilter");
        userService.createUser(userByUsername);

        Usuario userByEmail = createRandomUser();
        userByEmail.setEmail("filter@example.com");
        userService.createUser(userByEmail);

        Usuario userByRoleAdmin = createRandomUser();
        userByRoleAdmin.setRole("ROLE_ADMIN");
        userService.createUser(userByRoleAdmin);

        Usuario userByFullName = createRandomUser();
        userByFullName.setFirstName("John");
        userByFullName.setLastName("Doe");
        userService.createUser(userByFullName);

        // Test 1: Filtrar por username (exacto)
        ResponseEntity<List<Usuario>> responseUsername = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?username=" + userByUsername.getUsername(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseUsername.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseUsername.getBody()).isNotNull();
        assertThat(responseUsername.getBody()).hasSize(1);
        assertThat(responseUsername.getBody().get(0).getUsername()).isEqualTo(userByUsername.getUsername());

        // Test 2: Filtrar por email (containing, case-insensitive)
        ResponseEntity<List<Usuario>> responseEmail = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?email=example.com", // Buscamos por parte del email
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseEmail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEmail.getBody()).isNotNull();
        assertThat(responseEmail.getBody().stream().anyMatch(u -> u.getEmail().contains("example.com"))).isTrue();

        // Test 3: Filtrar por rol
        ResponseEntity<List<Usuario>> responseRole = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?role=ROLE_ADMIN",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseRole.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseRole.getBody()).isNotNull();
        assertThat(responseRole.getBody().stream().allMatch(u -> "ROLE_ADMIN".equals(u.getRole()))).isTrue();
        assertThat(responseRole.getBody().stream().anyMatch(u -> u.getId().equals(userByRoleAdmin.getId()))).isTrue();

        // Test 4: Filtrar por nombre completo
        ResponseEntity<List<Usuario>> responseFullName = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?fullName=John Doe",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseFullName.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseFullName.getBody()).isNotNull();
        assertThat(responseFullName.getBody()).hasSize(1);
        assertThat(responseFullName.getBody().get(0).getFirstName()).isEqualTo("John");
        assertThat(responseFullName.getBody().get(0).getLastName()).isEqualTo("Doe");

        // Test 5: No se encuentran resultados de filtro (204 No Content)
        ResponseEntity<List<Usuario>> responseNoContent = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?username=nonexistentUser",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseNoContent.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}