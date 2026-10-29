package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<String> rolesNombres = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());

        Set<Permiso> permisos = PermisosPorRol.obtenerPermisosCombinados(rolesNombres);

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Permiso p : permisos) {
            authorities.add(new SimpleGrantedAuthority(p.name()));
        }
        for (String r : rolesNombres) {
            String roleName = r.startsWith("ROLE_") ? r : "ROLE_" + r;
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!usuario.getActivo())
                .authorities(authorities)
                .build();
    }
}
