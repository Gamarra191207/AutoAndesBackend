package pe.edu.upeu.autoandes.dto.orden;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenServicioRequestDTO {

    @NotNull(message = "El vehiculoId es obligatorio")
    private Long vehiculoId;

    @NotNull(message = "El kilometraje es obligatorio")
    @PositiveOrZero(message = "El kilometraje debe ser mayor o igual a cero")
    private Integer kilometraje;

    @NotEmpty(message = "La orden debe contener al menos un detalle")
    @Size(min = 1, max = 10, message = "Una orden debe tener entre 1 y 10 detalles")
    @Valid
    private List<DetalleOrdenRequestDTO> detalles;
}
