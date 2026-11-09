package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;
import br.com.fiap.siase.infrastructure.persistence.mapper.OrdemServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepositoryPort {

    private final OrdemServicoJpaRepository jpaRepository;
    private final OrdemServicoMapper mapper;

    @Override
    public OrdemDeServico save(OrdemDeServico os) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(os)));
    }

    @Override
    public Optional<OrdemDeServico> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrdemDeServico> findByNumero(String numero) {
        return jpaRepository.findByNumero(numero).map(mapper::toDomain);
    }

    @Override
    public List<OrdemDeServico> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrdemDeServico> findByStatus(StatusOS status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrdemDeServico> findByClienteId(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByVeiculoIdAndStatusNotIn(UUID veiculoId, List<StatusOS> statusExcluidos) {
        return jpaRepository.existsByVeiculoIdAndStatusNotIn(veiculoId, statusExcluidos);
    }
}
