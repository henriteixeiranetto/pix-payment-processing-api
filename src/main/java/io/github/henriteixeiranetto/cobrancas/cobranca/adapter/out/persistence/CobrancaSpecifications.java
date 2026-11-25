package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Filtros do GET /cob: cada filtro opcional só entra no WHERE se foi informado. */
final class CobrancaSpecifications {

    private CobrancaSpecifications() {
    }

    static Specification<CobrancaEntity> doFiltro(FiltroCobrancas filtro) {
        List<Specification<CobrancaEntity>> condicoes = new ArrayList<>();
        condicoes.add((root, _, cb) -> cb.between(root.<Instant>get("criacao"), filtro.inicio(), filtro.fim()));
        if (filtro.cpf() != null) {
            condicoes.add((root, _, cb) -> cb.equal(root.get("devedor").get("cpf"), filtro.cpf()));
        }
        if (filtro.cnpj() != null) {
            condicoes.add((root, _, cb) -> cb.equal(root.get("devedor").get("cnpj"), filtro.cnpj()));
        }
        if (filtro.status() != null) {
            condicoes.add((root, _, cb) -> cb.equal(root.get("status"), filtro.status()));
        }
        return Specification.allOf(condicoes);
    }
}
