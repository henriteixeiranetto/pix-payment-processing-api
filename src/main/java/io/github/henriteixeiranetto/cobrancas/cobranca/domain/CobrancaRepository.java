package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.util.Optional;

/**
 * Porta de saída para persistência de cobranças (arquitetura hexagonal).
 *
 * <p>O domínio só conhece esta interface. A implementação com JPA/PostgreSQL fica no adaptador
 * {@code adapter.out.persistence}; nos testes de unidade, usamos uma implementação em memória.
 */
public interface CobrancaRepository {

    Optional<Cobranca> buscarPorTxid(Txid txid);

    Cobranca salvar(Cobranca cobranca);

    Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao);
}
