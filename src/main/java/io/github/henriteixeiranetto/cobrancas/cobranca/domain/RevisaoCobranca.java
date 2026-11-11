package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import java.math.BigDecimal;

/**
 * Alterações pedidas em uma revisão (PATCH /cob/{txid}). Campos nulos ficam como estão.
 *
 * @param remover true quando o pedido é para mudar o status para REMOVIDA_PELO_USUARIO_RECEBEDOR
 */
public record RevisaoCobranca(
        Integer expiracao,
        BigDecimal valorOriginal,
        ChavePix chave,
        Devedor devedor,
        String solicitacaoPagador,
        boolean remover) {

    public boolean alteraDados() {
        return expiracao != null || valorOriginal != null || chave != null
                || devedor != null || solicitacaoPagador != null;
    }
}
