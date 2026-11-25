package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaRepository;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Implementação JPA do {@link CobrancaRepository}. */
@Repository
class CobrancaPersistenceAdapter implements CobrancaRepository {

    private static final Sort ORDEM = Sort.by("criacao", "txid");

    private final CobrancaJpaRepository jpa;

    CobrancaPersistenceAdapter(CobrancaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Cobranca> buscarPorTxid(Txid txid) {
        return jpa.findById(txid.valor()).map(CobrancaEntity::paraDominio);
    }

    // Atualiza a entidade já carregada na transação, assim o @Version pega alteração concorrente.
    // saveAndFlush porque o histórico, gravado logo depois, tem FK para a cobrança.
    @Override
    public Cobranca salvar(Cobranca cobranca) {
        CobrancaEntity entidade = jpa.findById(cobranca.txid().valor()).orElseGet(CobrancaEntity::nova);
        entidade.copiarDe(cobranca);
        return jpa.saveAndFlush(entidade).paraDominio();
    }

    @Override
    public Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao) {
        var pageRequest = PageRequest.of(paginacao.paginaAtual(), paginacao.itensPorPagina(), ORDEM);
        Page<CobrancaEntity> pagina = jpa.findAll(CobrancaSpecifications.doFiltro(filtro), pageRequest);
        return new Pagina<>(pagina.map(CobrancaEntity::paraDominio).getContent(), paginacao, pagina.getTotalElements());
    }
}
