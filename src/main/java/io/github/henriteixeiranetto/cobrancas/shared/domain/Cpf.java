package io.github.henriteixeiranetto.cobrancas.shared.domain;

import java.util.regex.Pattern;

/** CPF com 11 dígitos, sem pontuação, e dígitos verificadores válidos. */
public record Cpf(String numero) implements Documento {

    private static final Pattern FORMATO = Pattern.compile("\\d{11}");

    public Cpf {
        if (!ehValido(numero)) {
            throw new IllegalArgumentException("CPF inválido: " + numero);
        }
    }

    public static boolean ehValido(String numero) {
        if (numero == null || !FORMATO.matcher(numero).matches()) {
            return false;
        }
        // Sequências como 11111111111 passam no cálculo, mas não são CPFs válidos
        if (numero.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(numero, 9) == numero.charAt(9) - '0'
                && digitoVerificador(numero, 10) == numero.charAt(10) - '0';
    }

    /** Módulo 11 com pesos decrescentes (10..2 para o 1º dígito, 11..2 para o 2º). */
    private static int digitoVerificador(String numero, int quantidadeDeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDeDigitos; i++) {
            soma += (numero.charAt(i) - '0') * (quantidadeDeDigitos + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
