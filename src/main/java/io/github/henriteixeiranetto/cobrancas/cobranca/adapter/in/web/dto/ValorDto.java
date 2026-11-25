package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Valor como texto ("123.45"), igual à API Pix: evita imprecisão de ponto flutuante no JSON. */
@Schema(name = "Valor")
public record ValorDto(
        @Schema(description = "Valor em reais, com 2 casas decimais separadas por ponto.", example = "150.00")
        @NotNull @Pattern(regexp = "\\d{1,10}\\.\\d{2}") String original) {
}
