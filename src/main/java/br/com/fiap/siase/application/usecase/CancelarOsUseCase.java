package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Cancelamento da OS — compensação do Saga: publica OS_CANCELADA para
 * billing (cancela orçamento/pagamento) e execution (remove da fila).
 */
@Service
public class CancelarOsUseCase {

    private final OsRepositoryPort repository;
    private final EventoPublicadorPort publicador;
    private final HistoricoEventoPort historico;

    public CancelarOsUseCase(OsRepositoryPort repository,
                             EventoPublicadorPort publicador,
                             HistoricoEventoPort historico) {
        this.repository = repository;
        this.publicador = publicador;
        this.historico = historico;
    }

    @Transactional
    public OrdemDeServico executar(UUID osId) {
        OrdemDeServico os = repository.buscarPorId(osId)
                .orElseThrow(() -> new OsNaoEncontradaException(osId.toString()));
        os.cancelar();
        repository.salvar(os);

        EventoOs evento = EventoOs.de(EventoOs.OS_CANCELADA, os);
        publicador.publicarParaBilling(evento);
        publicador.publicarParaExecution(evento);
        publicador.publicarEventoDominio(evento);
        historico.registrar(evento);
        return os;
    }
}
