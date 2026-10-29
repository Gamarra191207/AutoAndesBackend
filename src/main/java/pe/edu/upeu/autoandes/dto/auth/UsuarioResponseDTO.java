package pe.edu.upeu.autoandes.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String nombrecompleto;
    private Boolean activo;
    private List<String> roles;
    private List<String> permisos;
}
