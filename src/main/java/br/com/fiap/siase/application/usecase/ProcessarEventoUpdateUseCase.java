package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer da fila os-updates: aplica na OS os eventos publicados por
 * billing e execution (Saga coreografado — nenhum serviço escreve no
 * banco de outro; a atualização vem sempre por evento).
 */
@Service
public class ProcessarEventoUpdateUseCase {

  private static final Logger log = LoggerFactory.getLogger(ProcessarEventoUpdateUseCase.class);

  private final OsRepositoryPort repository;
  private final HistoricoEventoPort historico;
  private final MeterRegistry metrics;

  public ProcessarEventoUpdateUseCase(OsRepositoryPort repository,
                                      HistoricoEventoPort historico,
                                      MeterRegistry metrics) {
    this.repository = repository;
    this.historico = historico;
    this.metrics = metrics;
  }

  @Transactional
  public void executar(EventoOs evento) {
    OrdemDeServico os = repository.buscarPorId(evento.osId())
            .orElseThrow(() -> new OsNaoEncontradaException(evento.osId().toString()));

    StatusOs destino = mapearStatus(evento.tipoEvento());
    if (destino != null) {
      os.transicionarPara(destino);
      repository.salvar(os);
    }

    historico.registrar(evento);
    metrics.counter("siase_saga_eventos_total",
            "servico", "os", "evento", evento.tipoEvento(), "direcao", "consumido").increment();
    log.info("Evento {} aplicado na OS {} -> {}", evento.tipoEvento(), evento.numeroOs(), os.getStatus());
  }

  private StatusOs mapearStatus(String tipoEvento) {
    return switch (tipoEvento) {
      case EventoOs.ORCAMENTO_GERADO -> StatusOs.AGUARDANDO_APROVACAO;
      case EventoOs.EXECUCAO_INICIADA -> StatusOs.EM_EXECUCAO;
      case EventoOs.EXECUCAO_FINALIZADA -> StatusOs.FINALIZADA;
      case EventoOs.PAGAMENTO_CONFIRMADO -> StatusOs.ENTREGUE;
      // eventos de compensação não mudam o status por si (já CANCELADA/RECUSADA localmente)
      case EventoOs.PAGAMENTO_CANCELADO, EventoOs.EXECUCAO_CANCELADA -> null;
      default -> null;
    };
  }
}
