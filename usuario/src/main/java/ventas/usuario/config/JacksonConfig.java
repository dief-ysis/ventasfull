package ventas.usuario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            builder.serializerByType(Long.class, ToStringSerializer.instance);
        };
    }
}