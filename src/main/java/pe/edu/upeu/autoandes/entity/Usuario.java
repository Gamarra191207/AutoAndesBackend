package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "nombrecompleto", nullable = false, length = 100)
    private String nombrecompleto;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarioroles",
            joinColumns = @JoinColumn(name = "usuarioid"),
            inverseJoinColumns = @JoinColumn(name = "rolid")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();
}
