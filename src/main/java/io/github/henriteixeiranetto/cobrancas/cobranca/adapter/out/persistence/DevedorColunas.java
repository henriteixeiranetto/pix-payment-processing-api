package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.out.persistence;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Devedor;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cnpj;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cpf;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Documento;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Colunas do devedor, usadas em cobranca e em cobranca_revisao. Tudo nulo = sem devedor. */
@Embeddable
public record DevedorColunas(
        @Column(name = "devedor_cpf") String cpf,
        @Column(name = "devedor_cnpj") String cnpj,
        @Column(name = "devedor_nome") String nome) {

    static DevedorColunas de(Devedor devedor) {
        if (devedor == null) {
            return null;
        }
        return switch (devedor.documento()) {
            case Cpf cpf -> new DevedorColunas(cpf.numero(), null, devedor.nome());
            case Cnpj cnpj -> new DevedorColunas(null, cnpj.numero(), devedor.nome());
        };
    }

    Devedor paraDominio() {
        Documento documento = cpf != null ? new Cpf(cpf) : new Cnpj(cnpj);
        return new Devedor(documento, nome);
    }
}
