package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.time.Instant;

/**
 * Cobrança imediata (recurso "cob" da API Pix): um pedido de pagamento com valor definido
 * e prazo de validade, identificado pelo txid.
 *
 * <p>É a raiz do agregado: toda alteração passa por aqui, para que as regras de status e de
 * revisão fiquem em um só lugar e possam ser testadas sem banco de dados nem Spring.
 */
public class Cobranca {

    private final Txid txid;
    private final Instant criacao;
    private final String location;
    private final String pixCopiaECola;
    private int revisao;
    private StatusCobranca status;
    private DadosCobranca dados;

    private Cobranca(Txid txid, Instant criacao, String location, String pixCopiaECola,
                     int revisao, StatusCobranca status, DadosCobranca dados) {
        this.txid = requireNonNull(txid);
        this.criacao = requireNonNull(criacao);
        this.location = requireNonNull(location);
        this.pixCopiaECola = requireNonNull(pixCopiaECola);
        this.revisao = revisao;
        this.status = requireNonNull(status);
        this.dados = requireNonNull(dados);
    }

    /** Toda cobrança nasce ATIVA e na revisão 0. */
    public static Cobranca criar(Txid txid, DadosCobranca dados, String location,
                                 String pixCopiaECola, Instant criacao) {
        return new Cobranca(txid, criacao, location, pixCopiaECola, 0, StatusCobranca.ATIVA, dados);
    }

    /** Recria uma cobrança que já existe, por exemplo ao ler do banco de dados. */
    public static Cobranca restaurar(Txid txid, Instant criacao, String location, String pixCopiaECola,
                                     int revisao, StatusCobranca status, DadosCobranca dados) {
        return new Cobranca(txid, criacao, location, pixCopiaECola, revisao, status, dados);
    }

    /**
     * Substitui todos os dados da cobrança (PUT /cob/{txid} com um txid que já existe).
     * Reenviar exatamente os mesmos dados não muda nada: é isso que torna o PUT idempotente.
     *
     * @return true se algum dado mudou (e a revisão foi incrementada)
     */
    public boolean substituirDados(DadosCobranca novosDados) {
        exigirStatusAtiva();
        return aplicar(novosDados);
    }

    /** Aplica uma revisão (PATCH /cob/{txid}). Cada revisão que altera algo soma 1 à revisão. */
    public void revisar(RevisaoCobranca alteracoes) {
        exigirStatusAtiva();
        if (alteracoes.remover()) {
            if (alteracoes.alteraDados()) {
                throw new RegraDeNegocioException("status",
                        "Não é permitido remover a cobrança e alterar outros campos na mesma requisição.");
            }
            status = StatusCobranca.REMOVIDA_PELO_USUARIO_RECEBEDOR;
            revisao++;
            return;
        }
        aplicar(new DadosCobranca(
                requireNonNullElse(alteracoes.expiracao(), dados.expiracao()),
                requireNonNullElse(alteracoes.valorOriginal(), dados.valorOriginal()),
                requireNonNullElse(alteracoes.chave(), dados.chave()),
                alteracoes.devedor() != null ? alteracoes.devedor() : dados.devedor(),
                alteracoes.solicitacaoPagador() != null ? alteracoes.solicitacaoPagador() : dados.solicitacaoPagador()));
    }

    public Instant expiraEm() {
        return criacao.plusSeconds(dados.expiracao());
    }

    public boolean estaExpirada(Instant agora) {
        return !agora.isBefore(expiraEm());
    }

    private boolean aplicar(DadosCobranca novosDados) {
        if (dados.equals(novosDados)) {
            return false;
        }
        dados = novosDados;
        revisao++;
        return true;
    }

    private void exigirStatusAtiva() {
        if (status != StatusCobranca.ATIVA) {
            throw new RegraDeNegocioException("status",
                    "Somente cobranças ATIVA podem ser alteradas. Status atual: " + status + ".");
        }
    }

    public Txid txid() {
        return txid;
    }

    public Instant criacao() {
        return criacao;
    }

    public String location() {
        return location;
    }

    public String pixCopiaECola() {
        return pixCopiaECola;
    }

    public int revisao() {
        return revisao;
    }

    public StatusCobranca status() {
        return status;
    }

    public DadosCobranca dados() {
        return dados;
    }
}
