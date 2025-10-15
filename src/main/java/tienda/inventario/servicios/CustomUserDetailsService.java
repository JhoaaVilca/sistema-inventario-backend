package tienda.inventario.servicios;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import tienda.inventario.modelo.Usuario;
import tienda.inventario.repositorio.UsuarioRepositorio;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public CustomUserDetailsService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepositorio.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo"));

        // Spring Security espera ROLE_ prefijo
        String role = "ROLE_" + (user.getRol() == null ? "USER" : user.getRol().toUpperCase());
        return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority(role)));
    }
}
