package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.CriarOsUseCaseTest.HistoricoMemoria;
import br.com.fiap.siase.application.CriarOsUseCaseTest.PublicadorMemoria;
import br.com.fiap.siase.application.CriarOsUseCaseTest.RepoMemoria;
import br.com.fiap.siase.application.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.siase.application.usecase.CancelarOsUseCase;
import br.com.fiap.siase.application.usecase.CriarOsUseCase;
import br.com.fiap.siase.application.usecase.ProcessarEventoUpdateUseCase;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import io.cucumber.java.pt.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD do fluxo completo do Saga no os-service (nível de use case,
 * com adapters em memória — sem AWS).
 */
public class SagaOsSteps {

  private final RepoMemoria repo = new RepoMemoria();
  private final PublicadorMemoria pub = new PublicadorMemoria();
  private final HistoricoMemoria hist = new HistoricoMemoria();
  private OrdemDeServico os;

  private CriarOsUseCase criar() {
    return new CriarOsUseCase(repo, pub, hist, new SimpleMeterRegistry());
  }

  private ProcessarEventoUpdateUseCase processar() {
    return new ProcessarEventoUpdateUseCase(repo, hist, new SimpleMeterRegistry());
  }

  private AprovarOrcamentoUseCase aprovar() {
    return new AprovarOrcamentoUseCase(repo, pub, hist);
  }

  private CancelarOsUseCase cancelar() {
    return new CancelarOsUseCase(repo, pub, hist);
  }

  @Dado("uma OS criada para o cliente")
  public void criarOs() {
    os = criar().executar(UUID.randomUUID(), "ABC-1234", "Barulho no motor");
    assertNotNull(os.getId());
  }

  @Dado("uma OS aguardando aprovação de orçamento")
  public void osAguardando() {
    criarOs();
    eventoChega(EventoOs.ORCAMENTO_GERADO);
    assertEquals(StatusOs.AGUARDANDO_APROVACAO, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
  }

  @Quando("o billing publica {string}")
  @Quando("o execution publica {string}")
  public void eventoChega(String tipo) {
    processar().executar(new EventoOs(tipo, os.getId(), os.getNumero(), null, Instant.now()));
  }

  @Quando("o cliente aprova o orçamento")
  public void clienteAprova() {
    aprovar().aprovar(os.getNumero());
  }

  @Quando("o cliente recusa o orçamento")
  public void clienteRecusa() {
    aprovar().recusar(os.getNumero());
  }

  @Quando("a OS é cancelada")
  public void osCancelada() {
    cancelar().executar(os.getId());
  }

  @Então("a OS fica com status {string}")
  public void statusEsperado(String status) {
    assertEquals(StatusOs.valueOf(status), repo.buscarPorId(os.getId()).orElseThrow().getStatus());
  }

  @Então("o evento {string} é publicado para o billing")
  public void publicadoParaBilling(String tipo) {
    assertTrue(pub.billing.contains(tipo), "esperava " + tipo + " em billing: " + pub.billing);
  }

  @Então("o evento {string} é publicado para o execution")
  public void publicadoParaExecution(String tipo) {
    assertTrue(pub.execution.contains(tipo), "esperava " + tipo + " em execution: " + pub.execution);
  }

  @Então("o histórico da OS contém todos os eventos do fluxo")
  public void historicoCompleto() {
    List<String> tipos = hist.historicoDaOs(os.getId()).stream().map(EventoOs::tipoEvento).toList();
    assertTrue(tipos.containsAll(List.of(
                    EventoOs.OS_CRIADA, EventoOs.ORCAMENTO_GERADO, EventoOs.ORCAMENTO_APROVADO,
                    EventoOs.EXECUCAO_INICIADA, EventoOs.EXECUCAO_FINALIZADA, EventoOs.PAGAMENTO_CONFIRMADO)),
            "histórico incompleto: " + tipos);
  }
}
