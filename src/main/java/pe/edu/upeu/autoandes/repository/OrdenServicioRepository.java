package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.entity.OrdenServicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {

    boolean existsByVehiculoId(Long vehiculoId);

    Optional<OrdenServicio> findTopByVehiculoIdAndEstadoOrderByFechaDescIdDesc(Long vehiculoId, String estado);

    @Query("SELECT o FROM OrdenServicio o JOIN FETCH o.vehiculo v JOIN FETCH v.cliente c " +
           "WHERE (:placa IS NULL OR UPPER(v.placa) LIKE UPPER(CONCAT('%', :placa, '%'))) " +
           "  AND (:estado IS NULL OR o.estado = :estado) " +
           "  AND (CAST(:desde AS timestamp) IS NULL OR o.fecha >= :desde) " +
           "  AND (CAST(:hasta AS timestamp) IS NULL OR o.fecha <= :hasta) " +
           "ORDER BY o.fecha DESC")
    List<OrdenServicio> buscarConFiltros(
            @Param("placa") String placa,
            @Param("estado") String estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("SELECT new pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO(" +
           "  s.codigo, " +
           "  s.nombre, " +
           "  SUM(d.cantidad), " +
           "  SUM(d.subtotal)" +
           ") " +
           "FROM DetalleOrden d " +
           "JOIN d.servicio s " +
           "JOIN d.orden o " +
           "WHERE o.estado <> 'ANULADA' " +
           "  AND (CAST(:desde AS timestamp) IS NULL OR o.fecha >= :desde) " +
           "  AND (CAST(:hasta AS timestamp) IS NULL OR o.fecha <= :hasta) " +
           "GROUP BY s.codigo, s.nombre " +
           "ORDER BY SUM(d.subtotal) DESC")
    List<IngresoPorServicioDTO> reporteIngresosPorServicio(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("SELECT MAX(o.id) FROM OrdenServicio o")
    Long obtenerMaxId();
}
