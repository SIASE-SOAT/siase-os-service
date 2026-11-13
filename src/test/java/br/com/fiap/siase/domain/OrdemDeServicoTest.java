package br.com.fiap.siase.domain;

import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import br.com.fiap.siase.domain.model.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeServicoTest {

    @Test
    void criaOsComStatusCriadaENumero() {
        OrdemDeServico os = OrdemDeServico.criar(UUID.randomUUID(), "ABC-1234", "Barulho no motor");
        assertEquals(StatusOs.CRIADA, os.getStatus());
        assertTrue(os.getNumero().startsWith("OS-"));
        assertNotNull(os.getId());
    }

    @Test
    void fluxoFelizDeTransicoes() {
        OrdemDeServico os = OrdemDeServico.criar(UUID.randomUUID(), "ABC-1234", "x");
        os.transicionarPara(StatusOs.AGUARDANDO_APROVACAO);
        os.transicionarPara(StatusOs.APROVADA);
        os.transicionarPara(StatusOs.EM_EXECUCAO);
        os.transicionarPara(StatusOs.FINALIZADA);
        os.transicionarPara(StatusOs.ENTREGUE);
        assertTrue(os.getStatus().isTerminal());
    }

    @Test
    void rejeitaTransicaoInvalida() {
        OrdemDeServico os = OrdemDeServico.criar(UUID.randomUUID(), "ABC-1234", "x");
        assertThrows(TransicaoStatusInvalidaException.class,
                () -> os.transicionarPara(StatusOs.ENTREGUE));
    }

    @Test
    void cancelamentoEmStatusValido() {
        OrdemDeServico os = OrdemDeServico.criar(UUID.randomUUID(), "ABC-1234", "x");
        os.transicionarPara(StatusOs.AGUARDANDO_APROVACAO);
        os.cancelar();
        assertEquals(StatusOs.CANCELADA, os.getStatus());
        assertThrows(TransicaoStatusInvalidaException.class,
                () -> os.transicionarPara(StatusOs.APROVADA));
    }
}
