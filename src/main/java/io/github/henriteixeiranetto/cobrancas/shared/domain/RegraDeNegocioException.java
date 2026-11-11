package io.github.henriteixeiranetto.cobrancas.shared.domain;

/**
 * Violação de uma regra de negócio ou de formato detectada pelo domínio.
 *
 * <p>A {@code propriedade} indica o campo que causou o erro (por exemplo, {@code valor.original}),
 * o que permite à camada web responder no formato de erros da API Pix, com a lista de violações.
 */
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
