package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.cliente.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO buscarPorId(Long id);
}
