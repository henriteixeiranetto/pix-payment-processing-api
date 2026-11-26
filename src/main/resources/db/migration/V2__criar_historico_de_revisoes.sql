-- Histórico de revisões da cobrança, para o GET /cob/{txid}?revisao=N
CREATE TABLE cobranca_revisao (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    txid                VARCHAR(35)    NOT NULL,
    revisao             INTEGER        NOT NULL,
    registrada_em       TIMESTAMPTZ    NOT NULL,
    status              VARCHAR(40)    NOT NULL,
    expiracao_segundos  INTEGER        NOT NULL,
    valor_original      NUMERIC(12, 2) NOT NULL,
    chave               VARCHAR(77)    NOT NULL,
    devedor_cpf         VARCHAR(11),
    devedor_cnpj        VARCHAR(14),
    devedor_nome        VARCHAR(200),
    solicitacao_pagador VARCHAR(140),
    campos_alterados    VARCHAR(300)   NOT NULL, -- separados por vírgula; vazio na revisão 0

    CONSTRAINT pk_cobranca_revisao PRIMARY KEY (id),
    CONSTRAINT uk_cobranca_revisao_txid_revisao UNIQUE (txid, revisao), -- também serve de índice
    CONSTRAINT fk_cobranca_revisao_cobranca FOREIGN KEY (txid) REFERENCES cobranca (txid)
);

-- Cobranças que já existiam entram só com o estado atual; as revisões de antes não foram guardadas
INSERT INTO cobranca_revisao (txid, revisao, registrada_em, status, expiracao_segundos, valor_original, chave,
                              devedor_cpf, devedor_cnpj, devedor_nome, solicitacao_pagador, campos_alterados)
SELECT txid, revisao, CASE WHEN revisao = 0 THEN criacao ELSE now() END, status, expiracao_segundos,
       valor_original, chave, devedor_cpf, devedor_cnpj, devedor_nome, solicitacao_pagador, ''
FROM cobranca;
