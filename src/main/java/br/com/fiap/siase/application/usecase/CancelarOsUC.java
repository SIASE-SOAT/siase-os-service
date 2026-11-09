package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

import java.util.UUID;

public class CancelarOsUC {

    private final OrdemServicoRepositoryPort repository;
    private final EventPublisherPort eventPublisher;
    private final EventoOSRepositoryPort eventoRepository;

    public CancelarOsUC(OrdemServicoRepositoryPort repository,
                        EventPublisherPort eventPublisher,
                        EventoOSRepositoryPort eventoRepository) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.eventoRepository = eventoRepository;
    }

    public OsResponse executar(UUID osId, String motivo) {
        var os = repository.findById(osId)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + osId));
        var statusAnterior = os.getStatus().name();
        os.cancelar();
        var salvo = repository.save(os);
        eventoRepository.salvar(EventoOS.criar(salvo, statusAnterior, "OS-SERVICE", "OS cancelada: " + motivo));
        eventPublisher.publicarOsCancelada(salvo, motivo);
        return OsResponse.from(salvo);
    }
}
