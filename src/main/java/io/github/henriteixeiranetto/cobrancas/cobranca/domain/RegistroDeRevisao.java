package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Estado completo da cobrança numa revisão, com a data e os campos alterados.
 *
 * @param camposAlterados nomes da API, como "valor.original"; vazio na revisão 0
 */
public record RegistroDeRevisao(
        Txid txid,
        int revisao,
        Instant registradaEm,
        StatusCobranca status,
        DadosCobranca dados,
        List<String> camposAlterados) {

    public RegistroDeRevisao {
        Objects.requireNonNull(txid, "txid");
        Objects.requireNonNull(registradaEm, "registradaEm");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(dados, "dados");
        camposAlterados = List.copyOf(camposAlterados);
    }

    /** Fotografia do estado atual da cobrança. */
    public static RegistroDeRevisao de(Cobranca cobranca, List<String> camposAlterados, Instant registradaEm) {
        return new RegistroDeRevisao(cobranca.txid(), cobranca.revisao(), registradaEm,
                cobranca.status(), cobranca.dados(), camposAlterados);
    }
}
