package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CobrancaTest {

    private static final Instant AGORA = Instant.parse("2026-11-06T12:00:00Z");
    private static final ChavePix CHAVE = ChavePix.de("fulano@example.com");
    private static final Devedor DEVEDOR = Devedor.de("12345678909", null, "Francisco da Silva");

    @Test
    void nasceAtivaNaRevisaoZero() {
        Cobranca cobranca = novaCobranca();

        assertThat(cobranca.status()).isEqualTo(StatusCobranca.ATIVA);
        assertThat(cobranca.revisao()).isZero();
    }

    @Test
    void substituirPelosMesmosDadosNaoAlteraNada() {
        Cobranca cobranca = novaCobranca();

        boolean alterou = cobranca.substituirDados(dados("100.00"));

        assertThat(alterou).isFalse();
        assertThat(cobranca.revisao()).isZero();
    }

    @Test
    void substituirPorDadosDiferentesIncrementaARevisao() {
        Cobranca cobranca = novaCobranca();

        boolean alterou = cobranca.substituirDados(dados("150.00"));

        assertThat(alterou).isTrue();
        assertThat(cobranca.revisao()).isEqualTo(1);
        assertThat(cobranca.dados().valorOriginal()).isEqualByComparingTo("150.00");
    }

    @Test
    void revisarAlteraApenasOsCamposInformados() {
        Cobranca cobranca = novaCobranca();

        cobranca.revisar(revisaoDoValor("80.00"));

        assertThat(cobranca.revisao()).isEqualTo(1);
        assertThat(cobranca.dados().valorOriginal()).isEqualByComparingTo("80.00");
        assertThat(cobranca.dados().devedor()).isEqualTo(DEVEDOR);
        assertThat(cobranca.dados().solicitacaoPagador()).isEqualTo("Pedido 123");
    }

    @Test
    void revisaoQueNaoMudaNadaNaoIncrementaARevisao() {
        Cobranca cobranca = novaCobranca();

        cobranca.revisar(revisaoDoValor("100.00"));

        assertThat(cobranca.revisao()).isZero();
    }

    @Test
    void removerMudaOStatusEIncrementaARevisao() {
        Cobranca cobranca = novaCobranca();

        cobranca.revisar(remocao());

        assertThat(cobranca.status()).isEqualTo(StatusCobranca.REMOVIDA_PELO_USUARIO_RECEBEDOR);
        assertThat(cobranca.revisao()).isEqualTo(1);
    }

    @Test
    void naoPermiteRemoverEAlterarCamposNaMesmaRevisao() {
        Cobranca cobranca = novaCobranca();
        var removerEAlterar = new RevisaoCobranca(null, new BigDecimal("80.00"), null, null, null, true);

        assertThatThrownBy(() -> cobranca.revisar(removerEAlterar))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasFieldOrPropertyWithValue("propriedade", "status");
        assertThat(cobranca.status()).isEqualTo(StatusCobranca.ATIVA);
    }

    @Test
    void naoPermiteAlterarCobrancaQueNaoEstaAtiva() {
        Cobranca cobranca = novaCobranca();
        cobranca.revisar(remocao());

        assertThatThrownBy(() -> cobranca.revisar(revisaoDoValor("80.00")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("REMOVIDA_PELO_USUARIO_RECEBEDOR");
        assertThatThrownBy(() -> cobranca.substituirDados(dados("80.00")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void expiraQuandoOPrazoTermina() {
        Cobranca cobranca = novaCobranca(); // expiração de 3600 segundos

        assertThat(cobranca.estaExpirada(AGORA.plusSeconds(3599))).isFalse();
        assertThat(cobranca.estaExpirada(AGORA.plusSeconds(3600))).isTrue();
    }

    private static Cobranca novaCobranca() {
        return Cobranca.criar(Txid.gerar(), dados("100.00"), "pix.example.com/qr/v2/abc", "000201", AGORA);
    }

    private static DadosCobranca dados(String valor) {
        return new DadosCobranca(3600, new BigDecimal(valor), CHAVE, DEVEDOR, "Pedido 123");
    }

    private static RevisaoCobranca revisaoDoValor(String valor) {
        return new RevisaoCobranca(null, new BigDecimal(valor), null, null, null, false);
    }

    private static RevisaoCobranca remocao() {
        return new RevisaoCobranca(null, null, null, null, null, true);
    }
}
