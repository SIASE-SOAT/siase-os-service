package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aprovação/recusa do orçamento pelo cliente (endpoint público por número da OS).
 * Aprova → publica ORCAMENTO_APROVADO para o execution-service.
 * Recusa → publica ORCAMENTO_RECUSADO (billing cancela o orçamento — compensação).
 */
@Service
public class AprovarOrcamentoUseCase {

    private final OsRepositoryPort repository;
    private final EventoPublicadorPort publicador;
    private final HistoricoEventoPort historico;

    public AprovarOrcamentoUseCase(OsRepositoryPort repository,
                                   EventoPublicadorPort publicador,
                                   HistoricoEventoPort historico) {
        this.repository = repository;
        this.publicador = publicador;
        this.historico = historico;
    }

    @Transactional
    public OrdemDeServico aprovar(String numeroOs) {
        OrdemDeServico os = buscar(numeroOs);
        os.transicionarPara(StatusOs.APROVADA);
        repository.salvar(os);

        EventoOs evento = EventoOs.de(EventoOs.ORCAMENTO_APROVADO, os);
        publicador.publicarParaExecution(evento);
        publicador.publicarEventoDominio(evento);
        historico.registrar(evento);
        return os;
    }

    @Transactional
    public OrdemDeServico recusar(String numeroOs) {
        OrdemDeServico os = buscar(numeroOs);
        os.transicionarPara(StatusOs.RECUSADA);
        repository.salvar(os);

        EventoOs evento = EventoOs.de(EventoOs.ORCAMENTO_RECUSADO, os);
        publicador.publicarParaBilling(evento);   // compensação: cancelar orçamento
        publicador.publicarEventoDominio(evento);
        historico.registrar(evento);
        return os;
    }

    private OrdemDeServico buscar(String numeroOs) {
        return repository.buscarPorNumero(numeroOs)
                .orElseThrow(() -> new OsNaoEncontradaException(numeroOs));
    }
}
