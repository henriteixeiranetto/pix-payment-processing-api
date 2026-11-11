package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CobrancaJpaRepository
        extends JpaRepository<CobrancaEntity, String>, JpaSpecificationExecutor<CobrancaEntity> {
}
