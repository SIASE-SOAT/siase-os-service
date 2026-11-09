package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

public class AtualizarStatusPorEventoUC {

    private final OrdemServicoRepositoryPort repository;
    private final EventoOSRepositoryPort eventoRepository;

    public AtualizarStatusPorEventoUC(OrdemServicoRepositoryPort repository,
                                       EventoOSRepositoryPort eventoRepository) {
        this.repository = repository;
        this.eventoRepository = eventoRepository;
    }

    public void executar(String osNumero, StatusOS novoStatus, String origem) {
        var os = repository.findByNumero(osNumero)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + osNumero));
        var statusAnterior = os.getStatus().name();
        os.setStatus(novoStatus);
        if (novoStatus == StatusOS.ENTREGUE) {
            os.setDataFechamento(java.time.LocalDateTime.now());
        }
        var salvo = repository.save(os);
        eventoRepository.salvar(EventoOS.criar(salvo, statusAnterior, origem,
                "Status atualizado via evento para " + novoStatus.getDescricao()));
    }
}
