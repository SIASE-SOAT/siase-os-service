# language: pt
Funcionalidade: Fluxo completo de Ordem de Serviço
  Como mecânico da oficina
  Quero gerenciar o ciclo de vida completo de uma OS
  Para garantir que o fluxo de atendimento funcione corretamente

  Cenário: Criar uma nova ordem de serviço com sucesso
    Dado que não existe OS ativa para o veículo
    Quando eu criar uma OS para o cliente "João Silva" com o veículo "ABC-1234"
    Então a OS deve ser criada com status "RECEBIDA"
    E um evento "OS_CRIADA" deve ser publicado

  Cenário: Avançar OS do status RECEBIDA para EM_DIAGNOSTICO
    Dado que existe uma OS com status "RECEBIDA"
    Quando eu avançar o status da OS
    Então a OS deve ter status "EM_DIAGNOSTICO"
    E um evento "STATUS_ALTERADO" deve ser publicado

  Cenário: Fluxo completo de aprovação de orçamento
    Dado que existe uma OS com status "AGUARDANDO_APROVACAO"
    Quando o cliente aprovar o orçamento pelo número da OS
    Então a OS deve ter status "APROVADO"
    E um evento "ORCAMENTO_APROVADO" deve ser publicado

  Cenário: Recusa de orçamento cancela a OS
    Dado que existe uma OS com status "AGUARDANDO_APROVACAO"
    Quando o cliente recusar o orçamento pelo número da OS
    Então a OS deve ter status "CANCELADA"
    E um evento "ORCAMENTO_RECUSADO" deve ser publicado

  Cenário: Cancelar OS em status RECEBIDA
    Dado que existe uma OS com status "RECEBIDA"
    Quando eu cancelar a OS com motivo "Cliente desistiu"
    Então a OS deve ter status "CANCELADA"
    E um evento "OS_CANCELADA" deve ser publicado

  Cenário: Não deve criar OS quando veículo já possui OS ativa
    Dado que existe uma OS ativa para o veículo
    Quando eu tentar criar uma nova OS para o mesmo veículo
    Então deve retornar erro de negócio "OS em andamento"

  Cenário: Fluxo completo da OS até entrega
    Dado que não existe OS ativa para o veículo
    Quando eu criar uma OS para o cliente "Maria Santos" com o veículo "XYZ-9876"
    E avançar o status da OS para "EM_DIAGNOSTICO"
    E avançar o status da OS para "AGUARDANDO_APROVACAO"
    E o cliente aprovar o orçamento pelo número da OS
    E avançar o status da OS para "EM_EXECUCAO"
    E avançar o status da OS para "FINALIZADA"
    E avançar o status da OS para "ENTREGUE"
    Então a OS deve ter status "ENTREGUE"
    E a data de fechamento deve estar preenchida
