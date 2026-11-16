package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Abertura de OS: persiste e publica OS_CRIADA na fila do billing (início do Saga).
 */
@Service
public class CriarOsUseCase {

    private final OsRepositoryPort repository;
    private final EventoPublicadorPort publicador;
    private final HistoricoEventoPort historico;
    private final MeterRegistry metrics;

    public CriarOsUseCase(OsRepositoryPort repository, EventoPublicadorPort publicador,
                          HistoricoEventoPort historico, MeterRegistry metrics) {
        this.repository = repository;
        this.publicador = publicador;
        this.historico = historico;
        this.metrics = metrics;
    }

    @Transactional
    public OrdemDeServico executar(UUID clienteId, String veiculo, String descricaoProblema) {
        return executar(clienteId, veiculo, descricaoProblema, null);
    }

    @Transactional
    public OrdemDeServico executar(UUID clienteId, String veiculo, String descricaoProblema,
                                   BigDecimal valorOrcamento) {
        return executar(clienteId, null, veiculo, descricaoProblema, valorOrcamento,
                List.of(), List.of());
    }

    @Transactional
    public OrdemDeServico executar(UUID clienteId, String clienteNome, String veiculo,
                                   String descricaoProblema, BigDecimal valorOrcamento,
                                   List<ItemServico> servicos, List<ItemPeca> pecas) {
        OrdemDeServico os = repository.salvar(
                OrdemDeServico.criar(clienteId, clienteNome, veiculo, descricaoProblema,
                        valorOrcamento, servicos, pecas));

        EventoOs evento = EventoOs.de(EventoOs.OS_CRIADA, os);
        publicador.publicarParaBilling(evento);
        publicador.publicarEventoDominio(evento);
        historico.registrar(evento);
        contar(evento.tipoEvento(), "produzido");
        return os;
    }

    void contar(String evento, String direcao) {
        metrics.counter("siase_saga_eventos_total",
                "servico", "os", "evento", evento, "direcao", direcao).increment();
    }
}