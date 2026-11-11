package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo de PATCH /cob/{txid} (schema "CobRevisada" da API Pix). Todos os campos são opcionais.
 * O único status aceito é REMOVIDA_PELO_USUARIO_RECEBEDOR, usado para cancelar a cobrança.
 */
@Schema(name = "CobRevisada", description = "Alterações em uma cobrança ATIVA: envie só os campos que mudam. "
        + "Para cancelar, envie apenas o status REMOVIDA_PELO_USUARIO_RECEBEDOR.", example = """
        {
          "valor": { "original": "120.00" },
          "solicitacaoPagador": "Desconto aplicado."
        }""")
public record CobRevisadaRequest(
        @Valid CalendarioRequest calendario,
        @Valid DevedorDto devedor,
        @Valid ValorDto valor,
        @Size(max = 77) String chave,
        @Size(max = 140) String solicitacaoPagador,
        @Schema(description = "Use REMOVIDA_PELO_USUARIO_RECEBEDOR para cancelar a cobrança.",
                allowableValues = "REMOVIDA_PELO_USUARIO_RECEBEDOR")
        @Pattern(regexp = "REMOVIDA_PELO_USUARIO_RECEBEDOR") String status) {
}
