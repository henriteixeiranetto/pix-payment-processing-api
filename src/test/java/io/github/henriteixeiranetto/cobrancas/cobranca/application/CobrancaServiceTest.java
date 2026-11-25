package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.github.henriteixeiranetto.cobrancas.cobranca.application.CobrancaService.ResultadoCriacao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Devedor;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.config.PixProperties;
import io.github.henriteixeiranetto.cobrancas.shared.domain.ConsultaInvalidaException;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class CobrancaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-06T12:00:00Z");
    private static final ChavePix CHAVE = ChavePix.de("fulano@example.com");

    private final CobrancaRepositoryEmMemoria repository = new CobrancaRepositoryEmMemoria();
    private final HistoricoDeRevisoesEmMemoria historico = new HistoricoDeRevisoesEmMemoria();
    private final PixProperties pix = new PixProperties(
            new PixProperties.Recebedor("Loja Exemplo LTDA", "Sao Paulo"), "pix.example.com/qr/v2");
    private final CobrancaService service =
            new CobrancaService(repository, historico, pix, Clock.fixed(AGORA, ZoneOffset.UTC));

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
        assertThat(historico.listar(txid)).hasSize(1); // nenhuma revisão nova
    }

    @Test
    void putComDadosDiferentesSubstituiEIncrementaARevisao() {
        Txid txid = Txid.gerar();
        service.criarOuSubstituir(txid, dados("100.00", "12345678909"));

        ResultadoCriacao resultado = service.criarOuSubstituir(txid, dados("250.00", "12345678909"));

        assertThat(resultado.criada()).isFalse();
        assertThat(resultado.cobranca().revisao()).isEqualTo(1);
        assertThat(service.consultar(txid).dados().valorOriginal()).isEqualByComparingTo("250.00");
        assertThat(historico.listar(txid)).last()
                .extracting(RegistroDeRevisao::camposAlterados)
                .isEqualTo(List.of("valor.original"));
    }

    @Test
    void consultarTxidInexistenteLancaExcecao() {
        assertThatThrownBy(() -> service.consultar(Txid.gerar()))
                .isInstanceOf(CobrancaNaoEncontradaException.class);
    }

    @Test
    void revisarCancelaACobranca() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        service.revisar(cobranca.txid(), remocao());

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

    // ---- Auditoria (histórico de revisões)

    @Test
    void criarRegistraARevisaoZeroNoHistorico() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        assertThat(historico.listar(cobranca.txid())).singleElement().satisfies(registro -> {
            assertThat(registro.revisao()).isZero();
            assertThat(registro.registradaEm()).isEqualTo(cobranca.criacao());
            assertThat(registro.camposAlterados()).isEmpty();
        });
    }

    @Test
    void cadaRevisaoFicaRegistradaComOsCamposQueMudaram() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        service.revisar(cobranca.txid(), revisaoDoValor("80.00"));
        service.revisar(cobranca.txid(), remocao());

        assertThat(historico.listar(cobranca.txid()))
                .extracting(RegistroDeRevisao::revisao, RegistroDeRevisao::camposAlterados)
                .containsExactly(
                        tuple(0, List.of()),
                        tuple(1, List.of("valor.original")),
                        tuple(2, List.of("status")));
    }

    @Test
    void revisaoQueNaoMudaNadaNaoGeraRegistro() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        service.revisar(cobranca.txid(), revisaoDoValor("100.00"));

        assertThat(historico.listar(cobranca.txid())).hasSize(1);
    }

    @Test
    void consultarUmaRevisaoAnteriorDevolveACobrancaComoEla() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));
        service.revisar(cobranca.txid(), revisaoDoValor("80.00"));

        Cobranca naRevisaoZero = service.consultar(cobranca.txid(), 0);
        Cobranca naRevisaoUm = service.consultar(cobranca.txid(), 1);

        assertThat(naRevisaoZero.revisao()).isZero();
        assertThat(naRevisaoZero.dados().valorOriginal()).isEqualByComparingTo("100.00");
        assertThat(naRevisaoUm.dados().valorOriginal()).isEqualByComparingTo("80.00");
    }

    @Test
    void consultarRevisaoInexistenteGeraConsultaInvalida() {
        Cobranca cobranca = service.criar(dados("100.00", "12345678909"));

        assertThatThrownBy(() -> service.consultar(cobranca.txid(), 5))
                .isInstanceOf(ConsultaInvalidaException.class)
                .hasFieldOrPropertyWithValue("propriedade", "revisao");
    }

    @Test
    void historicoDeCobrancaInexistenteLancaExcecao() {
        assertThatThrownBy(() -> service.listarRevisoes(Txid.gerar()))
                .isInstanceOf(CobrancaNaoEncontradaException.class);
    }

    private static DadosCobranca dados(String valor, String cpfDevedor) {
        return new DadosCobranca(3600, new BigDecimal(valor), CHAVE,
                Devedor.de(cpfDevedor, null, "Francisco da Silva"), "Pedido 123");
    }

    private static RevisaoCobranca revisaoDoValor(String valor) {
        return new RevisaoCobranca(null, new BigDecimal(valor), null, null, null, false);
    }

    private static RevisaoCobranca remocao() {
        return new RevisaoCobranca(null, null, null, null, null, true);
    }
}
