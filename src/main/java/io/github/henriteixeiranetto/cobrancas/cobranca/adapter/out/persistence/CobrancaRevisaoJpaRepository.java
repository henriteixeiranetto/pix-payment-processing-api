package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobrancaRevisaoJpaRepository extends JpaRepository<CobrancaRevisaoEntity, Long> {

    Optional<CobrancaRevisaoEntity> findByTxidAndRevisao(String txid, int revisao);

    List<CobrancaRevisaoEntity> findByTxidOrderByRevisaoAsc(String txid);
}
