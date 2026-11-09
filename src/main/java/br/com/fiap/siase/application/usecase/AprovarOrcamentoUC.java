package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

public class AprovarOrcamentoUC {

    private final OrdemServicoRepositoryPort repository;
    private final EventPublisherPort eventPublisher;
    private final EventoOSRepositoryPort eventoRepository;

    public AprovarOrcamentoUC(OrdemServicoRepositoryPort repository,
                               EventPublisherPort eventPublisher,
                               EventoOSRepositoryPort eventoRepository) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.eventoRepository = eventoRepository;
    }

    public OsResponse aprovar(String numero) {
        var os = repository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + numero));
        var statusAnterior = os.getStatus().name();
        os.aprovarOrcamento();
        var salvo = repository.save(os);
        eventoRepository.salvar(EventoOS.criar(salvo, statusAnterior, "CLIENTE", "Orçamento aprovado"));
        eventPublisher.publicarOrcamentoAprovado(salvo);
        return OsResponse.from(salvo);
    }

    public OsResponse recusar(String numero) {
        var os = repository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + numero));
        var statusAnterior = os.getStatus().name();
        os.recusarOrcamento();
        var salvo = repository.save(os);
        eventoRepository.salvar(EventoOS.criar(salvo, statusAnterior, "CLIENTE", "Orçamento recusado"));
        eventPublisher.publicarOrcamentoRecusado(salvo);
        return OsResponse.from(salvo);
    }
}
