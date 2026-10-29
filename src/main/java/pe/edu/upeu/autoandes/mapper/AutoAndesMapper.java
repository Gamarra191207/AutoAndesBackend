package pe.edu.upeu.autoandes.mapper;

import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.dto.cliente.ClienteResponseDTO;
import pe.edu.upeu.autoandes.dto.orden.DetalleOrdenResponseDTO;
import pe.edu.upeu.autoandes.dto.orden.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.dto.servicio.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.servicio.ServicioResponseDTO;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.vehiculo.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.security.Permiso;
import pe.edu.upeu.autoandes.security.PermisosPorRol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class AutoAndesMapper {

    private AutoAndesMapper() {}

    public static ClienteResponseDTO toClienteResponse(Cliente c) {
        if (c == null) return null;
        return ClienteResponseDTO.builder()
                .id(c.getId())
                .documento(c.getDocumento())
                .nombres(c.getNombres())
                .telefono(c.getTelefono())
                .build();
    }

    public static Servicio toServicioEntity(ServicioRequestDTO dto) {
        if (dto == null) return null;
        return Servicio.builder()
                .codigo(dto.getCodigo().trim().toUpperCase())
                .nombre(dto.getNombre().trim())
                .precio(dto.getPrecio())
                .duracionMin(dto.getDuracionMin())
                .activo(dto.getActivo() == null || dto.getActivo())
                .build();
    }

    public static ServicioResponseDTO toServicioResponse(Servicio s) {
        if (s == null) return null;
        return ServicioResponseDTO.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .precio(s.getPrecio())
                .duracionMin(s.getDuracionMin())
                .activo(s.getActivo())
                .build();
    }

    public static Vehiculo toVehiculoEntity(VehiculoRequestDTO dto, Cliente cliente) {
        if (dto == null) return null;
        return Vehiculo.builder()
                .placa(dto.getPlaca().trim().toUpperCase())
                .marca(dto.getMarca().trim())
                .modelo(dto.getModelo().trim())
                .anio(dto.getAnio())
                .cliente(cliente)
                .build();
    }

    public static VehiculoResponseDTO toVehiculoResponse(Vehiculo v) {
        if (v == null) return null;
        return VehiculoResponseDTO.builder()
                .id(v.getId())
                .placa(v.getPlaca())
                .marca(v.getMarca())
                .modelo(v.getModelo())
                .anio(v.getAnio())
                .clienteId(v.getCliente() != null ? v.getCliente().getId() : null)
                .clienteNombre(v.getCliente() != null ? v.getCliente().getNombres() : null)
                .build();
    }

    public static DetalleOrdenResponseDTO toDetalleOrdenResponse(DetalleOrden d) {
        if (d == null) return null;
        return DetalleOrdenResponseDTO.builder()
                .id(d.getId())
                .servicioId(d.getServicio() != null ? d.getServicio().getId() : null)
                .servicioCodigo(d.getServicio() != null ? d.getServicio().getCodigo() : null)
                .servicioNombre(d.getServicio() != null ? d.getServicio().getNombre() : null)
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .subtotal(d.getSubtotal())
                .build();
    }

    public static OrdenServicioResponseDTO toOrdenServicioResponse(OrdenServicio o) {
        if (o == null) return null;
        List<DetalleOrdenResponseDTO> detallesDto = new ArrayList<>();
        if (o.getDetalles() != null) {
            detallesDto = o.getDetalles().stream()
                    .map(AutoAndesMapper::toDetalleOrdenResponse)
                    .collect(Collectors.toList());
        }

        Vehiculo v = o.getVehiculo();
        Cliente c = v != null ? v.getCliente() : null;

        return OrdenServicioResponseDTO.builder()
                .id(o.getId())
                .numero(o.getNumero())
                .fecha(o.getFecha())
                .vehiculoId(v != null ? v.getId() : null)
                .vehiculoPlaca(v != null ? v.getPlaca() : null)
                .vehiculoModelo(v != null ? (v.getMarca() + " " + v.getModelo()) : null)
                .clienteId(c != null ? c.getId() : null)
                .clienteNombre(c != null ? c.getNombres() : null)
                .kilometraje(o.getKilometraje())
                .estado(o.getEstado())
                .total(o.getTotal())
                .usuarioRegistro(o.getUsuarioRegistro())
                .motivoAnulacion(o.getMotivoAnulacion())
                .detalles(detallesDto)
                .build();
    }

    public static UsuarioResponseDTO toUsuarioResponse(Usuario u) {
        if (u == null) return null;
        List<String> rolesStr = u.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());

        Set<Permiso> permisosSet = PermisosPorRol.obtenerPermisosCombinados(rolesStr);
        List<String> permisosStr = permisosSet.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombrecompleto(u.getNombrecompleto())
                .activo(u.getActivo())
                .roles(rolesStr)
                .permisos(permisosStr)
                .build();
    }
}
