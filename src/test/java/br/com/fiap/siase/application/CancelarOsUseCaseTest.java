package br.com.fiap.siase.application;

import br.com.fiap.siase.application.CriarOsUseCaseTest.*;
import br.com.fiap.siase.application.usecase.CancelarOsUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CancelarOsUseCaseTest {

  @Test
  void cancelarPublicaCompensacaoParaBillingEExecution() {
    var repo = new RepoMemoria();
    var pub = new PublicadorMemoria();
    var uc = new CancelarOsUseCase(repo, pub, new HistoricoMemoria());
    OrdemDeServico os = repo.salvar(OrdemDeServico.criar(UUID.randomUUID(), "X", "y"));

    uc.executar(os.getId());

    assertEquals(StatusOs.CANCELADA, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
    assertEquals(List.of(EventoOs.OS_CANCELADA), pub.billing);
    assertEquals(List.of(EventoOs.OS_CANCELADA), pub.execution);
    assertEquals(List.of(EventoOs.OS_CANCELADA), pub.dominio);
  }
}
