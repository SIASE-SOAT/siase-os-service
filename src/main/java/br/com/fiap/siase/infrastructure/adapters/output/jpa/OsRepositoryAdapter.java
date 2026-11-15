package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OsRepositoryAdapter implements OsRepositoryPort {

  private final OsJpaRepository jpa;

  public OsRepositoryAdapter(OsJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public OrdemDeServico salvar(OrdemDeServico os) {
    return paraDominio(jpa.save(paraEntidade(os)));
  }

  @Override
  public Optional<OrdemDeServico> buscarPorId(UUID id) {
    return jpa.findById(id).map(this::paraDominio);
  }

  @Override
  public Optional<OrdemDeServico> buscarPorNumero(String numero) {
    return jpa.findByNumero(numero).map(this::paraDominio);
  }

  @Override
  public List<OrdemDeServico> listar() {
    return jpa.findAll().stream().map(this::paraDominio).toList();
  }

  @Override
  public List<OrdemDeServico> listarPorCliente(UUID clienteId) {
    return jpa.findByClienteId(clienteId).stream().map(this::paraDominio).toList();
  }

  private OsJpaEntity paraEntidade(OrdemDeServico os) {
    OsJpaEntity e = new OsJpaEntity();
    e.setId(os.getId());
    e.setNumero(os.getNumero());
    e.setClienteId(os.getClienteId());
    e.setVeiculo(os.getVeiculo());
    e.setDescricaoProblema(os.getDescricaoProblema());
    e.setStatus(os.getStatus());
    e.setCriadoEm(os.getCriadoEm());
    e.setAtualizadoEm(os.getAtualizadoEm());
    return e;
  }

  private OrdemDeServico paraDominio(OsJpaEntity e) {
    return new OrdemDeServico(e.getId(), e.getNumero(), e.getClienteId(), e.getVeiculo(),
            e.getDescricaoProblema(), e.getStatus(), e.getCriadoEm(), e.getAtualizadoEm());
  }
}
