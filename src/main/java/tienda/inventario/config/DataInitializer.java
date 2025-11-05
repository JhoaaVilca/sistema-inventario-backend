package tienda.inventario.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import tienda.inventario.modelo.Usuario;
import tienda.inventario.repositorio.UsuarioRepositorio;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UsuarioRepositorio repo, PasswordEncoder encoder) {
        return args -> {
            String username = "tiendayamisa";
            if (!repo.existsByUsername(username)) {
                Usuario admin = new Usuario();
                admin.setUsername(username);
                admin.setPassword(encoder.encode("123456"));
                admin.setRol("ADMIN");
                admin.setActivo(true);
                repo.save(admin);
            }
        };
    }
}
