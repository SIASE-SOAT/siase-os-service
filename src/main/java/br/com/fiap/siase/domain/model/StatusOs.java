package br.com.fiap.siase.domain.model;

import java.util.Set;

/**
 * Máquina de estados da Ordem de Serviço (Saga coreografado).
 */
public enum StatusOs {
  CRIADA,
  AGUARDANDO_APROVACAO,
  APROVADA,
  EM_EXECUCAO,
  FINALIZADA,
  AGUARDANDO_PAGAMENTO,
  ENTREGUE,
  RECUSADA,
  CANCELADA;

  private Set<StatusOs> transicoes;

  static {
    CRIADA.transicoes = Set.of(AGUARDANDO_APROVACAO, CANCELADA);
    AGUARDANDO_APROVACAO.transicoes = Set.of(APROVADA, RECUSADA, CANCELADA);
    APROVADA.transicoes = Set.of(EM_EXECUCAO, CANCELADA);
    EM_EXECUCAO.transicoes = Set.of(FINALIZADA, CANCELADA);
    FINALIZADA.transicoes = Set.of(AGUARDANDO_PAGAMENTO, ENTREGUE);
    AGUARDANDO_PAGAMENTO.transicoes = Set.of(ENTREGUE, CANCELADA);
    // PAGAMENTO_CONFIRMADO pode chegar em FINALIZADA ou AGUARDANDO_PAGAMENTO
    ENTREGUE.transicoes = Set.of();
    RECUSADA.transicoes = Set.of(CANCELADA);
    CANCELADA.transicoes = Set.of();
  }

  public boolean podeTransicionarPara(StatusOs destino) {
    return transicoes.contains(destino);
  }

  public boolean isTerminal() {
    return transicoes.isEmpty();
  }
}
