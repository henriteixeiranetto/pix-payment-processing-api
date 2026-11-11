package io.github.henriteixeiranetto.cobrancas.pix;

public enum TipoChavePix {
    CPF,
    CNPJ,
    EMAIL,
    TELEFONE,
    /** Chave aleatória (Endereço Virtual de Pagamento), no formato UUID. */
    EVP
}
