package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.HistoricoDeRevisoes;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Implementação JPA do {@link HistoricoDeRevisoes}. */
@Repository
class HistoricoDeRevisoesJpaAdapter implements HistoricoDeRevisoes {

    private final CobrancaRevisaoJpaRepository jpa;

    HistoricoDeRevisoesJpaAdapter(CobrancaRevisaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(RegistroDeRevisao registro) {
        jpa.save(CobrancaRevisaoEntity.de(registro));
    }

    @Override
    public Optional<RegistroDeRevisao> buscar(Txid txid, int revisao) {
        return jpa.findByTxidAndRevisao(txid.valor(), revisao).map(CobrancaRevisaoEntity::paraDominio);
    }

    @Override
    public List<RegistroDeRevisao> listar(Txid txid) {
        return jpa.findByTxidOrderByRevisaoAsc(txid.valor()).stream()
                .map(CobrancaRevisaoEntity::paraDominio)
                .toList();
    }
}
