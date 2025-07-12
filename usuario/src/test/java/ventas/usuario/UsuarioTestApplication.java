package ventas.usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioTestApplication {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnListOfUsersUsingMockMvc() throws Exception {
        // Insertar datos de prueba
        Usuario testUser = new Usuario();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepository.save(testUser);

        // Realizar la prueba con estructura simplificada
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("testuser")) // Lista directa
            .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }
}