CREATE TABLE IF NOT EXISTS ordens_servico (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero          VARCHAR(30)    NOT NULL UNIQUE,
    cliente_id      UUID           NOT NULL,
    cliente_nome    VARCHAR(255)   NOT NULL,
    cliente_email   VARCHAR(255),
    veiculo_id      UUID           NOT NULL,
    veiculo_placa   VARCHAR(10)    NOT NULL,
    veiculo_modelo  VARCHAR(100),
    status          VARCHAR(30)    NOT NULL DEFAULT 'RECEBIDA',
    observacoes     TEXT,
    total_servicos  NUMERIC(10,2)  NOT NULL DEFAULT 0,
    total_pecas     NUMERIC(10,2)  NOT NULL DEFAULT 0,
    total           NUMERIC(10,2)  NOT NULL DEFAULT 0,
    data_abertura   TIMESTAMP,
    data_fechamento TIMESTAMP,
    criado_em       TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status CHECK (status IN (
        'RECEBIDA','EM_DIAGNOSTICO','AGUARDANDO_APROVACAO',
        'APROVADO','EM_EXECUCAO','FINALIZADA','ENTREGUE','CANCELADA'
    ))
);

CREATE TABLE IF NOT EXISTS itens_servico (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ordem_servico_id        UUID           NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    servico_id              UUID           NOT NULL,
    servico_nome            VARCHAR(255)   NOT NULL,
    preco_unitario          NUMERIC(10,2)  NOT NULL,
    tempo_estimado_minutos  INTEGER,
    observacoes             TEXT,
    data_inicio_execucao    TIMESTAMP,
    data_fim_execucao       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS itens_peca (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ordem_servico_id UUID           NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    peca_id          UUID           NOT NULL,
    peca_codigo      VARCHAR(50)    NOT NULL,
    peca_nome        VARCHAR(255)   NOT NULL,
    quantidade       INTEGER        NOT NULL,
    preco_unitario   NUMERIC(10,2)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_os_status ON ordens_servico(status);
CREATE INDEX IF NOT EXISTS idx_os_cliente_id ON ordens_servico(cliente_id);
CREATE INDEX IF NOT EXISTS idx_os_veiculo_id ON ordens_servico(veiculo_id);
CREATE INDEX IF NOT EXISTS idx_os_numero ON ordens_servico(numero);
