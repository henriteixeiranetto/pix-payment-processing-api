package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.swagger.v3.oas.annotations.media.Schema;

/** Cobrança devolvida pela API (schema "CobGerada" da API Pix). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"calendario", "txid", "revisao", "location", "status", "devedor", "valor", "chave",
        "solicitacaoPagador", "pixCopiaECola"})
@Schema(name = "CobGerada", description = "Cobrança imediata, como devolvida pela API.", example = """
        {
          "calendario": { "criacao": "2026-11-06T13:18:02.710Z", "expiracao": 3600 },
          "txid": "pedido20261106000000000000001",
          "revisao": 0,
          "location": "pix.example.com/qr/v2/9569264b4ceb498e914173c5443d40a3",
          "status": "ATIVA",
          "devedor": { "cpf": "12345678909", "nome": "Francisco da Silva" },
          "valor": { "original": "150.00" },
          "chave": "financeiro@example.com",
          "solicitacaoPagador": "Pedido 123",
          "pixCopiaECola": "00020101021226760014br.gov.bcb.pix2554pix.example.com/qr/v2/9569264b4ceb498e914173c5443d40a35204000053039865802BR5917Loja Exemplo LTDA6009Sao Paulo62070503***6304D57E"
        }""")
public record CobGeradaResponse(
        CalendarioResponse calendario,
        String txid,
        int revisao,
        String location,
        StatusCobranca status,
        DevedorDto devedor,
        ValorDto valor,
        String chave,
        String solicitacaoPagador,
        String pixCopiaECola) {
}
