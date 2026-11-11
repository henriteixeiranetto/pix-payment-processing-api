package io.github.henriteixeiranetto.cobrancas.shared.domain;

import java.util.regex.Pattern;

/**
 * CNPJ com 14 caracteres, sem pontuação, e dígitos verificadores válidos.
 *
 * <p>Aceita o CNPJ alfanumérico (IN RFB nº 2.229/2024, emitido a partir de julho de 2026):
 * os 12 primeiros caracteres podem ser letras maiúsculas ou números e os 2 últimos
 * (dígitos verificadores) continuam numéricos. O cálculo é o mesmo módulo 11 de sempre,
 * usando como valor de cada caractere o seu código ASCII menos 48 ('0' = 0, 'A' = 17).
 * Assim, CNPJs numéricos antigos continuam válidos sem nenhum tratamento especial.
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
