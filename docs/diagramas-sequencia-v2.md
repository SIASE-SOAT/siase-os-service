# Diagramas de Sequência — v2 (Fase 4 — Microsserviços)


---

## Fluxo Completo de OS — Saga Coreografado

```
Cliente/Mecânico    OS Service         SQS Billing     Billing Service    SQS Execution   Execution Service   SQS OS-Updates
      │                  │                  │                 │                  │                 │                  │
      │── POST /ordens ──►│                  │                 │                  │                 │                  │
      │                  │── save(OS) ──────►│                 │                  │                 │                  │
      │                  │── DynamoDB ───────│                 │                  │                 │                  │
      │                  │── OS_CRIADA ──────►│                 │                  │                 │                  │
      │◄── 201 Created ──│                  │── consume ──────►│                  │                 │                  │
      │                  │                  │                 │── gera orçamento  │                 │                  │
      │                  │                  │                 │── ORCAMENTO_GERADO►│                 │                  │
      │                  │                  │                 │                  │── consume ──────►│                  │
      │                  │                  │                 │                  │                 │── STATUS_ALTERADO►│
      │                  │                  │                 │                  │                 │                  │── consume
      │                  │◄─────────────────────────────────────────────────────────────────────────────────────────────│
      │                  │── update(AGUARDANDO_APROVACAO)      │                  │                 │                  │
      │                  │                  │                 │                  │                 │                  │
      │── PATCH /aprovar ►│                  │                 │                  │                 │                  │
      │                  │── update(APROVADO)│                 │                  │                 │                  │
      │                  │── ORCAMENTO_APROVADO ──────────────────────────────────►│                 │                  │
      │◄── 200 OK ───────│                  │                 │                  │── consume ──────►│                  │
      │                  │                  │                 │                  │                 │── inicia execução │
      │                  │                  │                 │                  │                 │── EXECUCAO_INICIADA►│
      │                  │                  │                 │                  │                 │                  │── consume
      │                  │◄─────────────────────────────────────────────────────────────────────────────────────────────│
      │                  │── update(EM_EXECUCAO)               │                 │                 │                  │
      │                  │                  │                 │                  │                 │                  │
      │                  │                  │                 │                  │                 │── finaliza ───────│
      │                  │                  │                 │                  │                 │── EXECUCAO_FINALIZADA►│
      │                  │                  │                 │                  │                 │                  │── consume
      │                  │◄─────────────────────────────────────────────────────────────────────────────────────────────│
      │                  │── update(FINALIZADA)                │                 │                 │                  │
      │                  │                  │                 │                  │                 │                  │
      │                  │                  │── EXECUCAO_FINALIZADA ──────────────►│                 │                  │
      │                  │                  │                 │── gera cobrança  │                 │                  │
      │                  │                  │                 │── Mercado Pago   │                 │                  │
      │◄── notificação ──────────────────────────────────────── │                 │                 │                  │
      │── confirma pagamento                │                 │                  │                 │                  │
      │                  │                  │                 │── PAGAMENTO_CONFIRMADO ─────────────────────────────────►│
      │                  │                  │                 │                  │                 │                  │── consume
      │                  │◄─────────────────────────────────────────────────────────────────────────────────────────────│
      │                  │── update(ENTREGUE)│                 │                 │                 │                  │
      │◄── notificação ──│                  │                 │                  │                 │                  │
```

---

## Fluxo de Compensação — Orçamento Recusado

```
Cliente             OS Service         SQS Billing     Billing Service    SQS Execution   Execution Service
      │                  │                  │                 │                  │                 │
      │── PATCH /recusar ►│                  │                 │                  │                 │
      │                  │── update(CANCELADA)                 │                 │                 │
      │                  │── OS_CANCELADA ───►│                 │                 │                 │
      │                  │── OS_CANCELADA ─────────────────────────────────────────►│                 │
      │◄── 200 OK ───────│                  │── consume ──────►│                 │                 │
      │                  │                  │                 │── cancela orçamento                 │
      │                  │                  │                 │                  │── consume ──────►│
      │                  │                  │                 │                  │                 │── remove da fila
```

---

## Criação de OS — Detalhe Interno do OS Service

```
Mecânico            Controller         CriarOsUC          Repository         SQS              DynamoDB
      │                  │                  │                  │                │                  │
      │── POST /ordens ──►│                  │                  │                │                  │
      │                  │── executar(req) ──►│                  │                │                  │
      │                  │                  │── existsByVeiculo ►│                │                  │
      │                  │                  │◄── false ─────────│                │                  │
      │                  │                  │── save(OS) ───────►│                │                  │
      │                  │                  │◄── OS salva ───────│                │                  │
      │                  │                  │── salvar(evento) ──────────────────────────────────────►│
      │                  │                  │── publicarOsCriada ►│                │                  │
      │                  │◄── OsResponse ───│                  │                │                  │
      │◄── 201 Created ──│                  │                  │                │                  │
```
