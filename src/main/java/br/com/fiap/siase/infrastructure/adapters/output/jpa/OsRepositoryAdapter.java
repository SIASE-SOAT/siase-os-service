package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OsRepositoryAdapter implements OsRepositoryPort {

  private static final TypeReference<List<ItemServico>> SERVICOS = new TypeReference<>() {};
  private static final TypeReference<List<ItemPeca>> PECAS = new TypeReference<>() {};

  private final OsJpaRepository jpa;
  private final ObjectMapper mapper;

  public OsRepositoryAdapter(OsJpaRepository jpa, ObjectMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
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
    e.setClienteNome(os.getClienteNome());
    e.setVeiculo(os.getVeiculo());
    e.setDescricaoProblema(os.getDescricaoProblema());
    e.setValorOrcamento(os.getValorOrcamento());
    e.setItensServicoJson(escreverJson(os.getItensServico()));
    e.setItensPecaJson(escreverJson(os.getItensPeca()));
    e.setStatus(os.getStatus());
    e.setCriadoEm(os.getCriadoEm());
    e.setAtualizadoEm(os.getAtualizadoEm());
    return e;
  }

  private OrdemDeServico paraDominio(OsJpaEntity e) {
    return new OrdemDeServico(e.getId(), e.getNumero(), e.getClienteId(), e.getClienteNome(),
            e.getVeiculo(), e.getDescricaoProblema(), e.getValorOrcamento(),
            lerJson(e.getItensServicoJson(), SERVICOS), lerJson(e.getItensPecaJson(), PECAS),
            e.getStatus(), e.getCriadoEm(), e.getAtualizadoEm());
  }

  private String escreverJson(Object valor) {
    try {
      return mapper.writeValueAsString(valor);
    } catch (Exception e) {
      throw new IllegalStateException("Falha ao serializar itens da OS", e);
    }
  }

  private <T> T lerJson(String json, TypeReference<T> tipo) {
    try {
      return json == null || json.isBlank() ? mapper.readValue("[]", tipo)
              : mapper.readValue(json, tipo);
    } catch (Exception e) {
      throw new IllegalStateException("Falha ao ler itens da OS", e);
    }
  }
}
