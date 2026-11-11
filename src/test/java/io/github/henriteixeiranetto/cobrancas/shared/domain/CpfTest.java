package io.github.henriteixeiranetto.cobrancas.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678909", "52998224725", "11144477735"})
    void aceitaCpfsValidos(String numero) {
        assertThat(Cpf.ehValido(numero)).isTrue();
        assertThat(new Cpf(numero).numero()).isEqualTo(numero);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "12345678900",    // dígito verificador errado
            "11111111111",    // todos os dígitos iguais
            "00000000000",
            "1234567890",     // curto
            "123456789012",   // longo
            "123.456.789-09", // com pontuação
            "1234567890a"})
    void rejeitaCpfsInvalidos(String numero) {
        assertThat(Cpf.ehValido(numero)).isFalse();
        assertThatThrownBy(() -> new Cpf(numero)).isInstanceOf(IllegalArgumentException.class);
    }
}
