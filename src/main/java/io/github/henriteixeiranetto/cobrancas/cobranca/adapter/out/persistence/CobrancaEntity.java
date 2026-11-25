package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

/** Mapeamento JPA da tabela cobranca. Separado de {@link Cobranca} para o domínio não depender de JPA. */
@Entity
@Table(name = "cobranca")
public class CobrancaEntity {

    @Id
    private String txid;
    private int revisao;
    @Enumerated(EnumType.STRING)
    private StatusCobranca status;
    private Instant criacao;
    private int expiracaoSegundos;
    private BigDecimal valorOriginal;
    private String chave;
    @Embedded
    private DevedorColunas devedor;
    private String solicitacaoPagador;
    private String location;
    // sem o nome explícito o Hibernate gera "pix_copiaecola" (não separa o "EC")
    @Column(name = "pix_copia_e_cola")
    private String pixCopiaECola;

    // lock otimista; Long e não long porque nulo indica ao Spring Data que a entidade é nova
    @Version
    private Long versao;

    protected CobrancaEntity() {
        // exigido pelo JPA
    }

    static CobrancaEntity nova() {
        return new CobrancaEntity();
    }

    void copiarDe(Cobranca cobranca) {
        DadosCobranca dados = cobranca.dados();
        this.txid = cobranca.txid().valor();
        this.revisao = cobranca.revisao();
        this.status = cobranca.status();
        this.criacao = cobranca.criacao();
        this.expiracaoSegundos = dados.expiracao();
        this.valorOriginal = dados.valorOriginal();
        this.chave = dados.chave().valor();
        this.devedor = DevedorColunas.de(dados.devedor());
        this.solicitacaoPagador = dados.solicitacaoPagador();
        this.location = cobranca.location();
        this.pixCopiaECola = cobranca.pixCopiaECola();
    }

    Cobranca paraDominio() {
        var dados = new DadosCobranca(expiracaoSegundos, valorOriginal, ChavePix.de(chave),
                devedor == null ? null : devedor.paraDominio(), solicitacaoPagador);
        return Cobranca.restaurar(new Txid(txid), criacao, location, pixCopiaECola, revisao, status, dados);
    }
}
