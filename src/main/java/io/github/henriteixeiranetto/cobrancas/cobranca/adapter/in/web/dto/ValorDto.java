package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Valor em reais como texto, no formato da API Pix (por exemplo "123.45").
 * Usar texto evita perda de precisão que poderia ocorrer com números de ponto flutuante no JSON.
 */
@Schema(name = "Valor")
public record ValorDto(
        @Schema(description = "Valor em reais, com 2 casas decimais separadas por ponto.", example = "150.00")
        @NotNull @Pattern(regexp = "\\d{1,10}\\.\\d{2}") String original) {
}
