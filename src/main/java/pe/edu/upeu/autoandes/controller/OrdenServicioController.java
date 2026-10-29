package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

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
        OrdenServicioResponseDTO anulada = ordenServicioService.anularOrden(id, request);
        return ResponseEntity.ok(anulada);
    }
}
