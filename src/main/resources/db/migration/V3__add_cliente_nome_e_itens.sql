ALTER TABLE ordens_servico
    ADD COLUMN cliente_nome VARCHAR(255),
    ADD COLUMN itens_servico_json TEXT,
    ADD COLUMN itens_peca_json TEXT;
