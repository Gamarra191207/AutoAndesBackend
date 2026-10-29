package pe.edu.upeu.autoandes.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoPorServicioDTO {

    private String codigo;
    private String servicio;
    private Long cantidadTotal;
    private BigDecimal importeTotal;
}
