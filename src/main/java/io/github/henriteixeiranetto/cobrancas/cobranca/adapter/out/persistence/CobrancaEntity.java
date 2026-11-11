package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Devedor;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cnpj;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cpf;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Documento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mapeamento JPA da tabela {@code cobranca}. Fica separado da classe de domínio {@link Cobranca}
 * para que o domínio não dependa de anotações de banco de dados.
 */
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
    private String devedorCpf;
    private String devedorCnpj;
    private String devedorNome;
    private String solicitacaoPagador;
    private String location;
    // Nome explícito: a conversão automática de camelCase geraria "pix_copiaecola",
    // porque não separa letras maiúsculas seguidas ("ECola").
    @Column(name = "pix_copia_e_cola")
    private String pixCopiaECola;

    /**
     * Controle de concorrência otimista: se duas requisições alterarem a mesma cobrança ao mesmo
     * tempo, a segunda a gravar falha em vez de sobrescrever a primeira sem perceber.
     * Como é {@code Long} (e não {@code long}), nulo também indica ao Spring Data que a entidade é nova.
     */
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
        this.solicitacaoPagador = dados.solicitacaoPagador();
        this.location = cobranca.location();
        this.pixCopiaECola = cobranca.pixCopiaECola();
        copiarDevedor(dados.devedor());
    }

    private void copiarDevedor(Devedor devedor) {
        this.devedorCpf = null;
        this.devedorCnpj = null;
        this.devedorNome = null;
        if (devedor == null) {
            return;
        }
        this.devedorNome = devedor.nome();
        switch (devedor.documento()) {
            case Cpf cpf -> this.devedorCpf = cpf.numero();
            case Cnpj cnpj -> this.devedorCnpj = cnpj.numero();
        }
    }

    Cobranca paraDominio() {
        Devedor devedor = null;
        if (devedorNome != null) {
            Documento documento = devedorCpf != null ? new Cpf(devedorCpf) : new Cnpj(devedorCnpj);
            devedor = new Devedor(documento, devedorNome);
        }
        var dados = new DadosCobranca(expiracaoSegundos, valorOriginal, ChavePix.de(chave), devedor, solicitacaoPagador);
        return Cobranca.restaurar(new Txid(txid), criacao, location, pixCopiaECola, revisao, status, dados);
    }
}
