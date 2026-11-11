package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.henriteixeiranetto.cobrancas.cobranca.application.CobrancaService.ResultadoCriacao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Devedor;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.config.PixProperties;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CobrancaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-06T12:00:00Z");
    private static final ChavePix CHAVE = ChavePix.de("fulano@example.com");

    private final CobrancaRepositoryEmMemoria repository = new CobrancaRepositoryEmMemoria();
    private final PixProperties pix = new PixProperties(
            new PixProperties.Recebedor("Loja Exemplo LTDA", "Sao Paulo"), "pix.example.com/qr/v2");
    private final CobrancaService service =
            new CobrancaService(repository, pix, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void criarGeraTxidLocationEPixCopiaECola() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        assertThat(cobranca.txid().valor()).hasSize(32);
        assertThat(cobranca.criacao()).isEqualTo(AGORA);
        assertThat(cobranca.location()).startsWith("pix.example.com/qr/v2/").hasSize(54);
        assertThat(cobranca.pixCopiaECola())
                .contains(cobranca.location())
                .contains("5917Loja Exemplo LTDA");
        assertThat(repository.buscarPorTxid(cobranca.txid())).isPresent();
    }

    @Test
    void putCriaACobrancaQuandoOTxidAindaNaoExiste() {
        Txid txid = Txid.gerar();

        ResultadoCriacao resultado = service.criarOuSubstituir(txid, dados("100.00", "12345678909"));

        assertThat(resultado.criada()).isTrue();
        assertThat(resultado.cobranca().txid()).isEqualTo(txid);
    }

    @Test
    void putRepetidoComOsMesmosDadosEhIdempotente() {
        Txid txid = Txid.gerar();
        ResultadoCriacao primeira = service.criarOuSubstituir(txid, dados("100.00", "12345678909"));

        ResultadoCriacao repetida = service.criarOuSubstituir(txid, dados("100.00", "12345678909"));

        assertThat(repetida.criada()).isFalse();
        assertThat(repetida.cobranca().revisao()).isZero();
        assertThat(repetida.cobranca().location()).isEqualTo(primeira.cobranca().location());
    }

    @Test
    void putComDadosDiferentesSubstituiEIncrementaARevisao() {
        Txid txid = Txid.gerar();
        service.criarOuSubstituir(txid, dados("100.00", "12345678909"));

        ResultadoCriacao resultado = service.criarOuSubstituir(txid, dados("250.00", "12345678909"));

        assertThat(resultado.criada()).isFalse();
        assertThat(resultado.cobranca().revisao()).isEqualTo(1);
        assertThat(service.consultar(txid).dados().valorOriginal()).isEqualByComparingTo("250.00");
    }

    @Test
    void consultarTxidInexistenteLancaExcecao() {
        assertThatThrownBy(() -> service.consultar(Txid.gerar()))
                .isInstanceOf(CobrancaNaoEncontradaException.class);
    }

    @Test
    void revisarCancelaACobranca() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        service.revisar(cobranca.txid(), new RevisaoCobranca(null, null, null, null, null, true));

        assertThat(service.consultar(cobranca.txid()).status()).isEqualTo(StatusCobranca.REMOVIDA_PELO_USUARIO_RECEBEDOR);
    }

    @Test
    void listarFiltraPorCpfEPagina() {
        service.criar(dados("10.00", "52998224725"));
        service.criar(dados("20.00", "52998224725"));
        service.criar(dados("30.00", "52998224725"));
        service.criar(dados("40.00", "11144477735"));
        var filtro = new FiltroCobrancas(AGORA.minusSeconds(60), AGORA.plusSeconds(60), "52998224725", null, null);

        Pagina<Cobranca> pagina = service.listar(filtro, new Paginacao(0, 2));

        assertThat(pagina.quantidadeTotalDeItens()).isEqualTo(3);
        assertThat(pagina.quantidadeDePaginas()).isEqualTo(2);
        assertThat(pagina.itens()).hasSize(2);
    }

    private static DadosCobranca dados(String valor, String cpfDevedor) {
        return new DadosCobranca(3600, new BigDecimal(valor), CHAVE,
                Devedor.de(cpfDevedor, null, "Francisco da Silva"), "Pedido 123");
    }
}
