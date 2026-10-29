package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documento", nullable = false, unique = true, length = 11)
    private String documento;

    @Column(name = "nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "telefono", length = 15)
    private String telefono;
}
