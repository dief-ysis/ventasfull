package ventas.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioTestApplication {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnListOfUsersUsingMockMvc() throws Exception {
        this.mockMvc.perform(get("/api/usuarios")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("[")));
    }
}