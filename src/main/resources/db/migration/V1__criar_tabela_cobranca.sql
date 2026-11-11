-- Cobranças imediatas (recurso "cob" da API Pix).
-- O txid é a chave primária: ele é o identificador de negócio da cobrança e não muda.
CREATE TABLE cobranca (
    txid                VARCHAR(35)    NOT NULL,
    revisao             INTEGER        NOT NULL,
    status              VARCHAR(40)    NOT NULL,
    criacao             TIMESTAMPTZ    NOT NULL,
    expiracao_segundos  INTEGER        NOT NULL,
    valor_original      NUMERIC(12, 2) NOT NULL,
    chave               VARCHAR(77)    NOT NULL,
    devedor_cpf         VARCHAR(11),
    devedor_cnpj        VARCHAR(14),
    devedor_nome        VARCHAR(200),
    solicitacao_pagador VARCHAR(140),
    location            VARCHAR(77)    NOT NULL,
    pix_copia_e_cola    VARCHAR(512)   NOT NULL,
    -- Controle de concorrência otimista (@Version no JPA)
    versao              BIGINT         NOT NULL,

    CONSTRAINT pk_cobranca PRIMARY KEY (txid),
    CONSTRAINT uk_cobranca_location UNIQUE (location),
    CONSTRAINT ck_cobranca_status CHECK (
        status IN ('ATIVA', 'CONCLUIDA', 'REMOVIDA_PELO_USUARIO_RECEBEDOR', 'REMOVIDA_PELO_PSP')
    ),
    CONSTRAINT ck_cobranca_valor_positivo CHECK (valor_original > 0),
    CONSTRAINT ck_cobranca_expiracao_positiva CHECK (expiracao_segundos > 0),
    -- Devedor é opcional; se existir, tem nome e exatamente um documento (CPF ou CNPJ)
    CONSTRAINT ck_cobranca_devedor CHECK (
        (devedor_cpf IS NULL AND devedor_cnpj IS NULL AND devedor_nome IS NULL)
        OR (devedor_nome IS NOT NULL AND (devedor_cpf IS NULL) <> (devedor_cnpj IS NULL))
    )
);

-- A consulta GET /cob sempre filtra por período de criação
CREATE INDEX idx_cobranca_criacao ON cobranca (criacao);
CREATE INDEX idx_cobranca_devedor_cpf ON cobranca (devedor_cpf) WHERE devedor_cpf IS NOT NULL;
CREATE INDEX idx_cobranca_devedor_cnpj ON cobranca (devedor_cnpj) WHERE devedor_cnpj IS NOT NULL;
