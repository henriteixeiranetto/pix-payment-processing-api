package io.github.henriteixeiranetto.cobrancas.cobranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.henriteixeiranetto.cobrancas.TestcontainersConfiguration;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de ponta a ponta: HTTP → controller → serviço → JPA → PostgreSQL real (via Testcontainers),
 * incluindo as migrações do Flyway. Sem Docker disponível, estes testes são pulados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class CobrancaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cicloDeVidaCompletoDeUmaCobranca() throws Exception {
        String txid = Txid.gerar().valor();

        criarComPut(txid, "12345678909", "150.00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revisao").value(0))
                .andExpect(jsonPath("$.status").value("ATIVA"));

        // Repetir o PUT (por exemplo, após um timeout) não cria outra cobrança
        criarComPut(txid, "12345678909", "150.00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisao").value(0));

        revisar(txid, """
                { "valor": { "original": "120.00" } }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisao").value(1))
                .andExpect(jsonPath("$.valor.original").value("120.00"));

        // A consulta lê do banco: confirma que a revisão foi gravada
        mockMvc.perform(get("/cob/{txid}", txid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor.original").value("120.00"))
                .andExpect(jsonPath("$.devedor.cpf").value("12345678909"))
                .andExpect(jsonPath("$.devedor.nome").value("Francisco da Silva"));

        revisar(txid, """
                { "status": "REMOVIDA_PELO_USUARIO_RECEBEDOR" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REMOVIDA_PELO_USUARIO_RECEBEDOR"))
                .andExpect(jsonPath("$.revisao").value(2));

        revisar(txid, """
                { "valor": { "original": "99.00" } }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violacoes[0].propriedade").value("cob.status"));
    }

    @Test
    void consultaFiltraPorPeriodoECpfComPaginacao() throws Exception {
        // A criação é gravada em milissegundos; truncar o início evita perder uma cobrança
        // criada no mesmo milissegundo em que o teste começou.
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String cpf = "52998224725"; // CPF usado só neste teste
        criarComPut(Txid.gerar().valor(), cpf, "10.00").andExpect(status().isCreated());
        criarComPut(Txid.gerar().valor(), cpf, "20.00").andExpect(status().isCreated());
        criarComPut(Txid.gerar().valor(), cpf, "30.00").andExpect(status().isCreated());
        criarComPut(Txid.gerar().valor(), "11144477735", "40.00").andExpect(status().isCreated());
        Instant fim = Instant.now().plusSeconds(1);

        List<String> primeiraPagina = valoresDaPagina(inicio, fim, cpf, 0);
        List<String> segundaPagina = valoresDaPagina(inicio, fim, cpf, 1);

        assertThat(primeiraPagina).hasSize(2);
        assertThat(segundaPagina).hasSize(1);
        // Juntas, as páginas trazem as três cobranças do CPF, sem repetir nem perder nenhuma.
        // A ordem exata não é verificada: cobranças criadas no mesmo milissegundo são desempatadas pelo txid.
        assertThat(Stream.concat(primeiraPagina.stream(), segundaPagina.stream()))
                .containsExactlyInAnyOrder("10.00", "20.00", "30.00");
    }

    @Test
    void documentacaoOpenApiDeclaraCodigosDeRespostaEExemplos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/cob'].post.tags[0]").value("Cob"))
                // Códigos de resposta: 201 ao criar, 200 quando já existia, erros em problem+json
                .andExpect(jsonPath("$.paths['/cob/{txid}'].put.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/cob/{txid}'].put.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/cob/{txid}'].put.responses['400'].content['application/problem+json']").exists())
                .andExpect(jsonPath("$.paths['/cob/{txid}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/cob'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/cob'].post.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.paths['/cob'].get.responses['400']").exists())
                // Exemplos que o Swagger já deixa preenchidos no "Try it out"
                .andExpect(jsonPath("$.paths['/cob/{txid}'].put.parameters[0].example").value("pedido20261106000000000000001"))
                .andExpect(jsonPath("$.components.schemas.CobSolicitada.example.valor.original").value("150.00"));
    }

    @Test
    void consultarTxidInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/cob/{txid}", Txid.gerar().valor()))
                .andExpect(status().isNotFound());
    }

    private ResultActions criarComPut(String txid, String cpfDevedor, String valor) throws Exception {
        return mockMvc.perform(put("/cob/{txid}", txid).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "calendario": { "expiracao": 3600 },
                  "devedor": { "cpf": "%s", "nome": "Francisco da Silva" },
                  "valor": { "original": "%s" },
                  "chave": "fulano@example.com"
                }
                """.formatted(cpfDevedor, valor)));
    }

    private List<String> valoresDaPagina(Instant inicio, Instant fim, String cpf, int pagina) throws Exception {
        String json = mockMvc.perform(get("/cob")
                        .param("inicio", inicio.toString())
                        .param("fim", fim.toString())
                        .param("cpf", cpf)
                        .param("paginacao.itensPorPagina", "2")
                        .param("paginacao.paginaAtual", String.valueOf(pagina)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parametros.paginacao.quantidadeTotalDeItens").value(3))
                .andExpect(jsonPath("$.parametros.paginacao.quantidadeDePaginas").value(2))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(json, "$.cobs[*].valor.original");
    }

    private ResultActions revisar(String txid, String corpo) throws Exception {
        return mockMvc.perform(patch("/cob/{txid}", txid).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
