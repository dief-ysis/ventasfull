package ventas.usuario;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    @Test
    void createUser_ShouldReturnCreated_WhenValidInput() throws Exception {
        Usuario user = new Usuario();
        user.setUsername("testuser_" + System.currentTimeMillis()); // Hacer único
        user.setPassword("ValidPass123!");
        user.setEmail("test_" + System.currentTimeMillis() + "@example.com"); // Hacer único
        user.setFirstName("Test");
        user.setLastName("User");
        
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenPasswordTooShort() throws Exception {
        Usuario user = new Usuario();
        user.setUsername("testuser");
        user.setPassword("short");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        MvcResult result = mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        // Verifica que el cuerpo de la respuesta no esté vacío
        String content = result.getResponse().getContentAsString();
        assertThat(content).isNotBlank();
        
        // Verifica el mensaje de error
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("Password")));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/usuarios/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }
}