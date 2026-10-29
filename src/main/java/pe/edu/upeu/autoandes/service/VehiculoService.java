package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoResponseDTO;

import java.util.List;

public interface VehiculoService {
    List<VehiculoResponseDTO> listarTodos(Long clienteId);
    VehiculoResponseDTO buscarPorId(Long id);
    VehiculoResponseDTO crear(VehiculoRequestDTO dto);
    VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO dto);
    void eliminar(Long id);
}
