package br.com.fiap.siase.domain;

import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrdemDeServicoTest {

    private OrdemDeServico osRecebida() {
        return OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-20250101-ABC123")
                .clienteId(UUID.randomUUID())
                .veiculoId(UUID.randomUUID())
                .status(StatusOS.RECEBIDA)
                .build();
    }

    @Test
    void deveAvancarDeRecebidaParaEmDiagnostico() {
        var os = osRecebida();
        os.avancarStatus();
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    }

    @Test
    void devePercorrerFluxoCompletoDeStatus() {
        var os = osRecebida();
        os.avancarStatus(); // EM_DIAGNOSTICO
        os.avancarStatus(); // AGUARDANDO_APROVACAO
        os.aprovarOrcamento(); // APROVADO
        os.avancarStatus(); // EM_EXECUCAO
        os.avancarStatus(); // FINALIZADA
        os.avancarStatus(); // ENTREGUE
        assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
        assertThat(os.getDataFechamento()).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoAvancarOsEntregue() {
        var os = osRecebida();
        os.setStatus(StatusOS.ENTREGUE);
        assertThatThrownBy(os::avancarStatus)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entregue");
    }

    @Test
    void deveCancelarOsRecebida() {
        var os = osRecebida();
        os.cancelar();
        assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
        assertThat(os.getDataFechamento()).isNotNull();
    }

    @Test
    void naoDeveCancelarOsEmExecucao() {
        var os = osRecebida();
        os.setStatus(StatusOS.EM_EXECUCAO);
        assertThatThrownBy(os::cancelar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("execução");
    }

    @Test
    void deveAprovarOrcamentoQuandoAguardandoAprovacao() {
        var os = osRecebida();
        os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
        os.aprovarOrcamento();
        assertThat(os.getStatus()).isEqualTo(StatusOS.APROVADO);
    }

    @Test
    void deveRecusarOrcamentoECancelarOs() {
        var os = osRecebida();
        os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
        os.recusarOrcamento();
        assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    }

    @Test
    void naoDeveAprovarOrcamentoEmStatusErrado() {
        var os = osRecebida();
        assertThatThrownBy(os::aprovarOrcamento)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aguardando aprovação");
    }

    @Test
    void deveGerarNumeroNoFormatoCorreto() {
        String numero = OrdemDeServico.gerarNumero();
        assertThat(numero).matches("OS-\\d{8}-[A-Z0-9]{6}");
    }
}
