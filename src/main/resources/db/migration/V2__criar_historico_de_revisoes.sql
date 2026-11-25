-- Auditoria: uma linha por revisão de cada cobrança, com o estado completo naquela revisão.
-- Permite consultar a cobrança como ela era (GET /cob/{txid}?revisao=N), como exige a API Pix.
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
    -- Campos que mudaram em relação à revisão anterior, separados por vírgula (vazio na criação)
    campos_alterados    VARCHAR(300)   NOT NULL,

    CONSTRAINT pk_cobranca_revisao PRIMARY KEY (id),
    -- Também serve de índice para as consultas por txid e revisão
    CONSTRAINT uk_cobranca_revisao_txid_revisao UNIQUE (txid, revisao),
    CONSTRAINT fk_cobranca_revisao_cobranca FOREIGN KEY (txid) REFERENCES cobranca (txid)
);

-- Cobranças criadas antes desta migração: registra o estado atual de cada uma.
-- As revisões anteriores não foram guardadas e, por isso, não podem ser recuperadas.
INSERT INTO cobranca_revisao (txid, revisao, registrada_em, status, expiracao_segundos, valor_original, chave,
                              devedor_cpf, devedor_cnpj, devedor_nome, solicitacao_pagador, campos_alterados)
SELECT txid, revisao, CASE WHEN revisao = 0 THEN criacao ELSE now() END, status, expiracao_segundos,
       valor_original, chave, devedor_cpf, devedor_cnpj, devedor_nome, solicitacao_pagador, ''
FROM cobranca;
