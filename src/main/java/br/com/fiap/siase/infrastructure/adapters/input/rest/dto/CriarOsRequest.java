package br.com.fiap.siase.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarOsRequest(
        @NotNull UUID clienteId,
        @NotBlank String veiculo,
        String descricaoProblema
) {
}
