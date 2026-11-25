package io.github.henriteixeiranetto.cobrancas.shared.domain;

/** CPF (pessoa física) ou CNPJ (pessoa jurídica). */
public sealed interface Documento permits Cpf, Cnpj {

    String numero();
}
