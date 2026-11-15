# siase-os-service — SIASE Fase 4

Microsserviço de **Ordens de Serviço**: ciclo de vida completo da OS, máquina de
estados, aprovação/recusa de orçamento pelo cliente e histórico de eventos.
Participa do **Saga coreografado via AWS SQS** com `siase-billing-service` e
`siase-execution-service` (ver ADR-001).

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | LTS |
| Spring Boot | 3.2.4 | Web + JPA + Security (JWT resource server) |
| PostgreSQL | 16 (RDS) | Banco relacional próprio (`siase_os_db`) |
| DynamoDB | AWS SDK v2 | Histórico de eventos (NoSQL) |
| AWS SQS | AWS SDK v2 | Mensageria do Saga |
| Flyway | — | Migrations |
| Cucumber | 7.15 | BDD |
| JaCoCo | 0.8.11 | Cobertura mínima 80% (gate no build) |
| Micrometer/Prometheus | — | Observabilidade (Fase 3) |

## Arquitetura (hexagonal)

```
infrastructure/adapters/input   → REST controller + consumer SQS
infrastructure/adapters/output  → JPA/Postgres, SQS, DynamoDB
application                     → use cases + ports
domain                          → OrdemDeServico, StatusOs (máquina de estados), EventoOs
```

## Responsabilidades

- Abertura e gestão de OS (`POST /api/ordens`)
- Máquina de estados: CRIADA → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO →
  FINALIZADA → ENTREGUE (+ RECUSADA/CANCELADA com compensação)
- Aprovação/recusa de orçamento (endpoints públicos por número da OS)
- Publicação de eventos de domínio (SQS) e histórico (DynamoDB)
- Consumo da fila `os-updates` (eventos de billing e execution)

## Filas SQS (RFC-001)

| Fila | Direção | Eventos |
|---|---|---|
| `siase-*-billing-queue` | produz | OS_CRIADA, OS_CANCELADA, ORCAMENTO_RECUSADO |
| `siase-*-execution-queue` | produz | ORCAMENTO_APROVADO, OS_CANCELADA |
| `siase-*-os-eventos` | produz | todos os eventos de domínio |
| `siase-*-os-updates-queue` | consome | ORCAMENTO_GERADO, EXECUCAO_INICIADA/FINALIZADA, PAGAMENTO_CONFIRMADO |

## Endpoints

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/api/ordens` | JWT | Criar OS (inicia o Saga) |
| GET | `/api/ordens` | JWT | Listar OS |
| GET | `/api/ordens/{id}` | JWT | Buscar por ID |
| GET | `/api/ordens/{id}/historico` | JWT | Histórico de eventos (DynamoDB) |
| GET | `/api/ordens/cliente/{clienteId}` | JWT | OS por cliente |
| GET | `/api/ordens/acompanhar/{numero}` | — | Acompanhamento público |
| PATCH | `/api/ordens/acompanhar/{numero}/aprovar-orcamento` | — | Aprovar orçamento |
| PATCH | `/api/ordens/acompanhar/{numero}/recusar-orcamento` | — | Recusar orçamento (compensação) |
| PATCH | `/api/ordens/{id}/cancelar` | JWT | Cancelar OS (compensação) |

Swagger UI: `/api/swagger-ui.html`

## Variáveis de ambiente

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`,
`AWS_REGION`, `AWS_ENDPOINT` (LocalStack opcional), `SQS_BILLING_URL`,
`SQS_EXECUTION_URL`, `SQS_OS_EVENTOS_URL`, `SQS_OS_UPDATES_URL`,
`DYNAMODB_HISTORICO_TABLE`, `ENVIRONMENT`.

## Execução local

```bash
mvn clean package -DskipTests
java -jar target/siase-os-service-1.0.0.jar
```

Para AWS local, usar LocalStack (`AWS_ENDPOINT=http://localhost:4566`).

## Testes

```bash
mvn clean verify
# Cobertura: target/site/jacoco/index.html (gate 80% no build)
# BDD:       target/cucumber-reports/report.html (features/saga_os.feature)
```

## Documentação

- `docs/adr/ADR-001` — decisão do Saga coreografado
- `docs/OBSERVABILIDADE.md` — métricas, logs JSON, correlationId
