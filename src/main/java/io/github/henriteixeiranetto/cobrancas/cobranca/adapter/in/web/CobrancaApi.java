package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web;

import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobGeradaResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobRevisadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobSolicitadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobsConsultadasResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.HistoricoRevisoesResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.shared.web.Problema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Documentação OpenAPI dos endpoints /cob, separada para o controller ficar só com o código. */
@Tag(name = "Cob", description = "Cobranças imediatas")
public interface CobrancaApi {

    String DESCRICAO_TXID = "Identificador da cobrança: de 26 a 35 letras ou números.";
    String EXEMPLO_TXID = "pedido20261106000000000000001";
    String JSON = MediaType.APPLICATION_JSON_VALUE;
    String PROBLEMA_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    @Operation(summary = "Criar cobrança imediata com txid informado pelo cliente",
            description = "Idempotente: repetir a mesma requisição não cria outra cobrança.")
    @ApiResponse(responseCode = "201", description = "Cobrança criada.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobGeradaResponse.class)))
    @ApiResponse(responseCode = "200",
            description = "A cobrança já existia: os dados foram substituídos ou a requisição foi repetida.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobGeradaResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos, ou a cobrança existe e não está ATIVA.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    @ApiResponse(responseCode = "409", description = "Outra requisição alterou a mesma cobrança ao mesmo tempo.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    ResponseEntity<CobGeradaResponse> criarComTxid(
            @Parameter(description = DESCRICAO_TXID, example = EXEMPLO_TXID) String txid,
            CobSolicitadaRequest request);

    @Operation(summary = "Criar cobrança imediata com txid gerado pelo sistema")
    @ApiResponse(responseCode = "201", description = "Cobrança criada, com um txid de 32 caracteres.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobGeradaResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    ResponseEntity<CobGeradaResponse> criar(CobSolicitadaRequest request);

    @Operation(summary = "Consultar cobrança imediata",
            description = "Com o parâmetro revisao, devolve a cobrança como ela era naquela revisão.")
    @ApiResponse(responseCode = "200", description = "Dados da cobrança.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobGeradaResponse.class)))
    @ApiResponse(responseCode = "400", description = "O txid não tem de 26 a 35 letras ou números, ou a revisão não existe.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    @ApiResponse(responseCode = "404", description = "Não existe cobrança com esse txid.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    CobGeradaResponse consultar(
            @Parameter(description = DESCRICAO_TXID, example = EXEMPLO_TXID) String txid,
            @Parameter(description = "Revisão desejada (a 0 é a criação). Se omitida, devolve a revisão atual.")
            Integer revisao);

    @Operation(summary = "Consultar o histórico de revisões da cobrança",
            description = "Mostra quando cada revisão foi registrada e quais campos mudaram. "
                    + "É uma extensão deste projeto: não faz parte da API Pix oficial.")
    @ApiResponse(responseCode = "200", description = "Histórico da cobrança, da revisão 0 até a atual.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = HistoricoRevisoesResponse.class)))
    @ApiResponse(responseCode = "400", description = "O txid não tem de 26 a 35 letras ou números.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    @ApiResponse(responseCode = "404", description = "Não existe cobrança com esse txid.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    HistoricoRevisoesResponse listarRevisoes(@Parameter(description = DESCRICAO_TXID, example = EXEMPLO_TXID) String txid);

    @Operation(summary = "Revisar cobrança imediata",
            description = "Altera campos de uma cobrança ATIVA ou a cancela com status REMOVIDA_PELO_USUARIO_RECEBEDOR.")
    @ApiResponse(responseCode = "200", description = "Cobrança revisada. A revisão sobe 1 quando algo muda.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobGeradaResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos, ou a cobrança não está ATIVA.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    @ApiResponse(responseCode = "404", description = "Não existe cobrança com esse txid.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    @ApiResponse(responseCode = "409", description = "Outra requisição alterou a mesma cobrança ao mesmo tempo.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    CobGeradaResponse revisar(
            @Parameter(description = DESCRICAO_TXID, example = EXEMPLO_TXID) String txid,
            CobRevisadaRequest request);

    @Operation(summary = "Consultar lista de cobranças imediatas por período de criação")
    @ApiResponse(responseCode = "200", description = "Cobranças encontradas, com os parâmetros e a paginação.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CobsConsultadasResponse.class)))
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos, como fim anterior ao início.",
            content = @Content(mediaType = PROBLEMA_JSON, schema = @Schema(implementation = Problema.class)))
    CobsConsultadasResponse listar(
            @Parameter(description = "Início do período de criação (ISO 8601, em UTC).", example = "2026-01-01T00:00:00Z")
            Instant inicio,
            @Parameter(description = "Fim do período de criação (ISO 8601, em UTC).", example = "2030-12-31T23:59:59Z")
            Instant fim,
            @Parameter(description = "CPF do devedor, com 11 dígitos. Não pode ser usado junto com o cnpj.")
            String cpf,
            @Parameter(description = "CNPJ do devedor, com 14 caracteres. Não pode ser usado junto com o cpf.")
            String cnpj,
            @Parameter(description = "Status da cobrança.")
            StatusCobranca status,
            @Parameter(description = "Página desejada. A primeira é a 0.")
            int paginaAtual,
            @Parameter(description = "Itens por página, de 1 a 1000.")
            int itensPorPagina);
}
