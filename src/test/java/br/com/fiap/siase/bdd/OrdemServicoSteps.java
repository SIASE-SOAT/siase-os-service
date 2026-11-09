package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.dto.input.CriarOsRequest;
import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;
import io.cucumber.java.pt.*;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrdemServicoSteps {

    private final OrdemServicoRepositoryPort repository = mock(OrdemServicoRepositoryPort.class);
    private final EventPublisherPort eventPublisher = mock(EventPublisherPort.class);
    private final EventoOSRepositoryPort eventoRepository = mock(EventoOSRepositoryPort.class);

    private final CriarOsUC criarOsUC = new CriarOsUC(repository, eventPublisher, eventoRepository);
    private final AvancarStatusOsUC avancarStatusUC = new AvancarStatusOsUC(repository, eventPublisher, eventoRepository);
    private final CancelarOsUC cancelarOsUC = new CancelarOsUC(repository, eventPublisher, eventoRepository);
    private final AprovarOrcamentoUC aprovarOrcamentoUC = new AprovarOrcamentoUC(repository, eventPublisher, eventoRepository);

    private OsResponse ultimaResposta;
    private Exception ultimaExcecao;
    private OrdemDeServico osAtual;
    private final UUID veiculoId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();

    @Dado("que não existe OS ativa para o veículo")
    public void naoExisteOsAtiva() {
        when(repository.existsByVeiculoIdAndStatusNotIn(any(), any())).thenReturn(false);
    }

    @Dado("que existe uma OS ativa para o veículo")
    public void existeOsAtiva() {
        when(repository.existsByVeiculoIdAndStatusNotIn(any(), any())).thenReturn(true);
    }

    @Dado("que existe uma OS com status {string}")
    public void existeOsComStatus(String status) {
        osAtual = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-20250101-TEST01")
                .clienteId(clienteId)
                .veiculoId(veiculoId)
                .status(StatusOS.valueOf(status))
                .build();
        when(repository.findById(osAtual.getId())).thenReturn(Optional.of(osAtual));
        when(repository.findByNumero(osAtual.getNumero())).thenReturn(Optional.of(osAtual));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Quando("eu criar uma OS para o cliente {string} com o veículo {string}")
    public void criarOs(String nomeCliente, String placa) {
        var osSalva = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero(OrdemDeServico.gerarNumero())
                .clienteId(clienteId)
                .clienteNome(nomeCliente)
                .clienteEmail(nomeCliente.toLowerCase().replace(" ", ".") + "@test.com")
                .veiculoId(veiculoId)
                .veiculoPlaca(placa)
                .status(StatusOS.RECEBIDA)
                .build();
        when(repository.save(any())).thenReturn(osSalva);
        osAtual = osSalva;
        ultimaResposta = criarOsUC.executar(
                new CriarOsRequest(clienteId, nomeCliente,
                        nomeCliente.toLowerCase().replace(" ", ".") + "@test.com",
                        veiculoId, placa, null, null, List.of(), List.of()));
    }

    @Quando("eu avançar o status da OS")
    @E("avançar o status da OS para {string}")
    public void avancarStatus(String statusEsperado) {
        ultimaResposta = avancarStatusUC.executar(osAtual.getId());
        osAtual = OrdemDeServico.builder()
                .id(osAtual.getId())
                .numero(osAtual.getNumero())
                .clienteId(osAtual.getClienteId())
                .veiculoId(osAtual.getVeiculoId())
                .status(StatusOS.valueOf(ultimaResposta.status()))
                .build();
        when(repository.findById(osAtual.getId())).thenReturn(Optional.of(osAtual));
        when(repository.findByNumero(osAtual.getNumero())).thenReturn(Optional.of(osAtual));
    }

    @Quando("eu avançar o status da OS")
    public void avancarStatus() {
        ultimaResposta = avancarStatusUC.executar(osAtual.getId());
    }

    @Quando("o cliente aprovar o orçamento pelo número da OS")
    @E("o cliente aprovar o orçamento pelo número da OS")
    public void aprovarOrcamento() {
        ultimaResposta = aprovarOrcamentoUC.aprovar(osAtual.getNumero());
        osAtual.setStatus(StatusOS.APROVADO);
        when(repository.findById(osAtual.getId())).thenReturn(Optional.of(osAtual));
        when(repository.findByNumero(osAtual.getNumero())).thenReturn(Optional.of(osAtual));
    }

    @Quando("o cliente recusar o orçamento pelo número da OS")
    public void recusarOrcamento() {
        ultimaResposta = aprovarOrcamentoUC.recusar(osAtual.getNumero());
    }

    @Quando("eu cancelar a OS com motivo {string}")
    public void cancelarOs(String motivo) {
        ultimaResposta = cancelarOsUC.executar(osAtual.getId(), motivo);
    }

    @Quando("eu tentar criar uma nova OS para o mesmo veículo")
    public void tentarCriarOsDuplicada() {
        try {
            criarOsUC.executar(new CriarOsRequest(clienteId, "João", "joao@test.com",
                    veiculoId, "ABC-1234", null, null, null, null));
        } catch (BusinessException e) {
            ultimaExcecao = e;
        }
    }

    @Então("a OS deve ser criada com status {string}")
    @Então("a OS deve ter status {string}")
    public void verificarStatus(String status) {
        assertThat(ultimaResposta.status()).isEqualTo(status);
    }

    @E("um evento {string} deve ser publicado")
    public void verificarEventoPublicado(String tipoEvento) {
        switch (tipoEvento) {
            case "OS_CRIADA" -> verify(eventPublisher, atLeastOnce()).publicarOsCriada(any());
            case "STATUS_ALTERADO" -> verify(eventPublisher, atLeastOnce()).publicarStatusAlterado(any(), any());
            case "OS_CANCELADA" -> verify(eventPublisher, atLeastOnce()).publicarOsCancelada(any(), any());
            case "ORCAMENTO_APROVADO" -> verify(eventPublisher, atLeastOnce()).publicarOrcamentoAprovado(any());
            case "ORCAMENTO_RECUSADO" -> verify(eventPublisher, atLeastOnce()).publicarOrcamentoRecusado(any());
        }
    }

    @Então("deve retornar erro de negócio {string}")
    public void verificarErroDenegocio(String mensagem) {
        assertThat(ultimaExcecao).isInstanceOf(BusinessException.class);
        assertThat(ultimaExcecao.getMessage()).contains(mensagem);
    }

    @E("a data de fechamento deve estar preenchida")
    public void verificarDataFechamento() {
        assertThat(ultimaResposta.dataFechamento()).isNotNull();
    }
}
