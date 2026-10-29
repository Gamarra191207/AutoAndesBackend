package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.orden.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.DetalleOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.OrdenServicioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenServicioServiceImpl implements OrdenServicioService {

    private static final Logger log = LoggerFactory.getLogger(OrdenServicioServiceImpl.class);

    private final OrdenServicioRepository ordenServicioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ServicioRepository servicioRepository;

    @Override
    public OrdenServicioResponseDTO registrarOrden(OrdenServicioRequestDTO dto, String usuarioRegistro) {
        // Vehiculo debe existir
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + dto.getVehiculoId()));

        // RN-03: Validar que no se repita el mismo servicio en multiples lineas (400)
        Set<Long> serviciosVistos = new HashSet<>();
        for (DetalleOrdenRequestDTO d : dto.getDetalles()) {
            if (!serviciosVistos.add(d.getServicioId())) {
                log.warn("RN-03 rechazada: Servicio ID={} duplicado en las lineas de la orden", d.getServicioId());
                throw new IllegalArgumentException("No se permite repetir el mismo servicio en múltiples líneas de la orden.");
            }
        }

        // RN-04: El kilometraje no puede ser menor que el de la ultima orden REGISTRADA del mismo vehiculo (409)
        Optional<OrdenServicio> ultimaOrdenOpt = ordenServicioRepository
                .findTopByVehiculoIdAndEstadoOrderByFechaDescIdDesc(vehiculo.getId(), "REGISTRADA");

        if (ultimaOrdenOpt.isPresent()) {
            OrdenServicio ultimaOrden = ultimaOrdenOpt.get();
            if (dto.getKilometraje() < ultimaOrden.getKilometraje()) {
                log.warn("RN-04 rechazada: Kilometraje {} menor al previo {} para vehiculo {}",
                        dto.getKilometraje(), ultimaOrden.getKilometraje(), vehiculo.getPlaca());
                throw new ReglaNegocioException(
                        "El kilometraje no puede ser menor que el de la última orden registrada (" +
                        ultimaOrden.getKilometraje() + " km) del vehículo " + vehiculo.getPlaca()
                );
            }
        }

        // Generar correlativo OS-000001
        Long maxId = ordenServicioRepository.obtenerMaxId();
        long siguiente = (maxId != null ? maxId : 0) + 1;
        String numeroCorrelativo = String.format("OS-%06d", siguiente);

        OrdenServicio orden = OrdenServicio.builder()
                .numero(numeroCorrelativo)
                .fecha(LocalDateTime.now())
                .vehiculo(vehiculo)
                .kilometraje(dto.getKilometraje())
                .estado("REGISTRADA")
                .total(BigDecimal.ZERO)
                .usuarioRegistro(usuarioRegistro != null ? usuarioRegistro : "sistema")
                .detalles(new ArrayList<>())
                .build();

        BigDecimal sumaTotal = BigDecimal.ZERO;

        for (DetalleOrdenRequestDTO detDto : dto.getDetalles()) {
            Servicio servicio = servicioRepository.findById(detDto.getServicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + detDto.getServicioId()));

            // RN-03: Todos los servicios deben estar activos (409)
            if (Boolean.FALSE.equals(servicio.getActivo())) {
                log.warn("RN-03 rechazada: Servicio '{}' ({}) inactivo", servicio.getNombre(), servicio.getCodigo());
                throw new ReglaNegocioException(
                        "El servicio '" + servicio.getNombre() + "' (" + servicio.getCodigo() + ") se encuentra inactivo."
                );
            }

            BigDecimal precioUnitario = servicio.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detDto.getCantidad()));
            sumaTotal = sumaTotal.add(subtotal);

            DetalleOrden detalle = DetalleOrden.builder()
                    .servicio(servicio)
                    .cantidad(detDto.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build();

            orden.agregarDetalle(detalle);
        }

        orden.setTotal(sumaTotal);

        OrdenServicio guardada = ordenServicioRepository.save(orden);
        log.info("Alta de Orden de Servicio: ID={}, Numero={}, Total={}, Lineas={}",
                guardada.getId(), guardada.getNumero(), guardada.getTotal(), guardada.getDetalles().size());

        return AutoAndesMapper.toOrdenServicioResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicioResponseDTO> listarConFiltros(String placa, String estado, LocalDateTime desde, LocalDateTime hasta) {
        String placaFiltrada = (placa != null && !placa.trim().isEmpty()) ? placa.trim() : null;
        String estadoFiltrado = (estado != null && !estado.trim().isEmpty()) ? estado.trim().toUpperCase() : null;

        List<OrdenServicio> ordenes = ordenServicioRepository.buscarConFiltros(placaFiltrada, estadoFiltrado, desde, hasta);
        return ordenes.stream()
                .map(AutoAndesMapper::toOrdenServicioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenServicioResponseDTO buscarPorId(Long id) {
        return ordenServicioRepository.findById(id)
                .map(AutoAndesMapper::toOrdenServicioResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio no encontrada con id: " + id));
    }

    @Override
    public OrdenServicioResponseDTO anularOrden(Long id, AnularOrdenRequestDTO dto) {
        OrdenServicio orden = ordenServicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio no encontrada con id: " + id));

        // RN-05: Solo se anula una orden REGISTRADA
        if (!"REGISTRADA".equalsIgnoreCase(orden.getEstado())) {
            log.warn("RN-05 rechazada: Intento de anular orden ID={} que ya esta en estado {}", id, orden.getEstado());
            throw new ReglaNegocioException("Solo se puede anular una orden que se encuentre en estado REGISTRADA.");
        }

        // RN-05: y en el mismo dia de su registro
        LocalDate hoy = LocalDate.now();
        LocalDate fechaRegistro = orden.getFecha().toLocalDate();
        if (!fechaRegistro.isEqual(hoy)) {
            log.warn("RN-05 rechazada: La orden ID={} fue registrada el {} y hoy es {}", id, fechaRegistro, hoy);
            throw new ReglaNegocioException(
                    "Solo se puede anular una orden en el mismo día de su registro (registrada el " + fechaRegistro + ")."
            );
        }

        orden.setEstado("ANULADA");
        orden.setMotivoAnulacion(dto.getMotivo().trim());

        OrdenServicio anulada = ordenServicioRepository.save(orden);
        log.info("Orden de Servicio ANULADA: ID={}, Numero={}, Motivo={}",
                anulada.getId(), anulada.getNumero(), anulada.getMotivoAnulacion());

        return AutoAndesMapper.toOrdenServicioResponse(anulada);
    }
}
