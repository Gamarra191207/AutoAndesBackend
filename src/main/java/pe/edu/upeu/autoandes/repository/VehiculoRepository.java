package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.autoandes.entity.Vehiculo;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
    List<Vehiculo> findByClienteId(Long clienteId);

    @Query("SELECT v FROM Vehiculo v JOIN FETCH v.cliente WHERE (:clienteId IS NULL OR v.cliente.id = :clienteId) ORDER BY v.placa ASC")
    List<Vehiculo> buscarConCliente(@Param("clienteId") Long clienteId);
}
