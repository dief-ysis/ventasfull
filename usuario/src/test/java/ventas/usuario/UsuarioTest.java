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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
    @Order(1)
    @DisplayName("1. El contexto de Spring Boot se carga correctamente")
    void contextLoads() {
        System.out.println("Cargando el contexto de Spring Boot para las pruebas...");
        System.out.println("El servidor de pruebas está corriendo en el puerto: " + port);
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void databaseIsEmptyInitially() {
        ResponseEntity<Void> response = restTemplate.exchange(
        "/api/usuarios",
        HttpMethod.GET,
        null,
        Void.class 
    );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(response.hasBody()).isFalse();
        assertThat(response.getBody()).isNull();
    }

    @Test
    @Order(3)
    @DisplayName("3. Se pueden crear al menos 10 registros de usuarios")
    void shouldCreateMultipleUsers() {
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
        Usuario savedUser = userService.createUser(createRandomUser());

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
        userService.createUser(createRandomUser());
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

        ResponseEntity<List<Usuario>> responseUsername = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?username=" + userByUsername.getUsername(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseUsername.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseUsername.getBody()).isNotNull();
        assertThat(responseUsername.getBody()).hasSize(1);
        assertThat(responseUsername.getBody().get(0).getUsername()).isEqualTo(userByUsername.getUsername());

        ResponseEntity<List<Usuario>> responseEmail = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?email=example.com",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseEmail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEmail.getBody()).isNotNull();
        assertThat(responseEmail.getBody().stream().anyMatch(u -> u.getEmail().contains("example.com"))).isTrue();

        ResponseEntity<List<Usuario>> responseRole = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?role=ROLE_ADMIN",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseRole.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseRole.getBody()).isNotNull();
        assertThat(responseRole.getBody().stream().allMatch(u -> "ROLE_ADMIN".equals(u.getRole()))).isTrue();
        assertThat(responseRole.getBody().stream().anyMatch(u -> u.getId().equals(userByRoleAdmin.getId()))).isTrue();

        ResponseEntity<List<Usuario>> responseFullName = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?fullName=John Doe",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseFullName.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseFullName.getBody()).isNotNull();
        assertThat(responseFullName.getBody()).hasSize(1);
        assertThat(responseFullName.getBody().get(0).getFirstName()).isEqualTo("John");
        assertThat(responseFullName.getBody().get(0).getLastName()).isEqualTo("Doe");

        ResponseEntity<List<Usuario>> responseNoContent = restTemplate.exchange(
                "http://localhost:" + port + "/api/usuarios?username=nonexistentUser",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {}
        );
        assertThat(responseNoContent.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}