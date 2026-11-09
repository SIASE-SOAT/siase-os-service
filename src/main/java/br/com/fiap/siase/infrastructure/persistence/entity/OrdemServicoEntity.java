package br.com.fiap.siase.infrastructure.persistence.entity;

import br.com.fiap.siase.domain.enums.StatusOS;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ordens_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Column(name = "veiculo_placa", nullable = false)
    private String veiculoPlaca;

    @Column(name = "veiculo_modelo")
    private String veiculoModelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "total_servicos", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalServicos = BigDecimal.ZERO;

    @Column(name = "total_pecas", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPecas = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ItemServicoEntity> itensServico = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ItemPecaEntity> itensPeca = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
