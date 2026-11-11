package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.shared.domain.ConsultaInvalidaException;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Filtros da consulta de cobranças (GET /cob). O período de criação é obrigatório;
 * os demais filtros são opcionais.
 */
public record FiltroCobrancas(Instant inicio, Instant fim, String cpf, String cnpj, StatusCobranca status) {

    private static final Pattern CPF = Pattern.compile("\\d{11}");
    private static final Pattern CNPJ = Pattern.compile("[0-9A-Z]{14}");

    public FiltroCobrancas {
        if (inicio == null) {
            throw new ConsultaInvalidaException("inicio", "O parâmetro inicio é obrigatório.");
        }
        if (fim == null) {
            throw new ConsultaInvalidaException("fim", "O parâmetro fim é obrigatório.");
        }
        if (fim.isBefore(inicio)) {
            throw new ConsultaInvalidaException("fim", "O parâmetro fim não pode ser anterior ao parâmetro inicio.");
        }
        if (cpf != null && cnpj != null) {
            throw new ConsultaInvalidaException("cpf", "Os parâmetros cpf e cnpj não podem ser informados juntos.");
        }
        if (cpf != null && !CPF.matcher(cpf).matches()) {
            throw new ConsultaInvalidaException("cpf", "O parâmetro cpf deve ter 11 dígitos.");
        }
        if (cnpj != null && !CNPJ.matcher(cnpj).matches()) {
            throw new ConsultaInvalidaException("cnpj",
                    "O parâmetro cnpj deve ter 14 caracteres (números ou letras maiúsculas).");
        }
    }
}
