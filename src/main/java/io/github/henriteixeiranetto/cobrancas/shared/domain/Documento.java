package io.github.henriteixeiranetto.cobrancas.shared.domain;

/**
 * Documento fiscal brasileiro: CPF (pessoa física) ou CNPJ (pessoa jurídica).
 *
 * <p>A interface é {@code sealed}: só existem esses dois tipos, e o compilador garante
 * que um {@code switch} sobre {@code Documento} trate os dois casos.
 */
public sealed interface Documento permits Cpf, Cnpj {

    String numero();
}
