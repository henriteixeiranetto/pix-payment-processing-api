package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

public class CobrancaNaoEncontradaException extends RuntimeException {

    public CobrancaNaoEncontradaException(Txid txid) {
        super("Cobrança não encontrada para o txid " + txid + ".");
    }
}
