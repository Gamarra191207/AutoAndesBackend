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
public class LoginResponseDTO {

    private String token;
    @Builder.Default
    private String tipo = "Bearer";
    private Long expiraEnSegundos;
    private String username;
    private String nombrecompleto;
    private List<String> roles;
    private List<String> permisos;
}
