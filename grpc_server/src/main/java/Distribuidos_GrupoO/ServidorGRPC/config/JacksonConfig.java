package Distribuidos_GrupoO.ServidorGRPC.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        // Configure to force lazy loading to be serialized as null instead of failing
        module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
        return module;
    }
}
