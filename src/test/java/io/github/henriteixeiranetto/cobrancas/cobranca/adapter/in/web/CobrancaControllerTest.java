package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.henriteixeiranetto.cobrancas.cobranca.application.CobrancaService;
import io.github.henriteixeiranetto.cobrancas.cobranca.application.CobrancaService.ResultadoCriacao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.pix.brcode.BrCode;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testa só a camada HTTP: formato do JSON, códigos de status e respostas de erro.
 * O serviço é substituído por um mock.
 */
@WebMvcTest(CobrancaController.class)
class CobrancaControllerTest {

    private static final String TIPOS_DE_ERRO = "https://pix.bcb.gov.br/api/v2/error/";
    private static final String TXID = "fc9a4366ff3d4964b5dbc6c91a8722d3";
    private static final Instant CRIACAO = Instant.parse("2026-11-06T12:00:00Z");
    private static final String CORPO_VALIDO = """
            {
              "calendario": { "expiracao": 3600 },
              "devedor": { "cpf": "12345678909", "nome": "Francisco da Silva" },
              "valor": { "original": "123.45" },
              "chave": "7d9f0335-8dcc-4054-9bf9-0dbd61d36906",
              "solicitacaoPagador": "Cobrança dos serviços prestados."
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CobrancaService service;

    @Test
    void postCriaCobrancaComTxidGeradoPeloSistema() throws Exception {
        given(service.criar(any())).willAnswer(chamada -> cobranca(chamada.getArgument(0)));

        mockMvc.perform(post("/cob").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cob/" + TXID))
                .andExpect(jsonPath("$.txid").value(TXID))
                .andExpect(jsonPath("$.revisao").value(0))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.calendario.criacao").value("2026-11-06T12:00:00Z"))
                .andExpect(jsonPath("$.calendario.expiracao").value(3600))
                .andExpect(jsonPath("$.devedor.cpf").value("12345678909"))
                .andExpect(jsonPath("$.devedor.cnpj").doesNotExist())
                .andExpect(jsonPath("$.valor.original").value("123.45"))
                .andExpect(jsonPath("$.chave").value("7d9f0335-8dcc-4054-9bf9-0dbd61d36906"))
                .andExpect(jsonPath("$.pixCopiaECola").value(startsWith("000201")));
    }

    @Test
    void putRespondeCreatedQuandoCriaEOkQuandoACobrancaJaExistia() throws Exception {
        given(service.criarOuSubstituir(eq(new Txid(TXID)), any()))
                .willAnswer(chamada -> new ResultadoCriacao(cobranca(chamada.getArgument(1)), true))
                .willAnswer(chamada -> new ResultadoCriacao(cobranca(chamada.getArgument(1)), false));

        mockMvc.perform(put("/cob/{txid}", TXID).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/cob/{txid}", TXID).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void valorForaDoFormatoGeraErroNoPadraoDaApiPix() throws Exception {
        mockMvc.perform(post("/cob").contentType(MediaType.APPLICATION_JSON).content("""
                        { "calendario": {}, "valor": { "original": "10" }, "chave": "fulano@example.com" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(TIPOS_DE_ERRO + "CobOperacaoInvalida"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("cob.valor.original"));
        verifyNoInteractions(service);
    }

    @Test
    void cpfComDigitoVerificadorErradoGeraViolacaoNoCampoDoDevedor() throws Exception {
        mockMvc.perform(post("/cob").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "calendario": {},
                          "devedor": { "cpf": "12345678900", "nome": "Francisco da Silva" },
                          "valor": { "original": "10.00" },
                          "chave": "fulano@example.com"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("cob.devedor.cpf"))
                .andExpect(jsonPath("$.violacoes[0].razao").value("CPF inválido."));
    }

