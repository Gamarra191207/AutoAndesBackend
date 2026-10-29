package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.servicio.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.servicio.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.service.ServicioService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private static final Logger log = LoggerFactory.getLogger(ServicioServiceImpl.class);

    private final ServicioRepository servicioRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listarTodos() {
        return servicioRepository.findAll().stream()
                .map(AutoAndesMapper::toServicioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listarActivos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(AutoAndesMapper::toServicioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponseDTO buscarPorId(Long id) {
        return servicioRepository.findById(id)
                .map(AutoAndesMapper::toServicioResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));
    }

    @Override
    public ServicioResponseDTO crear(ServicioRequestDTO dto) {
        String codigoNorm = dto.getCodigo().trim().toUpperCase();
        if (servicioRepository.existsByCodigo(codigoNorm)) {
            log.warn("RN-01 rechazada: El codigo de servicio '{}' ya se encuentra registrado", codigoNorm);
            throw new ReglaNegocioException("El código de servicio ya existe: " + codigoNorm);
        }

        Servicio servicio = AutoAndesMapper.toServicioEntity(dto);
        Servicio guardado = servicioRepository.save(servicio);

        log.info("Alta de servicio: ID={}, Codigo={}, Nombre={}", guardado.getId(), guardado.getCodigo(), guardado.getNombre());
        return AutoAndesMapper.toServicioResponse(guardado);
    }

    @Override
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));

        String codigoNorm = dto.getCodigo().trim().toUpperCase();
        if (servicioRepository.existsByCodigoAndIdNot(codigoNorm, id)) {
            log.warn("RN-01 rechazada: El codigo de servicio '{}' ya pertenece a otro servicio", codigoNorm);
            throw new ReglaNegocioException("El código de servicio ya existe: " + codigoNorm);
        }

        servicio.setCodigo(codigoNorm);
        servicio.setNombre(dto.getNombre().trim());
        servicio.setPrecio(dto.getPrecio());
        servicio.setDuracionMin(dto.getDuracionMin());
        if (dto.getActivo() != null) {
            servicio.setActivo(dto.getActivo());
        }

        Servicio actualizado = servicioRepository.save(servicio);
        log.info("Cambio de servicio: ID={}, Codigo={}, Nombre={}", actualizado.getId(), actualizado.getCodigo(), actualizado.getNombre());
        return AutoAndesMapper.toServicioResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));

        if (detalleOrdenRepository.existsByServicioId(id)) {
            log.warn("RN-01 rechazada: Intento de eliminar fisicamente el servicio ID={}, pero ya esta en uso en ordenes", id);
            throw new ReglaNegocioException("El servicio ya ha sido utilizado en órdenes y no se puede eliminar físicamente. Use PATCH /api/v1/servicios/" + id + "/estado para darlo de baja.");
        }

        servicioRepository.delete(servicio);
        log.info("Baja fisica de servicio: ID={}, Codigo={}", id, servicio.getCodigo());
    }

    @Override
    public ServicioResponseDTO cambiarEstado(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));

        servicio.setActivo(!servicio.getActivo());
        Servicio actualizado = servicioRepository.save(servicio);

        log.info("Cambio de estado de servicio: ID={}, Activo={}", actualizado.getId(), actualizado.getActivo());
        return AutoAndesMapper.toServicioResponse(actualizado);
    }
}
