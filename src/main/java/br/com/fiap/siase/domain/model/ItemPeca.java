package br.com.fiap.siase.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPeca {
    private UUID id;
    private UUID pecaId;
    private String pecaCodigo;
    private String pecaNome;
    private Integer quantidade;
    private BigDecimal precoUnitario;

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
