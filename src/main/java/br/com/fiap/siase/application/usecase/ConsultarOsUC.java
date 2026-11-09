package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

import java.util.List;
import java.util.UUID;

public class ConsultarOsUC {

    private final OrdemServicoRepositoryPort repository;

    public ConsultarOsUC(OrdemServicoRepositoryPort repository) {
        this.repository = repository;
    }

    public OsResponse porId(UUID id) {
        return OsResponse.from(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + id)));
    }

    public OsResponse porNumero(String numero) {
        return OsResponse.from(repository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + numero)));
    }

    public List<OsResponse> listar(StatusOS status) {
        var lista = status != null ? repository.findByStatus(status) : repository.findAll();
        return lista.stream().map(OsResponse::from).toList();
    }

    public List<OsResponse> porCliente(UUID clienteId) {
        return repository.findByClienteId(clienteId).stream().map(OsResponse::from).toList();
    }
}
