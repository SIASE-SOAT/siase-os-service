package br.com.fiap.siase.infrastructure.adapters.input.rest.dto;

import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;

import java.time.Instant;
import java.util.UUID;

public record OsResponse(
        UUID id,
        String numero,
        UUID clienteId,
        String veiculo,
        String descricaoProblema,
        StatusOs status,
        Instant criadoEm,
        Instant atualizadoEm
) {
  public static OsResponse de(OrdemDeServico os) {
    return new OsResponse(os.getId(), os.getNumero(), os.getClienteId(), os.getVeiculo(),
            os.getDescricaoProblema(), os.getStatus(), os.getCriadoEm(), os.getAtualizadoEm());
  }
}
