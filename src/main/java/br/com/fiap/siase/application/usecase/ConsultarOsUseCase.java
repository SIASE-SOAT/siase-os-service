package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarOsUseCase {

    private final OsRepositoryPort repository;
    private final HistoricoEventoPort historico;

    public ConsultarOsUseCase(OsRepositoryPort repository, HistoricoEventoPort historico) {
        this.repository = repository;
        this.historico = historico;
    }

    public OrdemDeServico porId(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new OsNaoEncontradaException(id.toString()));
    }

    public OrdemDeServico porNumero(String numero) {
        return repository.buscarPorNumero(numero)
                .orElseThrow(() -> new OsNaoEncontradaException(numero));
    }

    public List<OrdemDeServico> listar() {
        return repository.listar();
    }

    public List<OrdemDeServico> porCliente(UUID clienteId) {
        return repository.listarPorCliente(clienteId);
    }

    public List<EventoOs> historico(UUID osId) {
        return historico.historicoDaOs(osId);
    }
}
