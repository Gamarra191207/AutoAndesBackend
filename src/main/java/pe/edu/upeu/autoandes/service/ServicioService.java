package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.servicio.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.servicio.ServicioResponseDTO;

import java.util.List;

public interface ServicioService {
    List<ServicioResponseDTO> listarTodos();
    List<ServicioResponseDTO> listarActivos();
    ServicioResponseDTO buscarPorId(Long id);
    ServicioResponseDTO crear(ServicioRequestDTO dto);
    ServicioResponseDTO actualizar(Long id, ServicioRequestDTO dto);
    void eliminar(Long id);
    ServicioResponseDTO cambiarEstado(Long id);
}
