package br.com.fiap.siase.application.dto.output;

import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OsResponse(
        UUID id,
        String numero,
        UUID clienteId,
        String clienteNome,
        UUID veiculoId,
        String veiculoPlaca,
        String veiculoModelo,
        String status,
        String statusDescricao,
        String observacoes,
        List<ItemServicoResponse> itensServico,
        List<ItemPecaResponse> itensPeca,
        BigDecimal totalServicos,
        BigDecimal totalPecas,
        BigDecimal total,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public record ItemServicoResponse(UUID id, UUID servicoId, String servicoNome,
                                      BigDecimal precoUnitario, Integer tempoEstimadoMinutos,
                                      String observacoes, LocalDateTime dataInicioExecucao,
                                      LocalDateTime dataFimExecucao) {
        public static ItemServicoResponse from(ItemServico i) {
            return new ItemServicoResponse(i.getId(), i.getServicoId(), i.getServicoNome(),
                    i.getPrecoUnitario(), i.getTempoEstimadoMinutos(), i.getObservacoes(),
                    i.getDataInicioExecucao(), i.getDataFimExecucao());
        }
    }

    public record ItemPecaResponse(UUID id, UUID pecaId, String pecaCodigo, String pecaNome,
                                   Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        public static ItemPecaResponse from(ItemPeca i) {
            return new ItemPecaResponse(i.getId(), i.getPecaId(), i.getPecaCodigo(),
                    i.getPecaNome(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public static OsResponse from(OrdemDeServico os) {
        return new OsResponse(
                os.getId(), os.getNumero(), os.getClienteId(), os.getClienteNome(),
                os.getVeiculoId(), os.getVeiculoPlaca(), os.getVeiculoModelo(),
                os.getStatus().name(), os.getStatus().getDescricao(), os.getObservacoes(),
                os.getItensServico().stream().map(ItemServicoResponse::from).toList(),
                os.getItensPeca().stream().map(ItemPecaResponse::from).toList(),
                os.getTotalServicos(), os.getTotalPecas(), os.getTotal(),
                os.getDataAbertura(), os.getDataFechamento(), os.getCriadoEm(), os.getAtualizadoEm()
        );
    }
}
