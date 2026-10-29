package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.orden.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.service.OrdenServicioService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    @PostMapping
    public ResponseEntity<OrdenServicioResponseDTO> registrarOrden(@Valid @RequestBody OrdenServicioRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null && auth.getName() != null) ? auth.getName() : "asesor";

        OrdenServicioResponseDTO creada = ordenServicioService.registrarOrden(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<OrdenServicioResponseDTO>> listarOrdenes(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeDateTime = parseFechaInicio(desde);
        LocalDateTime hastaDateTime = parseFechaFin(hasta);

        List<OrdenServicioResponseDTO> resultado = ordenServicioService.listarConFiltros(placa, estado, desdeDateTime, hastaDateTime);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenServicioResponseDTO> obtenerOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenServicioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<OrdenServicioResponseDTO> anularOrden(
            @PathVariable Long id,
            @Valid @RequestBody AnularOrdenRequestDTO request
    ) {
        return ResponseEntity.ok(ordenServicioService.anularOrden(id, request));
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
