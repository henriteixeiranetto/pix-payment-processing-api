package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A parte da cobrança que o recebedor define e pode revisar enquanto ela está ATIVA.
 *
 * <p>Como é um record, dois {@code DadosCobranca} com os mesmos valores são iguais. É isso que
 * permite saber se uma revisão mudou algo de fato (e só então incrementar a revisão).
 *
 * @param expiracao          tempo de vida da cobrança, em segundos, contado a partir da criação
 * @param valorOriginal      valor em reais, sempre com 2 casas decimais
 * @param devedor            opcional
 * @param solicitacaoPagador opcional; texto exibido ao pagador
 */
public record DadosCobranca(
        int expiracao,
        BigDecimal valorOriginal,
        ChavePix chave,
        Devedor devedor,
        String solicitacaoPagador) {

    /** 24 horas, o padrão da API Pix. */
    public static final int EXPIRACAO_PADRAO = 86_400;
    public static final int TAMANHO_MAXIMO_SOLICITACAO = 140;
    private static final int DIGITOS_INTEIROS_MAXIMOS = 10;

    public DadosCobranca {
        if (expiracao <= 0) {
            throw new RegraDeNegocioException("calendario.expiracao", "A expiração deve ser maior que zero.");
        }
        valorOriginal = validarValor(valorOriginal);
        if (chave == null) {
            throw new RegraDeNegocioException("chave", "A chave Pix é obrigatória.");
        }
        if (solicitacaoPagador != null && solicitacaoPagador.length() > TAMANHO_MAXIMO_SOLICITACAO) {
            throw new RegraDeNegocioException("solicitacaoPagador",
                    "A solicitação ao pagador deve ter no máximo " + TAMANHO_MAXIMO_SOLICITACAO + " caracteres.");
        }
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new RegraDeNegocioException("valor.original", "O valor original é obrigatório.");
        }
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("valor.original", "O valor original deve ser maior que zero.");
        }
        BigDecimal semZerosAMais = valor.stripTrailingZeros();
        if (semZerosAMais.scale() > 2) {
            throw new RegraDeNegocioException("valor.original", "O valor original deve ter no máximo 2 casas decimais.");
        }
        if (semZerosAMais.precision() - semZerosAMais.scale() > DIGITOS_INTEIROS_MAXIMOS) {
            throw new RegraDeNegocioException("valor.original",
                    "O valor original deve ter no máximo " + DIGITOS_INTEIROS_MAXIMOS + " dígitos antes da vírgula.");
        }
        // Dinheiro sempre com 2 casas: 10 vira 10.00, assim 10 e 10.00 são o mesmo valor
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
