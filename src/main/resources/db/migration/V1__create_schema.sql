CREATE TABLE ordens_servico (
    id                  UUID PRIMARY KEY,
    numero              VARCHAR(32)  NOT NULL UNIQUE,
    cliente_id          UUID         NOT NULL,
    veiculo             VARCHAR(120) NOT NULL,
    descricao_problema  TEXT,
    status              VARCHAR(32)  NOT NULL,
    criado_em           TIMESTAMPTZ  NOT NULL,
    atualizado_em       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_ordens_cliente ON ordens_servico (cliente_id);
CREATE INDEX idx_ordens_status  ON ordens_servico (status);
