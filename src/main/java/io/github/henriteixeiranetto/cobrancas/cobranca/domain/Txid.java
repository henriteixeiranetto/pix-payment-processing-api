package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Identificador da transação. Na cobrança imediata ele tem de 26 a 35 caracteres alfanuméricos
 * e é o que permite ao recebedor conciliar o pagamento recebido com a cobrança que emitiu.
 */
public record Txid(String valor) {

    private static final Pattern FORMATO = Pattern.compile("[a-zA-Z0-9]{26,35}");

    public Txid {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new RegraDeNegocioException("txid", "O txid deve ter de 26 a 35 caracteres alfanuméricos.");
        }
    }

    /** Gera um txid aleatório de 32 caracteres (UUID sem hífens). */
    public static Txid gerar() {
        return new Txid(UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public String toString() {
        return valor;
    }
}
