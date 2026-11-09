package br.com.fiap.siase.application.dto.input;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CriarOsRequest(
        @NotNull UUID clienteId,
        @NotNull String clienteNome,
        @NotNull String clienteEmail,
        @NotNull UUID veiculoId,
        @NotNull String veiculoPlaca,
        String veiculoModelo,
        String observacoes,
        List<ItemServicoRequest> itensServico,
        List<ItemPecaRequest> itensPeca
) {
    public record ItemServicoRequest(
            @NotNull UUID servicoId,
            @NotNull String servicoNome,
            @NotNull BigDecimal preco,
            Integer tempoEstimadoMinutos,
            String observacoes
    ) {}

    public record ItemPecaRequest(
            @NotNull UUID pecaId,
            @NotNull String pecaCodigo,
            @NotNull String pecaNome,
            @NotNull Integer quantidade,
            @NotNull BigDecimal preco
    ) {}
}
