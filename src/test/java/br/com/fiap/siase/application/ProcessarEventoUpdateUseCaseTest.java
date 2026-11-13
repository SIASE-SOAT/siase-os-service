package br.com.fiap.siase.application;

import br.com.fiap.siase.application.CriarOsUseCaseTest.*;
import br.com.fiap.siase.application.usecase.ProcessarEventoUpdateUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessarEventoUpdateUseCaseTest {

  @Test
  void orcamentoGeradoAvancaParaAguardandoAprovacao() {
    var repo = new RepoMemoria();
    var hist = new HistoricoMemoria();
    var uc = new ProcessarEventoUpdateUseCase(repo, hist, new SimpleMeterRegistry());
    OrdemDeServico os = repo.salvar(OrdemDeServico.criar(UUID.randomUUID(), "X", "y"));

    uc.executar(new EventoOs(EventoOs.ORCAMENTO_GERADO, os.getId(), os.getNumero(), null, Instant.now()));

    assertEquals(StatusOs.AGUARDANDO_APROVACAO, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
    assertEquals(1, hist.historicoDaOs(os.getId()).size());
  }

  @Test
  void pagamentoConfirmadoEntregaOsFinalizada() {
    var repo = new RepoMemoria();
    var uc = new ProcessarEventoUpdateUseCase(repo, new HistoricoMemoria(), new SimpleMeterRegistry());
    OrdemDeServico os = repo.salvar(OrdemDeServico.criar(UUID.randomUUID(), "X", "y"));
    os.transicionarPara(StatusOs.AGUARDANDO_APROVACAO);
    os.transicionarPara(StatusOs.APROVADA);
    os.transicionarPara(StatusOs.EM_EXECUCAO);
    os.transicionarPara(StatusOs.FINALIZADA);
    repo.salvar(os);

    uc.executar(new EventoOs(EventoOs.PAGAMENTO_CONFIRMADO, os.getId(), os.getNumero(), null, Instant.now()));

    assertEquals(StatusOs.ENTREGUE, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
  }
}
