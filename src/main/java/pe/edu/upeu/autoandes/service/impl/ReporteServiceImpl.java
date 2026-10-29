package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.service.ReporteService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final OrdenServicioRepository ordenServicioRepository;

    @Override
    public List<IngresoPorServicioDTO> obtenerIngresosPorServicio(LocalDateTime desde, LocalDateTime hasta) {
        return ordenServicioRepository.reporteIngresosPorServicio(desde, hasta);
    }
}
