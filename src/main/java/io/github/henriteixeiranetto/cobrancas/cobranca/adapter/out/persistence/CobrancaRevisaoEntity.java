package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/** Mapeamento JPA da tabela {@code cobranca_revisao}: uma linha por revisão de cada cobrança. */
@Entity
@Table(name = "cobranca_revisao")
public class CobrancaRevisaoEntity {

    private static final String SEPARADOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String txid;
    private int revisao;
    private Instant registradaEm;
    @Enumerated(EnumType.STRING)
    private StatusCobranca status;
    private int expiracaoSegundos;
    private BigDecimal valorOriginal;
    private String chave;
    @Embedded
    private DevedorColunas devedor;
    private String solicitacaoPagador;
    private String camposAlterados;

    protected CobrancaRevisaoEntity() {
        // exigido pelo JPA
    }

    static CobrancaRevisaoEntity de(RegistroDeRevisao registro) {
        DadosCobranca dados = registro.dados();
        var entidade = new CobrancaRevisaoEntity();
        entidade.txid = registro.txid().valor();
        entidade.revisao = registro.revisao();
        entidade.registradaEm = registro.registradaEm();
        entidade.status = registro.status();
        entidade.expiracaoSegundos = dados.expiracao();
        entidade.valorOriginal = dados.valorOriginal();
        entidade.chave = dados.chave().valor();
        entidade.devedor = DevedorColunas.de(dados.devedor());
        entidade.solicitacaoPagador = dados.solicitacaoPagador();
        entidade.camposAlterados = String.join(SEPARADOR, registro.camposAlterados());
        return entidade;
    }

    RegistroDeRevisao paraDominio() {
        var dados = new DadosCobranca(expiracaoSegundos, valorOriginal, ChavePix.de(chave),
                devedor == null ? null : devedor.paraDominio(), solicitacaoPagador);
        List<String> campos = camposAlterados.isEmpty() ? List.of() : Arrays.asList(camposAlterados.split(SEPARADOR));
        return new RegistroDeRevisao(new Txid(txid), revisao, registradaEm, status, dados, campos);
    }
}
