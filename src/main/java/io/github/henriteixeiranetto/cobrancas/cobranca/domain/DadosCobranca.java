package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dados que o recebedor define e pode revisar enquanto a cobrança está ATIVA.
 *
 * @param expiracao          segundos de validade, contados da criação
 * @param valorOriginal      sempre com 2 casas decimais
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

    /** Campos que mudam em relação aos novos dados, com os nomes da API. Vazio se forem iguais. */
    public List<String> camposAlteradosEm(DadosCobranca novos) {
        List<String> alterados = new ArrayList<>();
        if (expiracao != novos.expiracao) {
            alterados.add("calendario.expiracao");
        }
        if (!valorOriginal.equals(novos.valorOriginal)) {
            alterados.add("valor.original");
        }
        if (!chave.equals(novos.chave)) {
            alterados.add("chave");
        }
        if (!Objects.equals(devedor, novos.devedor)) {
            alterados.add("devedor");
        }
        if (!Objects.equals(solicitacaoPagador, novos.solicitacaoPagador)) {
            alterados.add("solicitacaoPagador");
        }
        return List.copyOf(alterados);
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
        // 10 vira 10.00; sem isso o equals do BigDecimal acha 10 e 10.00 diferentes
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
