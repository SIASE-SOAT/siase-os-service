# ADR-001 — Separação em Microsserviços e Saga Coreografado

**Status:** Aceito  
**Data:** 2025-01  
**Contexto:** Fase 4 — Evolução para Microsserviços

## Contexto

A oficina mecânica atingiu escala nacional com múltiplas filiais. O monólito hexagonal da Fase 3 não suporta mais o volume de OS, a necessidade de resiliência por domínio e o deploy independente de cada área de negócio.

## Decisão

Separar a aplicação em 3 microsserviços independentes:

| Serviço | Responsabilidade | Banco SQL | Banco NoSQL |
|---|---|---|---|
| **siase-os-service** | Ciclo de vida das OS (porta 8081) | RDS PostgreSQL (`siase_os_db`) | DynamoDB (histórico de eventos) |
| **siase-billing-service** | Orçamentos e pagamentos (Mercado Pago Pix) (porta 8082) | RDS PostgreSQL (`siase_billing_db`) | — |
| **siase-execution-service** | Fila e execução de serviços (porta 8083) | RDS PostgreSQL (`siase_exec_db`) | DynamoDB (fila em tempo real) |

## Comunicação

- **SQS assíncrono**: todos os eventos de mudança de estado são publicados em filas SQS. Nenhum serviço chama diretamente o banco ou a API REST de outro — a atualização de estado ocorre exclusivamente via consumo de eventos.

## Saga Pattern — Coreografado

Escolhemos **Saga Coreografado** (sem orquestrador central) porque:
- Cada serviço é autônomo e reage a eventos sem depender de um coordenador
- Menor acoplamento — nenhum serviço conhece o fluxo completo
- Mais resiliente: falha em um serviço não bloqueia os demais

### Fluxo principal

```
OS criada → [SQS billing] → Billing gera orçamento
Orçamento gerado → [SQS os-updates] → OS avança para AGUARDANDO_APROVACAO
Cliente aprova → [SQS execution] → Execution inicia fila
Execução iniciada → [SQS os-updates] → OS avança para EM_EXECUCAO
Execução finalizada → [SQS os-updates] → OS avança para FINALIZADA
Pagamento confirmado → [SQS os-updates] → OS avança para ENTREGUE
  (transição possível de FINALIZADA ou AGUARDANDO_PAGAMENTO → ENTREGUE)
```

### Rollback (compensação)

```
Orçamento recusado → OS cancelada → [SQS billing, execution] → compensação
OS cancelada → Billing cancela cobrança pendente
OS cancelada → Execution remove da fila
```

## Consequências

- **Positivo**: deploy independente, escalabilidade por domínio, resiliência isolada
- **Negativo**: consistência eventual (não imediata), maior complexidade operacional
- **Mitigação**: DynamoDB registra todos os eventos para auditoria e rastreabilidade
