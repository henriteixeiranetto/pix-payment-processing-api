package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo de PUT /cob/{txid} e POST /cob (schema "CobSolicitada" da API Pix). */
@Schema(name = "CobSolicitada", description = "Dados para criar uma cobrança imediata.", example = """
        {
          "calendario": { "expiracao": 3600 },
          "devedor": { "cpf": "12345678909", "nome": "Francisco da Silva" },
          "valor": { "original": "150.00" },
          "chave": "financeiro@example.com",
          "solicitacaoPagador": "Pedido 123"
        }""")
public record CobSolicitadaRequest(
        @Valid @NotNull CalendarioRequest calendario,
        @Valid DevedorDto devedor,
        @Valid @NotNull ValorDto valor,
        @Schema(description = "Chave Pix de quem recebe: CPF, CNPJ, e-mail, telefone (+55...) ou chave aleatória.",
                example = "financeiro@example.com")
        @NotBlank @Size(max = 77) String chave,
        @Schema(description = "Texto exibido para quem vai pagar.", example = "Pedido 123")
        @Size(max = 140) String solicitacaoPagador) {
}
