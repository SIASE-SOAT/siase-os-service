package br.com.fiap.siase.infrastructure.adapters.input.rest.dto;

import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OsResponse(
        UUID id,
        String numero,
        UUID clienteId,
        String clienteNome,
        String veiculo,
        String descricaoProblema,
        BigDecimal valorOrcamento,
        List<ItemServico> servicos,
        List<ItemPeca> pecas,
        StatusOs status,
        Instant criadoEm,
        Instant atualizadoEm
) {
  public static OsResponse de(OrdemDeServico os) {
    return new OsResponse(os.getId(), os.getNumero(), os.getClienteId(), os.getClienteNome(),
            os.getVeiculo(), os.getDescricaoProblema(), os.getValorOrcamento(),
            os.getItensServico(), os.getItensPeca(), os.getStatus(),
            os.getCriadoEm(), os.getAtualizadoEm());
  }
}

