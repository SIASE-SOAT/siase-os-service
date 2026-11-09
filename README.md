# siase-os-service

> SIASE — Fase 4 | Microsserviço de Ordens de Serviço

> README v1 (Fase 3 — Monólito): ver repositório `15SOAT`

## Descrição

Microsserviço responsável pelo ciclo de vida completo das Ordens de Serviço. Faz parte da arquitetura de microsserviços da Fase 4, comunicando-se com `siase-billing-service` e `siase-execution-service` via **AWS SQS** (Saga Coreografado).

## Responsabilidades

- Abertura e gestão de OS
- Controle de status (máquina de estados)
- Aprovação/recusa de orçamento pelo cliente
- Publicação de eventos de domínio via SQS
- Histórico de eventos via DynamoDB
- Consumo de eventos dos demais microsserviços

## Tecnologias

| Tecnologia | Versão | Justificativa |
|---|---|---|
| Java | 17 | LTS, records, sealed classes |
| Spring Boot | 3.2.4 | Framework consolidado |
| PostgreSQL | 16 | Banco relacional para OS |
| DynamoDB | AWS SDK v2 | Histórico de eventos (NoSQL nativo AWS) |
| AWS SQS | AWS SDK v2 | Mensageria assíncrona para Saga |
| Flyway | — | Migrations versionadas |
| MapStruct | 1.5.x | Mapeamento domínio ↔ JPA |
| Cucumber | 7.15 | Testes BDD |
| JaCoCo | 0.8.11 | Cobertura mínima 80% |

## Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                        │
│  ┌──────────┐  ┌──────────────┐  ┌────────┐  ┌───────┐  │
│  │ REST API │  │ JPA/Postgres │  │  SQS   │  │Dynamo │  │
│  └────┬─────┘  └──────┬───────┘  └───┬────┘  └───┬───┘  │
├───────┼───────────────┼──────────────┼────────────┼──────┤
│       ▼               ▼              ▼            ▼      │
│  ┌────────────────────────────────────────────────────┐  │
│  │              APPLICATION (use cases)               │  │
│  │  CriarOsUC  AvancarStatusOsUC  CancelarOsUC        │  │
│  │  AprovarOrcamentoUC  ConsultarOsUC                 │  │
│  │  AtualizarStatusPorEventoUC                        │  │
│  └───────────────────┬────────────────────────────────┘  │
├───────────────────────┼────────────────────────────────── ┤
│                       ▼                                   │
│  ┌────────────────────────────────────────────────────┐  │
│  │                   DOMAIN                           │  │
│  │  OrdemDeServico  ItemServico  ItemPeca  EventoOS   │  │
│  │  StatusOS  Ports (interfaces)                      │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Saga Pattern — Coreografado

```
OS criada ──► [SQS billing] ──► Billing gera orçamento
                                       │
                              [SQS os-updates] ──► OS: AGUARDANDO_APROVACAO
                                       │
Cliente aprova ──► [SQS execution] ──► Execution inicia
                                       │
                              [SQS os-updates] ──► OS: EM_EXECUCAO
                                       │
Execução finaliza ──► [SQS os-updates] ──► OS: FINALIZADA
                                       │
Pagamento confirmado ──► [SQS os-updates] ──► OS: ENTREGUE
```

## Filas SQS

| Variável | Fila | Direção |
|---|---|---|
| `SQS_BILLING_URL` | siase-billing-queue | Produz |
| `SQS_EXECUTION_URL` | siase-execution-queue | Produz |
| `SQS_OS_EVENTOS_URL` | siase-os-eventos | Produz |
| `SQS_OS_UPDATES_URL` | siase-os-updates-queue | Consome |

## Endpoints

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/api/ordens` | JWT | Criar OS |
| GET | `/api/ordens` | JWT | Listar OS |
| GET | `/api/ordens/{id}` | JWT | Buscar por ID |
| GET | `/api/ordens/acompanhar/{numero}` | — | Acompanhar (público) |
| GET | `/api/ordens/cliente/{clienteId}` | JWT | OS por cliente |
| PATCH | `/api/ordens/{id}/avancar` | JWT | Avançar status |
| PATCH | `/api/ordens/{id}/cancelar` | JWT | Cancelar |
| PATCH | `/api/ordens/acompanhar/{numero}/aprovar-orcamento` | — | Aprovar (público) |
| PATCH | `/api/ordens/acompanhar/{numero}/recusar-orcamento` | — | Recusar (público) |

## Execução Local

```bash
# Variáveis necessárias
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=siase_os_db
export DB_USER=siase_user
export DB_PASSWORD=siase_pass
export JWT_SECRET=<segredo>
export AWS_REGION=us-east-1
export SQS_OS_EVENTOS_URL=https://sqs.us-east-1.amazonaws.com/...
export SQS_BILLING_URL=https://sqs.us-east-1.amazonaws.com/...
export SQS_EXECUTION_URL=https://sqs.us-east-1.amazonaws.com/...
export SQS_OS_UPDATES_URL=https://sqs.us-east-1.amazonaws.com/...

# Build e execução
mvn clean package -DskipTests
java -jar target/siase-os-service-1.0.0.jar
```

## Testes

```bash
# Todos os testes (unitários + BDD)
mvn clean verify

# Relatório de cobertura
open target/site/jacoco/index.html

# Relatório BDD
open target/cucumber-reports/report.html
```

### Cobertura mínima: 80% de linhas (enforced pelo JaCoCo no build)

## Documentação

- [ADR-001 — Microsserviços e Saga Coreografado](docs/adr/ADR-001-microservicos-saga-coreografado.md)
- [ADR-002 — DynamoDB para Histórico de Eventos](docs/adr/ADR-002-dynamodb-historico-eventos.md)
- [RFC-001 — Contratos de Eventos SQS](docs/rfc/RFC-001-saga-coreografado-contratos.md)
- [Diagramas de Sequência v2](docs/diagramas-sequencia-v2.md)
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
