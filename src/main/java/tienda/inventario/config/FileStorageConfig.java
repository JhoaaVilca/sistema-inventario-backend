package tienda.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar el manejo de archivos estáticos para las facturas
        String uploadDir = Paths.get("uploads/facturas").toAbsolutePath().toString();
        
        registry.addResourceHandler("/uploads/facturas/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
