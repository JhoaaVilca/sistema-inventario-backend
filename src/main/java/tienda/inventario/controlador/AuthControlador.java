package tienda.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tienda.inventario.dto.LoginRequest;
import tienda.inventario.dto.LoginResponse;
import tienda.inventario.modelo.Usuario;
import tienda.inventario.repositorio.UsuarioRepositorio;
import tienda.inventario.seguridad.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepositorio usuarioRepositorio;

    public AuthControlador(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsuarioRepositorio usuarioRepositorio) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepositorio.findByUsernameAndActivoTrue(userDetails.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(userDetails.getUsername(), usuario.getRol());
        return ResponseEntity.ok(new LoginResponse(token, usuario.getUsername(), usuario.getRol()));
    }
}
