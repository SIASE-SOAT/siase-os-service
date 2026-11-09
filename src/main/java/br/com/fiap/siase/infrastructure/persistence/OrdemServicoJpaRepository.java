package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.infrastructure.persistence.entity.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {
    Optional<OrdemServicoEntity> findByNumero(String numero);
    List<OrdemServicoEntity> findByStatus(StatusOS status);
    List<OrdemServicoEntity> findByClienteId(UUID clienteId);
    boolean existsByVeiculoIdAndStatusNotIn(UUID veiculoId, List<StatusOS> status);
}