    @Test
    void jsonMalformadoGeraErroDeRequisicaoInvalida() throws Exception {
        mockMvc.perform(post("/cob").contentType(MediaType.APPLICATION_JSON).content("{ isto não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(TIPOS_DE_ERRO + "CobOperacaoInvalida"));
    }

    @Test
    void consultarCobrancaInexistenteRetorna404() throws Exception {
        given(service.consultar(new Txid(TXID))).willThrow(new CobrancaNaoEncontradaException(new Txid(TXID)));

        mockMvc.perform(get("/cob/{txid}", TXID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(TIPOS_DE_ERRO + "CobNaoEncontrado"));
    }

    @Test
    void txidForaDoFormatoGeraViolacao() throws Exception {
        mockMvc.perform(get("/cob/{txid}", "curto-demais"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("cob.txid"));
    }

    @Test
    void patchComStatusRemovidaPedeOCancelamento() throws Exception {
        given(service.revisar(eq(new Txid(TXID)), any())).willReturn(cobranca(dados()));

        mockMvc.perform(patch("/cob/{txid}", TXID).contentType(MediaType.APPLICATION_JSON).content("""
                        { "status": "REMOVIDA_PELO_USUARIO_RECEBEDOR" }
                        """))
                .andExpect(status().isOk());

        ArgumentCaptor<RevisaoCobranca> revisao = ArgumentCaptor.forClass(RevisaoCobranca.class);
        then(service).should().revisar(eq(new Txid(TXID)), revisao.capture());
        assertThat(revisao.getValue().remover()).isTrue();
        assertThat(revisao.getValue().alteraDados()).isFalse();
    }

    @Test
    void patchNaoAceitaOutrosStatus() throws Exception {
        mockMvc.perform(patch("/cob/{txid}", TXID).contentType(MediaType.APPLICATION_JSON).content("""
                        { "status": "CONCLUIDA" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("cob.status"));
    }

    @Test
    void listarExigeOInicioDoPeriodo() throws Exception {
        mockMvc.perform(get("/cob").param("fim", "2026-11-30T23:59:59Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(TIPOS_DE_ERRO + "CobConsultaInvalida"))
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("inicio"));
    }

    @Test
    void listarRejeitaFimAnteriorAoInicio() throws Exception {
        mockMvc.perform(get("/cob")
                        .param("inicio", "2026-11-30T00:00:00Z")
                        .param("fim", "2026-11-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(TIPOS_DE_ERRO + "CobConsultaInvalida"))
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("fim"));
    }

    @Test
    void listarDevolveOsParametrosEAPaginacao() throws Exception {
        given(service.listar(any(), any()))
                .willAnswer(chamada -> new Pagina<>(List.of(cobranca(dados())), chamada.getArgument(1), 1));

        mockMvc.perform(get("/cob")
                        .param("inicio", "2026-11-01T00:00:00Z")
                        .param("fim", "2026-11-30T23:59:59Z")
                        .param("paginacao.itensPorPagina", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parametros.inicio").value("2026-11-01T00:00:00Z"))
                .andExpect(jsonPath("$.parametros.paginacao.paginaAtual").value(0))
                .andExpect(jsonPath("$.parametros.paginacao.itensPorPagina").value(10))
                .andExpect(jsonPath("$.parametros.paginacao.quantidadeDePaginas").value(1))
                .andExpect(jsonPath("$.parametros.paginacao.quantidadeTotalDeItens").value(1))
                .andExpect(jsonPath("$.cobs[0].txid").value(TXID));
    }

    private static DadosCobranca dados() {
        return new DadosCobranca(3600, new BigDecimal("123.45"), ChavePix.de("fulano@example.com"), null, null);
    }

    private static Cobranca cobranca(DadosCobranca dados) {
        String location = "pix.example.com/qr/v2/" + "a".repeat(32);
        return Cobranca.criar(new Txid(TXID), dados, location,
                BrCode.dinamico(location, "Loja Exemplo LTDA", "Sao Paulo"), CRIACAO);
    }
}
