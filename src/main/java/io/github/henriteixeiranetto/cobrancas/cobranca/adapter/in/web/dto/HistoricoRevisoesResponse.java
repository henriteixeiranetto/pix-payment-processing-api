package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Resposta de GET /cob/{txid}/revisoes: o histórico de revisões da cobrança. */
@Schema(name = "HistoricoDeRevisoes", description = "Revisões da cobrança, da criação (revisão 0) até a atual.")
public record HistoricoRevisoesResponse(String txid, List<Revisao> revisoes) {

    @Schema(name = "RevisaoDaCobranca")
    public record Revisao(
            @Schema(example = "1") int revisao,
            @Schema(description = "Quando a revisão foi registrada.") Instant registradaEm,
            StatusCobranca status,
            @Schema(description = "Campos que mudaram em relação à revisão anterior. Vazio na criação.",
                    example = "[\"valor.original\"]")
            List<String> camposAlterados) {
    }
}
