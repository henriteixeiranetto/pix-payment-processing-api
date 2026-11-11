package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import java.time.Instant;

public record CalendarioResponse(Instant criacao, int expiracao) {
}
