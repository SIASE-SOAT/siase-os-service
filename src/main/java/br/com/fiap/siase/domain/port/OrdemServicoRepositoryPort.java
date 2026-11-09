package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.model.OrdemDeServico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepositoryPort {
    OrdemDeServico save(OrdemDeServico os);
    Optional<OrdemDeServico> findById(UUID id);
    Optional<OrdemDeServico> findByNumero(String numero);
    List<OrdemDeServico> findAll();
    List<OrdemDeServico> findByStatus(StatusOS status);
    List<OrdemDeServico> findByClienteId(UUID clienteId);
    boolean existsByVeiculoIdAndStatusNotIn(UUID veiculoId, List<StatusOS> statusExcluidos);
}
