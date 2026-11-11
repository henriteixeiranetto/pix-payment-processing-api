package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record CalendarioRequest(
        @Schema(description = "Tempo de vida da cobrança, em segundos. Se omitido, vale 86400 (24 horas).",
                example = "3600")
        @Positive Integer expiracao) {
}
