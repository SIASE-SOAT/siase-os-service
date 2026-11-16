package br.com.fiap.siase.application;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.application.ports.OsRepositoryPort;
import br.com.fiap.siase.application.usecase.CriarOsUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
  void valorOrcamentoEPublicadoNoEventoOsCriada() {
    var repo = new RepoMemoria();
    var pub = new PublicadorMemoria();
    var hist = new HistoricoMemoria();
    var uc = new CriarOsUseCase(repo, pub, hist, new SimpleMeterRegistry());

    OrdemDeServico os = uc.executar(UUID.randomUUID(), "ABC-1234", "Freios",
            new BigDecimal("480.00"));

    assertEquals(new BigDecimal("480.00"), os.getValorOrcamento());
    assertTrue(hist.historicoDaOs(os.getId()).get(0).payload().contains("480.00"));
  }

  @Test
  void dadosDaFase3SaoPreservadosNoDominioENoEvento() {
    var repo = new RepoMemoria();
    var pub = new PublicadorMemoria();
    var hist = new HistoricoMemoria();
    var uc = new CriarOsUseCase(repo, pub, hist, new SimpleMeterRegistry());

    OrdemDeServico os = uc.executar(UUID.randomUUID(), "Maria Silva", "ABC-1234",
            "Revisão", new BigDecimal("480.00"),
            List.of(new ItemServico("Troca de óleo", new BigDecimal("120.00"))),
            List.of(new ItemPeca("Filtro de óleo", 1, new BigDecimal("45.00"))));

    assertEquals("Maria Silva", os.getClienteNome());
    assertEquals(1, os.getItensServico().size());
    assertEquals(1, os.getItensPeca().size());
    String payload = hist.historicoDaOs(os.getId()).get(0).payload();
    assertTrue(payload.contains("Maria Silva"));
    assertTrue(payload.contains("Troca de óleo"));
    assertTrue(payload.contains("Filtro de óleo"));
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