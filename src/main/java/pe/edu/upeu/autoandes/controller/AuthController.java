package pe.edu.upeu.autoandes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.Permiso;
import pe.edu.upeu.autoandes.security.PermisosPorRol;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        String usernameLimpio = request.getUsername().trim();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameIgnoreCase(usernameLimpio);

        if (usuarioOpt.isEmpty() ||
            !passwordEncoder.matches(request.getPassword(), usuarioOpt.get().getPassword()) ||
            Boolean.FALSE.equals(usuarioOpt.get().getActivo())) {

            log.warn("Intento de login fallido para el usuario '{}'", usernameLimpio);
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .mensaje("Credenciales inválidas")
                    .ruta(httpRequest.getRequestURI())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Usuario usuario = usuarioOpt.get();
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());

        Set<Permiso> permisosSet = PermisosPorRol.obtenerPermisosCombinados(roles);
        List<String> permisos = permisosSet.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String token = jwtService.generarToken(usuario.getUsername(), usuario.getNombrecompleto(), roles, permisos);

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEnSegundos(jwtService.getExpiracionSegundos())
                .username(usuario.getUsername())
                .nombrecompleto(usuario.getNombrecompleto())
                .roles(roles)
                .permisos(permisos)
                .build();

        log.info("Login exitoso: Usuario={}, Roles={}", usuario.getUsername(), roles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("Unauthorized")
                            .mensaje("Debe iniciar sesión para acceder a este recurso")
                            .ruta(httpRequest.getRequestURI())
                            .build()
            );
        }

        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponseDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.NOT_FOUND.value())
                            .error("Not Found")
                            .mensaje("Usuario no encontrado en la base de datos")
                            .ruta(httpRequest.getRequestURI())
                            .build()
            );
        }

        UsuarioResponseDTO response = AutoAndesMapper.toUsuarioResponse(usuario);
        return ResponseEntity.ok(response);
    }
}
