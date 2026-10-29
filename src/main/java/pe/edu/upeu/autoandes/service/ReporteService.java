package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteService {
    List<IngresoPorServicioDTO> obtenerIngresosPorServicio(LocalDateTime desde, LocalDateTime hasta);
}
