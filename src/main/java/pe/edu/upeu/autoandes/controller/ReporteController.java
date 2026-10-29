package pe.edu.upeu.autoandes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.service.ReporteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ingresos-por-servicio")
    public ResponseEntity<List<IngresoPorServicioDTO>> ingresosPorServicio(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeDateTime = parseFechaInicio(desde);
        LocalDateTime hastaDateTime = parseFechaFin(hasta);

        List<IngresoPorServicioDTO> reporte = reporteService.obtenerIngresosPorServicio(desdeDateTime, hastaDateTime);
        return ResponseEntity.ok(reporte);
    }

    private LocalDateTime parseFechaInicio(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return null;
        if (fechaStr.contains("T")) return LocalDateTime.parse(fechaStr);
        return LocalDate.parse(fechaStr).atStartOfDay();
    }

    private LocalDateTime parseFechaFin(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return null;
        if (fechaStr.contains("T")) return LocalDateTime.parse(fechaStr);
        return LocalDate.parse(fechaStr).atTime(LocalTime.MAX);
    }
}
