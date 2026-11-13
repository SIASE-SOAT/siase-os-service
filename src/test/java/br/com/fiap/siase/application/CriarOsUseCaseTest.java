package br.com.fiap.siase.application;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.application.usecase.CriarOsUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CriarOsUseCaseTest {

  public static class RepoMemoria implements OsRepositoryPort {
    public Map<UUID, OrdemDeServico> dados = new HashMap<>();
    public OrdemDeServico salvar(OrdemDeServico os) { dados.put(os.getId(), os); return os; }
    public Optional<OrdemDeServico> buscarPorId(UUID id) { return Optional.ofNullable(dados.get(id)); }
    public Optional<OrdemDeServico> buscarPorNumero(String n) {
      return dados.values().stream().filter(o -> o.getNumero().equals(n)).findFirst();
    }
    public List<OrdemDeServico> listar() { return new ArrayList<>(dados.values()); }
    public List<OrdemDeServico> listarPorCliente(UUID c) {
      return dados.values().stream().filter(o -> o.getClienteId().equals(c)).toList();
    }
  }

  public static class PublicadorMemoria implements EventoPublicadorPort {
    public List<String> billing = new ArrayList<>();
    public List<String> execution = new ArrayList<>();
    public List<String> dominio = new ArrayList<>();
    public void publicarParaBilling(EventoOs e) { billing.add(e.tipoEvento()); }
    public void publicarParaExecution(EventoOs e) { execution.add(e.tipoEvento()); }
    public void publicarEventoDominio(EventoOs e) { dominio.add(e.tipoEvento()); }
  }

  public static class HistoricoMemoria implements HistoricoEventoPort {
    public List<EventoOs> eventos = new ArrayList<>();
    public void registrar(EventoOs e) { eventos.add(e); }
    public List<EventoOs> historicoDaOs(UUID osId) {
      return eventos.stream().filter(e -> e.osId().equals(osId)).toList();
    }
  }

  @Test
  void criarOsPersistePublicaOsCriadaERegistraHistorico() {
    var repo = new RepoMemoria();
    var pub = new PublicadorMemoria();
    var hist = new HistoricoMemoria();
    var uc = new CriarOsUseCase(repo, pub, hist, new SimpleMeterRegistry());

    OrdemDeServico os = uc.executar(UUID.randomUUID(), "ABC-1234", "Freios");

    assertEquals(StatusOs.CRIADA, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
    assertEquals(List.of(EventoOs.OS_CRIADA), pub.billing);
    assertEquals(List.of(EventoOs.OS_CRIADA), pub.dominio);
    assertEquals(1, hist.historicoDaOs(os.getId()).size());
  }
}
