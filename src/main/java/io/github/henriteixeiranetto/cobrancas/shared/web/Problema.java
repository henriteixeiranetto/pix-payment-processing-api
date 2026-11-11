package io.github.henriteixeiranetto.cobrancas.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Formato das respostas de erro (RFC 9457, {@code application/problem+json}), com o mesmo nome
 * do schema da API Pix. Serve para a documentação OpenAPI: em tempo de execução, quem monta essas
 * respostas é o {@link ApiExceptionHandler}, com o {@code ProblemDetail} do Spring.
 */
@Schema(name = "Problema", description = "Erro no formato RFC 9457, com a lista de violações da API Pix.")
public record Problema(
        @Schema(example = "https://pix.bcb.gov.br/api/v2/error/CobOperacaoInvalida") String type,
        @Schema(example = "Cobrança inválida.") String title,
        @Schema(example = "400") int status,
        @Schema(example = "A requisição que busca alterar ou criar uma cobrança para pagamento imediato "
                + "não respeita o schema ou está semanticamente errada.") String detail,
        @Schema(example = "/cob") String instance,
        List<Violacao> violacoes) {
}
