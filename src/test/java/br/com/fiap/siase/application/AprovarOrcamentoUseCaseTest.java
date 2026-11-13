package br.com.fiap.siase.application;

import br.com.fiap.siase.application.CriarOsUseCaseTest.*;
import br.com.fiap.siase.application.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.model.StatusOs;
import br.com.fiap.siase.domain.model.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AprovarOrcamentoUseCaseTest {

    private OrdemDeServico osAguardando(RepoMemoria repo) {
        OrdemDeServico os = repo.salvar(OrdemDeServico.criar(UUID.randomUUID(), "X", "y"));
        os.transicionarPara(StatusOs.AGUARDANDO_APROVACAO);
        return repo.salvar(os);
    }

    @Test
    void aprovarPublicaEventoParaExecution() {
        var repo = new RepoMemoria();
        var pub = new PublicadorMemoria();
        var uc = new AprovarOrcamentoUseCase(repo, pub, new HistoricoMemoria());
        OrdemDeServico os = osAguardando(repo);

        uc.aprovar(os.getNumero());

        assertEquals(StatusOs.APROVADA, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
        assertEquals(List.of(EventoOs.ORCAMENTO_APROVADO), pub.execution);
    }

    @Test
    void recusarDisparaCompensacaoParaBilling() {
        var repo = new RepoMemoria();
        var pub = new PublicadorMemoria();
        var uc = new AprovarOrcamentoUseCase(repo, pub, new HistoricoMemoria());
        OrdemDeServico os = osAguardando(repo);

        uc.recusar(os.getNumero());

        assertEquals(StatusOs.RECUSADA, repo.buscarPorId(os.getId()).orElseThrow().getStatus());
        assertEquals(List.of(EventoOs.ORCAMENTO_RECUSADO), pub.billing);
    }

    @Test
    void aprovarOsCriadaFalha_transicaoInvalida() {
        var repo = new RepoMemoria();
        var uc = new AprovarOrcamentoUseCase(repo, new PublicadorMemoria(), new HistoricoMemoria());
        OrdemDeServico os = repo.salvar(OrdemDeServico.criar(UUID.randomUUID(), "X", "y"));

        assertThrows(TransicaoStatusInvalidaException.class, () -> uc.aprovar(os.getNumero()));
    }
}
