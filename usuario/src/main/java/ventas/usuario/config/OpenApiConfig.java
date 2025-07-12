package ventas.usuario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Usuarios - API")
                        .version("1.0")
                        .description("Documentación de la API para el microservicio de gestión de usuarios. " +
                                "Incluye endpoints para operaciones CRUD y filtrado de usuarios.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("desarrollo@ventas.com")
                                .url("https://ventas.com/dev"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                        .termsOfService("https://ventas.com/terms"))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("Documentación adicional")
                        .url("https://ventas.com/docs"));
    }
}