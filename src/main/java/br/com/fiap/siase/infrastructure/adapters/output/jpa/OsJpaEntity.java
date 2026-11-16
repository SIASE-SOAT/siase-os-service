package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import br.com.fiap.siase.domain.model.StatusOs;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ordens_servico")
public class OsJpaEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String numero;

  @Column(nullable = false)
  private UUID clienteId;

  @Column
  private String clienteNome;

  @Column(nullable = false)
  private String veiculo;

  @Column(columnDefinition = "text")
  private String descricaoProblema;

  @Column(precision = 12, scale = 2)
  private BigDecimal valorOrcamento;

  @Column(columnDefinition = "text")
  private String itensServicoJson;

  @Column(columnDefinition = "text")
  private String itensPecaJson;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusOs status;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getNumero() { return numero; }
  public void setNumero(String numero) { this.numero = numero; }
  public UUID getClienteId() { return clienteId; }
  public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
  public String getClienteNome() { return clienteNome; }
  public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
  public String getVeiculo() { return veiculo; }
  public void setVeiculo(String veiculo) { this.veiculo = veiculo; }
  public String getDescricaoProblema() { return descricaoProblema; }
  public void setDescricaoProblema(String d) { this.descricaoProblema = d; }
  public BigDecimal getValorOrcamento() { return valorOrcamento; }
  public void setValorOrcamento(BigDecimal valorOrcamento) { this.valorOrcamento = valorOrcamento; }
  public String getItensServicoJson() { return itensServicoJson; }
  public void setItensServicoJson(String json) { this.itensServicoJson = json; }
  public String getItensPecaJson() { return itensPecaJson; }
  public void setItensPecaJson(String json) { this.itensPecaJson = json; }
  public StatusOs getStatus() { return status; }
  public void setStatus(StatusOs status) { this.status = status; }
  public Instant getCriadoEm() { return criadoEm; }
  public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
  public Instant getAtualizadoEm() { return atualizadoEm; }
  public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}


