package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

import java.util.UUID;

public class AvancarStatusOsUC {

    private final OrdemServicoRepositoryPort repository;
    private final EventPublisherPort eventPublisher;
    private final EventoOSRepositoryPort eventoRepository;

    public AvancarStatusOsUC(OrdemServicoRepositoryPort repository,
                              EventPublisherPort eventPublisher,
                              EventoOSRepositoryPort eventoRepository) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.eventoRepository = eventoRepository;
    }

    public OsResponse executar(UUID osId) {
        var os = repository.findById(osId)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + osId));
        var statusAnterior = os.getStatus().name();
        try {
            os.avancarStatus();
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
        var salvo = repository.save(os);
        eventoRepository.salvar(EventoOS.criar(salvo, statusAnterior, "OS-SERVICE", "Status avançado"));
        eventPublisher.publicarStatusAlterado(salvo, statusAnterior);
        return OsResponse.from(salvo);
    }
}
