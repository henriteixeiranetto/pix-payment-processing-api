package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.util.Optional;

/** Porta de persistência das cobranças. A implementação JPA fica em adapter.out.persistence. */
public interface CobrancaRepository {

    Optional<Cobranca> buscarPorTxid(Txid txid);

    Cobranca salvar(Cobranca cobranca);

    Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao);
}
