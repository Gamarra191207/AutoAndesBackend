package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.service.VehiculoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculos(
            @RequestParam(required = false) Long clienteId
    ) {
        return ResponseEntity.ok(vehiculoService.listarTodos(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> obtenerVehiculo(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> crearVehiculo(@Valid @RequestBody VehiculoRequestDTO request) {
        VehiculoResponseDTO creado = vehiculoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> actualizarVehiculo(
            @PathVariable Long id,
            @Valid @RequestBody VehiculoRequestDTO request
    ) {
        VehiculoResponseDTO actualizado = vehiculoService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
