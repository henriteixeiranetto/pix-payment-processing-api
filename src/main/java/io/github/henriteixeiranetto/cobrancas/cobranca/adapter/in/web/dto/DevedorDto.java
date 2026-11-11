package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Devedor: informe {@code cpf} ou {@code cnpj} (nunca os dois), sempre com {@code nome}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Devedor", description = "Quem deve pagar: informe cpf ou cnpj (nunca os dois), sempre com o nome.")
public record DevedorDto(
        @Schema(description = "CPF com 11 dígitos, sem pontuação.", example = "12345678909")
        @Pattern(regexp = "\\d{11}") String cpf,
        @Schema(description = "CNPJ com 14 caracteres (numérico ou alfanumérico), sem pontuação.",
                example = "12ABC34501DE35")
        @Pattern(regexp = "[0-9A-Z]{14}") String cnpj,
        @Schema(example = "Francisco da Silva")
        @NotBlank @Size(max = 200) String nome) {
}
