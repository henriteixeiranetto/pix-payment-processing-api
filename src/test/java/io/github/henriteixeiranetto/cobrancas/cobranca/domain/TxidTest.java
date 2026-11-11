package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TxidTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "abcdefghijklmnopqrstuvwxyz",            // 26 caracteres
            "ABCDEFGHIJ0123456789abcdefghijklm12"})  // 35 caracteres
    void aceitaDe26a35CaracteresAlfanumericos(String valor) {
        assertThat(new Txid(valor).valor()).isEqualTo(valor);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "abcdefghijklmnopqrstuvwxy",             // 25 caracteres
            "ABCDEFGHIJ0123456789abcdefghijklm123",  // 36 caracteres
            "abcdefghijklmnopqrstuvwxy-z"})          // hífen
    void rejeitaFormatoInvalido(String valor) {
        assertThatThrownBy(() -> new Txid(valor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "txid");
    }

    @Test
    void geraTxidsValidosEDiferentes() {
        Txid primeiro = Txid.gerar();
        Txid segundo = Txid.gerar();

        assertThat(primeiro.valor()).hasSize(32).matches("[a-zA-Z0-9]+");
        assertThat(primeiro).isNotEqualTo(segundo);
    }
}
