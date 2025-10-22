package Distribuidos_GrupoO.ServidorGRPC.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Donaciones ONG")
                        .version("1.0")
                        .description("API para gestión de donaciones"));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openSwagger() {
        System.out.println("=== SWAGGER AUTO-OPEN INICIADO ===");
        try {
            Thread.sleep(3000); // Esperar que el servidor esté listo
            
            // Intentar con Desktop primero
            if (Desktop.isDesktopSupported()) {
                System.out.println("Usando Desktop.browse()...");
                Desktop.getDesktop().browse(URI.create("http://localhost:8080/swagger-ui.html"));
                System.out.println("🚀 Swagger UI abierto con Desktop");
                return;
            }
            
            // Fallback: usar comando del sistema (Windows)
            System.out.println("Desktop no soportado, usando comando del sistema...");
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: usar start
                new ProcessBuilder("cmd", "/c", "start", "http://localhost:8080/swagger-ui.html").start();
                System.out.println("🚀 Swagger UI abierto con comando Windows");
            } else if (os.contains("mac")) {
                // Mac: usar open
                new ProcessBuilder("open", "http://localhost:8080/swagger-ui.html").start();
                System.out.println("🚀 Swagger UI abierto con comando Mac");
            } else {
                // Linux: usar xdg-open
                new ProcessBuilder("xdg-open", "http://localhost:8080/swagger-ui.html").start();
                System.out.println("🚀 Swagger UI abierto con comando Linux");
            }
            
        } catch (Exception e) {
            System.out.println("❗ ERROR al abrir automáticamente: " + e.getMessage());
            System.out.println("📋 Swagger UI disponible manualmente en: http://localhost:8080/swagger-ui.html");
        }
        System.out.println("=== SWAGGER AUTO-OPEN TERMINADO ===");
    }
}
