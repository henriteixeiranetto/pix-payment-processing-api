package io.github.henriteixeiranetto.cobrancas.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

/** Item da lista {@code violacoes} das respostas de erro, no formato da API Pix. */
public record Violacao(
        @Schema(example = "O campo cob.valor.original não respeita o schema.") String razao,
        @Schema(example = "cob.valor.original") String propriedade) {
}
