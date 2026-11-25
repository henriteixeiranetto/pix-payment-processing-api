package io.github.henriteixeiranetto.cobrancas.pix;

import io.github.henriteixeiranetto.cobrancas.shared.domain.Cnpj;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cpf;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Chave Pix do recebedor. O tipo sai do formato, conforme o Manual de Padrões para
 * Iniciação do Pix ("Formatação das chaves do DICT no BR Code").
 */
public record ChavePix(TipoChavePix tipo, String valor) {

    public static final int TAMANHO_MAXIMO = 77;

    private static final Pattern EVP =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    /** Formato internacional E.164, por exemplo +5561988887777. */
    private static final Pattern TELEFONE = Pattern.compile("\\+[1-9]\\d{1,14}");
    /** No DICT, e-mails são registrados em letras minúsculas. */
    private static final Pattern EMAIL =
            Pattern.compile("[a-z0-9.!#$&'*+/=?^_`{|}~-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*");

    public ChavePix {
        if (tipo == null || identificarTipo(valor).filter(tipo::equals).isEmpty()) {
            throw chaveInvalida();
        }
    }

    public static ChavePix de(String valor) {
        return identificarTipo(valor)
                .map(tipo -> new ChavePix(tipo, valor))
                .orElseThrow(ChavePix::chaveInvalida);
    }

    private static Optional<TipoChavePix> identificarTipo(String valor) {
        if (valor == null || valor.isBlank() || valor.length() > TAMANHO_MAXIMO) {
            return Optional.empty();
        }
        if (EVP.matcher(valor).matches()) {
            return Optional.of(TipoChavePix.EVP);
        }
        if (TELEFONE.matcher(valor).matches()) {
            return Optional.of(TipoChavePix.TELEFONE);
        }
        if (EMAIL.matcher(valor).matches()) {
            return Optional.of(TipoChavePix.EMAIL);
        }
        if (Cpf.ehValido(valor)) {
            return Optional.of(TipoChavePix.CPF);
        }
        if (Cnpj.ehValido(valor)) {
            return Optional.of(TipoChavePix.CNPJ);
        }
        return Optional.empty();
    }

    private static RegraDeNegocioException chaveInvalida() {
        return new RegraDeNegocioException("chave",
                "A chave Pix deve ser um CPF, CNPJ, e-mail, telefone (+55...) ou chave aleatória (EVP) válido.");
    }
}
