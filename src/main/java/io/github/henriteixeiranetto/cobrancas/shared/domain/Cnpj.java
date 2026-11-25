package io.github.henriteixeiranetto.cobrancas.shared.domain;

import java.util.regex.Pattern;

/**
 * CNPJ de 14 caracteres, numérico ou alfanumérico (IN RFB 2.229/2024): 12 posições [0-9A-Z]
 * e 2 dígitos verificadores. No módulo 11 cada caractere vale o ASCII menos 48, então o mesmo
 * cálculo serve para os dois formatos.
 */
public record Cnpj(String numero) implements Documento {

    private static final Pattern FORMATO = Pattern.compile("[0-9A-Z]{12}\\d{2}");
    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (!ehValido(numero)) {
            throw new IllegalArgumentException("CNPJ inválido: " + numero);
        }
    }

    public static boolean ehValido(String numero) {
        if (numero == null || !FORMATO.matcher(numero).matches()) {
            return false;
        }
        if (numero.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(numero, PESOS_PRIMEIRO_DIGITO) == numero.charAt(12) - '0'
                && digitoVerificador(numero, PESOS_SEGUNDO_DIGITO) == numero.charAt(13) - '0';
    }

    private static int digitoVerificador(String numero, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (numero.charAt(i) - '0') * pesos[i]; // código ASCII - 48
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
