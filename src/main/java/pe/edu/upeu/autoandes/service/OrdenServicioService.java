package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.orden.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdenServicioService {
    OrdenServicioResponseDTO registrarOrden(OrdenServicioRequestDTO dto, String usuarioRegistro);
    List<OrdenServicioResponseDTO> listarConFiltros(String placa, String estado, LocalDateTime desde, LocalDateTime hasta);
    OrdenServicioResponseDTO buscarPorId(Long id);
    OrdenServicioResponseDTO anularOrden(Long id, AnularOrdenRequestDTO dto);
}
