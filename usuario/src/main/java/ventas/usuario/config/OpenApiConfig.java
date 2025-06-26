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
                        .description("Documentación de la API para el microservicio de gestión de usuarios.")
                        .contact(new Contact()
                                .name("Equipo de Soporte de Ventas")
                                .email("soporte.ventas@example.com")
                                .url("http://www.ejemplo-soporte-ventas.com/"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}