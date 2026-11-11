package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import io.github.henriteixeiranetto.cobrancas.shared.domain.Cnpj;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cpf;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Documento;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.util.Objects;

/**
 * Pessoa ou empresa a quem a cobrança é endereçada. Não é necessariamente quem vai pagar:
 * outra pessoa pode quitar a cobrança.
 */
public record Devedor(Documento documento, String nome) {

    public static final int TAMANHO_MAXIMO_NOME = 200;

    public Devedor {
        Objects.requireNonNull(documento, "documento");
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("devedor.nome", "O nome do devedor é obrigatório.");
        }
        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            throw new RegraDeNegocioException("devedor.nome",
                    "O nome do devedor deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
    }

    /** Cria o devedor a partir dos campos da API: exatamente um entre CPF e CNPJ, mais o nome. */
    public static Devedor de(String cpf, String cnpj, String nome) {
        if (cpf != null && cnpj != null) {
            throw new RegraDeNegocioException("devedor", "Não é permitido informar CPF e CNPJ ao mesmo tempo.");
        }
        if (cpf == null && cnpj == null) {
            throw new RegraDeNegocioException("devedor", "Informe o CPF ou o CNPJ do devedor.");
        }
        if (cpf != null && !Cpf.ehValido(cpf)) {
            throw new RegraDeNegocioException("devedor.cpf", "CPF inválido.");
        }
        if (cnpj != null && !Cnpj.ehValido(cnpj)) {
            throw new RegraDeNegocioException("devedor.cnpj", "CNPJ inválido.");
        }
        Documento documento = cpf != null ? new Cpf(cpf) : new Cnpj(cnpj);
        return new Devedor(documento, nome);
    }
}
