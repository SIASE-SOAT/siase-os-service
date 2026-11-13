package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OsJpaRepository extends JpaRepository<OsJpaEntity, UUID> {

  Optional<OsJpaEntity> findByNumero(String numero);

  List<OsJpaEntity> findByClienteId(UUID clienteId);
}
