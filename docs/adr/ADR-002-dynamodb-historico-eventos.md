# ADR-002 — DynamoDB como Banco NoSQL para Histórico de Eventos

**Status:** Aceito  
**Data:** 2026  
**Contexto:** Fase 4 — Escolha de banco NoSQL nativo AWS

## Contexto

O desafio exige uso de pelo menos um banco não relacional (NoSQL). As opções avaliadas foram:

| Opção | Prós | Contras |
|---|---|---|
| MongoDB Atlas | Flexível, rico em queries | Fora da AWS, custo adicional |
| Amazon DocumentDB | Compatível MongoDB, gerenciado | Caro, cluster obrigatório |
| **Amazon DynamoDB** | Serverless, nativo AWS, sem cluster | Modelo de acesso por chave |

## Decisão

Usar **Amazon DynamoDB** para armazenar o histórico de eventos de mudança de status das OS.

### Modelo de dados

**Tabela `siase-os-eventos`**

| Atributo | Tipo | Descrição |
|---|---|---|
| `id` (PK) | String | UUID do evento |
| `osId` (GSI) | String | ID da OS (índice para consulta) |
| `osNumero` | String | Número legível da OS |
| `statusAnterior` | String | Status antes da mudança |
| `statusNovo` | String | Status após a mudança |
| `origem` | String | Serviço que gerou o evento |
| `descricao` | String | Descrição da mudança |
| `ocorridoEm` | String | Timestamp ISO-8601 |

**Tabela `siase-execution-fila`** (Execution Service)

| Atributo | Tipo | Descrição |
|---|---|---|
| `osId` (PK) | String | ID da OS |
| `status` | String | Estado na fila |
| `itens` | List | Itens de serviço pendentes |
| `atualizadoEm` | String | Timestamp |

## Consequências

- **Positivo**: zero custo de cluster, escala automática, integração nativa com IAM
- **Positivo**: histórico de eventos é append-only — padrão ideal para DynamoDB
- **Negativo**: queries complexas não são suportadas nativamente (mitigado com GSI)
