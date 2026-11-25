package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DadosCobrancaTest {

    private static final ChavePix CHAVE = ChavePix.de("fulano@example.com");

    @Test
    void guardaOValorSempreComDuasCasasDecimais() {
        var dados = new DadosCobranca(3600, new BigDecimal("10"), CHAVE, null, null);

        assertThat(dados.valorOriginal()).isEqualTo(new BigDecimal("10.00"));
        assertThat(dados).isEqualTo(new DadosCobranca(3600, new BigDecimal("10.00"), CHAVE, null, null));
    }

    @Test
    void aceitaAteDezDigitosAntesDaVirgula() {
        var dados = new DadosCobranca(3600, new BigDecimal("9999999999.99"), CHAVE, null, null);

        assertThat(dados.valorOriginal()).isEqualByComparingTo("9999999999.99");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1.00", "10.001", "12345678901.00"})
    void rejeitaValoresInvalidos(String valor) {
        assertThatThrownBy(() -> new DadosCobranca(3600, new BigDecimal(valor), CHAVE, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "valor.original");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejeitaExpiracaoQueNaoSejaPositiva(int expiracao) {
        assertThatThrownBy(() -> new DadosCobranca(expiracao, BigDecimal.TEN, CHAVE, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "calendario.expiracao");
    }

    @Test
    void informaQuaisCamposMudaram() {
        var antes = new DadosCobranca(3600, new BigDecimal("100.00"), CHAVE, null, "Pedido 123");
        var depois = new DadosCobranca(7200, new BigDecimal("100.00"), CHAVE, null, "Pedido 124");

        assertThat(antes.camposAlteradosEm(depois)).containsExactly("calendario.expiracao", "solicitacaoPagador");
        assertThat(antes.camposAlteradosEm(antes)).isEmpty();
    }

    @Test
    void limitaASolicitacaoAoPagadorA140Caracteres() {
        assertThatThrownBy(() -> new DadosCobranca(3600, BigDecimal.TEN, CHAVE, null, "a".repeat(141)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "solicitacaoPagador");
    }
}
