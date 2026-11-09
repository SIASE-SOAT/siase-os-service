# RFC-001 — Fluxo Saga Coreografado entre Microsserviços

**Status:** Aprovado  
**Data:** 2025-01  
**Autores:** Time SIASE

## Resumo

Define o contrato de eventos SQS entre os microsserviços e o fluxo de compensação (rollback) do Saga Pattern.

## Filas SQS

| Fila | Produtor | Consumidor | Propósito |
|---|---|---|---|
| `siase-os-eventos` | OS Service | Monitoramento/Auditoria | Todos os eventos de OS |
| `siase-billing-queue` | OS Service, Billing | Billing Service | Eventos para billing |
| `siase-execution-queue` | OS Service, Billing | Execution Service | Eventos para execução |
| `siase-os-updates-queue` | Billing, Execution | OS Service | Atualizações de status |

## Contratos de Eventos

### OS_CRIADA (OS → Billing)
```json
{
  "tipo": "OS_CRIADA",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "clienteId": "uuid",
  "clienteNome": "João Silva",
  "clienteEmail": "joao@email.com",
  "veiculoId": "uuid",
  "total": "350.00",
  "status": "RECEBIDA"
}
```

### ORCAMENTO_APROVADO (OS → Execution)
```json
{
  "tipo": "ORCAMENTO_APROVADO",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "clienteId": "uuid",
  "veiculoId": "uuid",
  "total": "350.00",
  "status": "APROVADO"
}
```

### EXECUCAO_INICIADA (Execution → OS)
```json
{
  "tipo": "EXECUCAO_INICIADA",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```

### EXECUCAO_FINALIZADA (Execution → OS)
```json
{
  "tipo": "EXECUCAO_FINALIZADA",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```

### PAGAMENTO_CONFIRMADO (Billing → OS)
```json
{
  "tipo": "PAGAMENTO_CONFIRMADO",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid",
  "valor": "350.00",
  "formaPagamento": "PIX"
}
```

### OS_CANCELADA (OS → Billing, Execution)
```json
{
  "tipo": "OS_CANCELADA",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid",
  "motivo": "Cliente desistiu"
}
```

## Fluxo de Compensação (Rollback)

```
Cenário: Pagamento falha no Mercado Pago
  Billing → publica PAGAMENTO_FALHOU → OS Service mantém status FINALIZADA
  Billing → notifica cliente para nova tentativa
  (OS não é cancelada — pagamento pode ser retentado)

Cenário: Orçamento recusado pelo cliente
  OS Service → cancela OS → publica OS_CANCELADA
  Billing → cancela orçamento pendente
  Execution → remove da fila (se já estava lá)

Cenário: Falha na execução
  Execution → publica EXECUCAO_FALHOU → OS Service mantém EM_EXECUCAO
  Execution → notifica mecânico responsável
```

## Idempotência

Todos os consumidores devem ser idempotentes. O `osNumero` é usado como chave de idempotência — processar o mesmo evento duas vezes não deve causar efeitos colaterais.
