package io.github.henriteixeiranetto.cobrancas.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "11222333000181",  // numérico (formato tradicional)
            "12ABC34501DE35"}) // alfanumérico (exemplo da Receita Federal)
    void aceitaCnpjsNumericosEAlfanumericos(String numero) {
        assertThat(Cnpj.ehValido(numero)).isTrue();
        assertThat(new Cnpj(numero).numero()).isEqualTo(numero);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "11222333000182",      // dígito verificador errado
            "00000000000000",      // todos os caracteres iguais
            "12abc34501de35",      // letras minúsculas
            "12ABC34501DE3A",      // dígito verificador com letra
            "1122233300018",       // curto
            "11.222.333/0001-81"}) // com pontuação
    void rejeitaCnpjsInvalidos(String numero) {
        assertThat(Cnpj.ehValido(numero)).isFalse();
        assertThatThrownBy(() -> new Cnpj(numero)).isInstanceOf(IllegalArgumentException.class);
    }
}
