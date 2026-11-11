package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

/** Status do registro da cobrança, com os mesmos valores da API Pix. */
public enum StatusCobranca {
    /** Gerada, ainda não paga nem removida. */
    ATIVA,
    /** Paga. */
    CONCLUIDA,
    /** O recebedor cancelou a cobrança. */
    REMOVIDA_PELO_USUARIO_RECEBEDOR,
    /** O PSP (a instituição) removeu a cobrança. */
    REMOVIDA_PELO_PSP
}
