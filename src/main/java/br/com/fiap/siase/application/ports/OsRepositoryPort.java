package br.com.fiap.siase.application.ports;

import br.com.fiap.siase.domain.model.OrdemDeServico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OsRepositoryPort {

  OrdemDeServico salvar(OrdemDeServico os);

  Optional<OrdemDeServico> buscarPorId(UUID id);

  Optional<OrdemDeServico> buscarPorNumero(String numero);

  List<OrdemDeServico> listar();

  List<OrdemDeServico> listarPorCliente(UUID clienteId);
}
