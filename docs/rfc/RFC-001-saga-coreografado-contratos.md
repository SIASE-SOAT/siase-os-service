# RFC-001 — Fluxo Saga Coreografado entre Microsserviços

**Status:** Aprovado
**Data:** 2025-01
**Autores:** Time SIASE

## Resumo

Define o contrato de eventos SQS entre os microsserviços e o fluxo de compensação (rollback) do Saga Pattern.

## Envelope de Evento

Todos os eventos trafegam com o mesmo envelope JSON, independente do serviço produtor:

```json
{
  "tipoEvento": "NOME_DO_EVENTO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{...json contextual opcional...}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```

O campo `payload` é um JSON serializado como string. Seu conteúdo varia por tipo de evento (ver abaixo). Consumidores que não precisam do payload podem ignorá-lo.

## Filas SQS

| Fila | Produtor | Consumidor | Propósito |
|---|---|---|---|
| `siase-os-eventos` | os-service | Monitoramento/Auditoria | Todos os eventos de OS |
| `siase-billing-queue` | os-service | billing-service | OS_CRIADA, OS_CANCELADA, ORCAMENTO_RECUSADO |
| `siase-execution-queue` | os-service | execution-service | ORCAMENTO_APROVADO, OS_CANCELADA |
| `siase-os-updates-queue` | billing-service, execution-service | os-service | ORCAMENTO_GERADO, EXECUCAO_INICIADA, EXECUCAO_FINALIZADA, PAGAMENTO_CONFIRMADO, PAGAMENTO_CANCELADO, EXECUCAO_CANCELADA |

## Contratos de Eventos

### OS_CRIADA (os-service → billing-queue + os-eventos)
```json
{
  "tipoEvento": "OS_CRIADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"clienteNome\":\"João Silva\",\"veiculo\":\"Fiat Uno\",\"valorOrcamento\":350.00,\"servicos\":[{\"descricao\":\"Troca de óleo\",\"preco\":80.00}],\"pecas\":[{\"nome\":\"Filtro\",\"quantidade\":1,\"precoUnitario\":30.00}]}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```

### ORCAMENTO_APROVADO (os-service → execution-queue + os-eventos)
```json
{
  "tipoEvento": "ORCAMENTO_APROVADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"clienteNome\":\"João Silva\",\"veiculo\":\"Fiat Uno\",\"valorOrcamento\":350.00,\"servicos\":[{\"descricao\":\"Troca de óleo\",\"preco\":80.00}]}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```

### ORCAMENTO_RECUSADO (os-service → billing-queue + os-eventos)
```json
{
  "tipoEvento": "ORCAMENTO_RECUSADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```

### OS_CANCELADA (os-service → billing-queue + execution-queue + os-eventos)
```json
{
  "tipoEvento": "OS_CANCELADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```

### ORCAMENTO_GERADO (billing-service → os-updates-queue)
```json
{
  "tipoEvento": "ORCAMENTO_GERADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"orcamentoId\":\"uuid\",\"valor\":350.00,\"paymentId\":\"pay-mp-123\",\"qrCodePix\":\"00020101...\",\"qrCodePixBase64\":\"bW9ja...\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `AGUARDANDO_APROVACAO`.

### PAGAMENTO_CONFIRMADO (billing-service → os-updates-queue)
```json
{
  "tipoEvento": "PAGAMENTO_CONFIRMADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"pagamentoId\":\"uuid\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `ENTREGUE` (de `FINALIZADA` ou `AGUARDANDO_PAGAMENTO`).

### PAGAMENTO_CANCELADO (billing-service → os-updates-queue)
```json
{
  "tipoEvento": "PAGAMENTO_CANCELADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service registra no histórico; OS já está `CANCELADA`.

### EXECUCAO_INICIADA (execution-service → os-updates-queue)
```json
{
  "tipoEvento": "EXECUCAO_INICIADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"mecanico\":\"Carlos\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `EM_EXECUCAO`.

### EXECUCAO_FINALIZADA (execution-service → os-updates-queue)
```json
{
  "tipoEvento": "EXECUCAO_FINALIZADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "Execução finalizada",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `FINALIZADA`.

### EXECUCAO_CANCELADA (execution-service → os-updates-queue)
```json
{
  "tipoEvento": "EXECUCAO_CANCELADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "Execução cancelada por compensação",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service registra no histórico; sem mudança de status (OS já está `CANCELADA`).

## Máquina de Estados da OS

```
CRIADA → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO → FINALIZADA → AGUARDANDO_PAGAMENTO → ENTREGUE
                              ↘ RECUSADA → CANCELADA
                                         ↘ CANCELADA (de qualquer estado não terminal)
```

Transições válidas por evento consumido:
- `ORCAMENTO_GERADO` → `AGUARDANDO_APROVACAO`
- `EXECUCAO_INICIADA` → `EM_EXECUCAO`
- `EXECUCAO_FINALIZADA` → `FINALIZADA`
- `PAGAMENTO_CONFIRMADO` → `ENTREGUE` (de `FINALIZADA` ou `AGUARDANDO_PAGAMENTO`)

## Fluxo de Compensação (Rollback)

```
Cenário: Orçamento recusado pelo cliente
  OS Service → RECUSADA → publica ORCAMENTO_RECUSADO
  Billing → cancela orçamento pendente

Cenário: OS cancelada (qualquer estado)
  OS Service → CANCELADA → publica OS_CANCELADA para billing e execution
  Billing → cancela orçamento; se pagamento confirmado → reembolsa → publica PAGAMENTO_CANCELADO
  Execution → cancela item na fila → publica EXECUCAO_CANCELADA

Cenário: Pagamento falha no Mercado Pago
  Billing → não publica evento; OS permanece em FINALIZADA aguardando nova tentativa
```

## Idempotência

Todos os consumidores são idempotentes:
- **billing-service**: `os_id` UNIQUE em `orcamentos` — `OS_CRIADA` duplicado retorna orçamento existente
- **billing-service**: `mp_payment_id` UNIQUE em `pagamentos` — webhook reenviado é ignorado
- **execution-service**: `osId` é PK no DynamoDB — `ORCAMENTO_APROVADO` duplicado retorna item existente

## Rastreabilidade

O `correlationId` é propagado como message attribute SQS (`X-Correlation-Id`) em todos os eventos, permitindo correlação ponta a ponta nos logs do Loki.
