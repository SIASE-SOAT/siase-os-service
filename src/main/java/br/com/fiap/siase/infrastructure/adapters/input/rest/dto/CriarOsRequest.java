package br.com.fiap.siase.infrastructure.adapters.input.rest.dto;

import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CriarOsRequest(
        @NotNull UUID clienteId,
        String clienteNome,
        @NotBlank String veiculo,
        String descricaoProblema,
        @DecimalMin(value = "0.01", message = "valorOrcamento deve ser maior que zero")
        BigDecimal valorOrcamento,
        @Valid List<ItemServicoRequest> servicos,
        @Valid List<ItemPecaRequest> pecas
) {
        public List<ItemServico> servicosDominio() {
                return servicos == null ? List.of()
                        : servicos.stream().map(s -> new ItemServico(s.descricao(), s.preco())).toList();
        }

        public List<ItemPeca> pecasDominio() {
                return pecas == null ? List.of()
                        : pecas.stream().map(p -> new ItemPeca(p.nome(), p.quantidade(), p.precoUnitario())).toList();
        }

        public record ItemServicoRequest(
                @NotBlank String descricao,
                @DecimalMin(value = "0.00") BigDecimal preco
        ) {
        }

        public record ItemPecaRequest(
                @NotBlank String nome,
                @Min(1) int quantidade,
                @DecimalMin(value = "0.00") BigDecimal precoUnitario
        ) {
        }
}
