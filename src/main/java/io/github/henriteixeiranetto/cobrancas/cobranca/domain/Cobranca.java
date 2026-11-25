package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.time.Instant;
import java.util.List;

/**
 * Cobrança imediata ("cob" na API Pix). Raiz do agregado: toda mudança de status
 * ou de dados passa por aqui.
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
     * PUT sobre cobrança existente. Com os mesmos dados nada muda, e é isso que deixa o PUT idempotente.
     *
     * @return campos alterados; vazio se nada mudou
     */
    public List<String> substituirDados(DadosCobranca novosDados) {
        exigirStatusAtiva();
        return aplicar(novosDados);
    }

    /**
     * PATCH /cob/{txid}. A revisão só sobe se algo mudar.
     *
     * @return campos alterados; vazio se nada mudou
     */
    public List<String> revisar(RevisaoCobranca alteracoes) {
        exigirStatusAtiva();
        if (alteracoes.remover()) {
            if (alteracoes.alteraDados()) {
                throw new RegraDeNegocioException("status",
                        "Não é permitido remover a cobrança e alterar outros campos na mesma requisição.");
            }
            status = StatusCobranca.REMOVIDA_PELO_USUARIO_RECEBEDOR;
            revisao++;
            return List.of("status");
        }
        return aplicar(new DadosCobranca(
                requireNonNullElse(alteracoes.expiracao(), dados.expiracao()),
                requireNonNullElse(alteracoes.valorOriginal(), dados.valorOriginal()),
                requireNonNullElse(alteracoes.chave(), dados.chave()),
                alteracoes.devedor() != null ? alteracoes.devedor() : dados.devedor(),
                alteracoes.solicitacaoPagador() != null ? alteracoes.solicitacaoPagador() : dados.solicitacaoPagador()));
    }

    /** A cobrança como estava na revisão do registro (txid, criação e location nunca mudam). */
    public Cobranca naRevisao(RegistroDeRevisao registro) {
        if (!registro.txid().equals(txid)) {
            throw new IllegalArgumentException("O registro de revisão pertence a outra cobrança.");
        }
        return new Cobranca(txid, criacao, location, pixCopiaECola, registro.revisao(), registro.status(), registro.dados());
    }

    public Instant expiraEm() {
        return criacao.plusSeconds(dados.expiracao());
    }

    public boolean estaExpirada(Instant agora) {
        return !agora.isBefore(expiraEm());
    }

    private List<String> aplicar(DadosCobranca novosDados) {
        List<String> alterados = dados.camposAlteradosEm(novosDados);
        if (!alterados.isEmpty()) {
            dados = novosDados;
            revisao++;
        }
        return alterados;
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
