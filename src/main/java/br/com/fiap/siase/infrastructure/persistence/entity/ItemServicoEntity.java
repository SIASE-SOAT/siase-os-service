package br.com.fiap.siase.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "itens_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServicoEntity ordemServico;

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(name = "servico_nome", nullable = false)
    private String servicoNome;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "tempo_estimado_minutos")
    private Integer tempoEstimadoMinutos;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "data_fim_execucao")
    private LocalDateTime dataFimExecucao;
}
