package io.github.henriteixeiranetto.cobrancas.pix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ChavePixTest {

    @ParameterizedTest
    @CsvSource({
            "123e4567-e12b-12d1-a456-426655440000, EVP",
            "+5561988887777, TELEFONE",
            "fulano@example.com, EMAIL",
            "12345678909, CPF",
            "11222333000181, CNPJ",
            "12ABC34501DE35, CNPJ"})
    void identificaOTipoPeloFormato(String valor, TipoChavePix tipoEsperado) {
        assertThat(ChavePix.de(valor).tipo()).isEqualTo(tipoEsperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "chave-invalida",
            "12345678900",        // CPF com dígito verificador errado
            "5561988887777",      // telefone sem o "+"
            "Fulano@Example.com"}) // e-mail com maiúsculas
    void rejeitaChavesInvalidas(String valor) {
        assertThatThrownBy(() -> ChavePix.de(valor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "chave");
    }

    @Test
    void naoAceitaTipoIncoerenteComOValor() {
        assertThatThrownBy(() -> new ChavePix(TipoChavePix.EMAIL, "12345678909"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
