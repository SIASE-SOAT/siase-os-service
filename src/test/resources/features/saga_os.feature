# language: pt
Funcionalidade: Saga coreografado da Ordem de Serviço (os-service)
  Como sistema de oficina distribuído
  Quero que a OS avance pelos eventos dos microsserviços
  Para manter consistência sem acesso cruzado de banco

  Cenário: Fluxo completo da OS até a entrega
    Dado uma OS criada para o cliente
    Então o evento "OS_CRIADA" é publicado para o billing
    Quando o billing publica "ORCAMENTO_GERADO"
    Então a OS fica com status "AGUARDANDO_APROVACAO"
    Quando o cliente aprova o orçamento
    Então a OS fica com status "APROVADA"
    E o evento "ORCAMENTO_APROVADO" é publicado para o execution
    Quando o execution publica "EXECUCAO_INICIADA"
    Então a OS fica com status "EM_EXECUCAO"
    Quando o execution publica "EXECUCAO_FINALIZADA"
    Então a OS fica com status "FINALIZADA"
    Quando o billing publica "PAGAMENTO_CONFIRMADO"
    Então a OS fica com status "ENTREGUE"
    E o histórico da OS contém todos os eventos do fluxo

  Cenário: Compensação — cancelamento dispara eventos para os dois serviços
    Dado uma OS criada para o cliente
    Quando a OS é cancelada
    Então a OS fica com status "CANCELADA"
    E o evento "OS_CANCELADA" é publicado para o billing
    E o evento "OS_CANCELADA" é publicado para o execution

  Cenário: Compensação — recusa de orçamento cancela no billing
    Dado uma OS aguardando aprovação de orçamento
    Quando o cliente recusa o orçamento
    Então a OS fica com status "RECUSADA"
    E o evento "ORCAMENTO_RECUSADO" é publicado para o billing
