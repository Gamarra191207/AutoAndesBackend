package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.VehiculoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculoServiceImpl implements VehiculoService {

    private static final Logger log = LoggerFactory.getLogger(VehiculoServiceImpl.class);

    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdenServicioRepository ordenServicioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarTodos(Long clienteId) {
        return vehiculoRepository.buscarConCliente(clienteId).stream()
                .map(AutoAndesMapper::toVehiculoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorId(Long id) {
        return vehiculoRepository.findById(id)
                .map(AutoAndesMapper::toVehiculoResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));
    }

    @Override
    public VehiculoResponseDTO crear(VehiculoRequestDTO dto) {
        String placaNorm = dto.getPlaca().trim().toUpperCase();

        // RN-02: La placa es única (409)
        if (vehiculoRepository.existsByPlaca(placaNorm)) {
            log.warn("RN-02 rechazada: La placa '{}' ya se encuentra registrada", placaNorm);
            throw new ReglaNegocioException("La placa ya se encuentra registrada: " + placaNorm);
        }

        // RN-02: El cliente debe existir (404)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + dto.getClienteId()));

        Vehiculo vehiculo = AutoAndesMapper.toVehiculoEntity(dto, cliente);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        log.info("Alta de vehículo: ID={}, Placa={}, Cliente={}", guardado.getId(), guardado.getPlaca(), cliente.getNombres());
        return AutoAndesMapper.toVehiculoResponse(guardado);
    }

    @Override
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));

        String placaNorm = dto.getPlaca().trim().toUpperCase();

        // RN-02: La placa es única (409)
        if (vehiculoRepository.existsByPlacaAndIdNot(placaNorm, id)) {
            log.warn("RN-02 rechazada: La placa '{}' ya pertenece a otro vehículo", placaNorm);
            throw new ReglaNegocioException("La placa ya se encuentra registrada: " + placaNorm);
        }

        // RN-02: El cliente debe existir (404)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + dto.getClienteId()));

        vehiculo.setPlaca(placaNorm);
        vehiculo.setMarca(dto.getMarca().trim());
        vehiculo.setModelo(dto.getModelo().trim());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setCliente(cliente);

        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        log.info("Cambio de vehículo: ID={}, Placa={}", actualizado.getId(), actualizado.getPlaca());
        return AutoAndesMapper.toVehiculoResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));

        // RN-02: Un vehículo con órdenes registradas no se puede eliminar (409)
        if (ordenServicioRepository.existsByVehiculoId(id)) {
            log.warn("RN-02 rechazada: Intento de eliminar vehículo ID={} con órdenes asociadas", id);
            throw new ReglaNegocioException("El vehículo tiene órdenes de servicio registradas y no se puede eliminar.");
        }

        vehiculoRepository.delete(vehiculo);
        log.info("Baja de vehículo: ID={}, Placa={}", id, vehiculo.getPlaca());
    }
}
