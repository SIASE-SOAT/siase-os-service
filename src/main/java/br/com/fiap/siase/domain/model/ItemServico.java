package br.com.fiap.siase.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemServico {
    private UUID id;
    private UUID servicoId;
    private String servicoNome;
    private BigDecimal precoUnitario;
    private Integer tempoEstimadoMinutos;
    private String observacoes;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
}
