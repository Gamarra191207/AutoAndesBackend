package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.servicio.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.servicio.ServicioResponseDTO;
import pe.edu.upeu.autoandes.service.ServicioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> listarServicios(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos
    ) {
        if (soloActivos) {
            return ResponseEntity.ok(servicioService.listarActivos());
        }
        return ResponseEntity.ok(servicioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> obtenerServicio(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ServicioResponseDTO> crearServicio(@Valid @RequestBody ServicioRequestDTO request) {
        ServicioResponseDTO creado = servicioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequestDTO request
    ) {
        ServicioResponseDTO actualizado = servicioService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ServicioResponseDTO> cambiarEstadoServicio(@PathVariable Long id) {
        ServicioResponseDTO actualizado = servicioService.cambiarEstado(id);
        return ResponseEntity.ok(actualizado);
    }
}
