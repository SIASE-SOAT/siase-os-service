package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import br.com.fiap.siase.domain.model.StatusOs;
import jakarta.persistence.*;

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

  @Column(nullable = false)
  private String veiculo;

  @Column(columnDefinition = "text")
  private String descricaoProblema;

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
  public String getVeiculo() { return veiculo; }
  public void setVeiculo(String veiculo) { this.veiculo = veiculo; }
  public String getDescricaoProblema() { return descricaoProblema; }
  public void setDescricaoProblema(String d) { this.descricaoProblema = d; }
  public StatusOs getStatus() { return status; }
  public void setStatus(StatusOs status) { this.status = status; }
  public Instant getCriadoEm() { return criadoEm; }
  public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
  public Instant getAtualizadoEm() { return atualizadoEm; }
  public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
