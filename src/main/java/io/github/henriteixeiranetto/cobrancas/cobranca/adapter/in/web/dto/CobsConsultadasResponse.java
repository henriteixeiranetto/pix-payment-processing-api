package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Resposta de GET /cob (schema "CobsConsultadas" da API Pix). */
@Schema(name = "CobsConsultadas", description = "Resultado da consulta de cobranças por período.")
public record CobsConsultadasResponse(Parametros parametros, List<CobGeradaResponse> cobs) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "ParametrosConsultaCob")
    public record Parametros(Instant inicio, Instant fim, String cpf, String cnpj, StatusCobranca status,
                             PaginacaoResponse paginacao) {
    }

    @Schema(name = "Paginacao")
    public record PaginacaoResponse(int paginaAtual, int itensPorPagina, int quantidadeDePaginas,
                                    long quantidadeTotalDeItens) {
    }
}
