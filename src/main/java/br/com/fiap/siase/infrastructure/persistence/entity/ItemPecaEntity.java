package br.com.fiap.siase.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "itens_peca")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServicoEntity ordemServico;

    @Column(name = "peca_id", nullable = false)
    private UUID pecaId;

    @Column(name = "peca_codigo", nullable = false)
    private String pecaCodigo;

    @Column(name = "peca_nome", nullable = false)
    private String pecaNome;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;
}
