package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.OrdemDeServico;

public interface EventPublisherPort {
    void publicarOsCriada(OrdemDeServico os);
    void publicarStatusAlterado(OrdemDeServico os, String statusAnterior);
    void publicarOsCancelada(OrdemDeServico os, String motivo);
    void publicarOrcamentoAprovado(OrdemDeServico os);
    void publicarOrcamentoRecusado(OrdemDeServico os);
}
