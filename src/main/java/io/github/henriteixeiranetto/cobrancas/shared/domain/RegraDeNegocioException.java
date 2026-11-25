package io.github.henriteixeiranetto.cobrancas.shared.domain;

/** Regra violada no domínio. A propriedade (ex.: "valor.original") vai para as violações da resposta. */
public class RegraDeNegocioException extends RuntimeException {

    private final String propriedade;

    public RegraDeNegocioException(String propriedade, String razao) {
        super(razao);
        this.propriedade = propriedade;
    }

    public String propriedade() {
        return propriedade;
    }

    public String razao() {
        return getMessage();
    }
}
