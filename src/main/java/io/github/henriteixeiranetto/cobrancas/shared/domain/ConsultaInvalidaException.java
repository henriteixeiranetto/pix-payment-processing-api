package io.github.henriteixeiranetto.cobrancas.shared.domain;

/** Parâmetros de consulta (filtros, paginação) inválidos ou incoerentes entre si. */
public class ConsultaInvalidaException extends RegraDeNegocioException {

    public ConsultaInvalidaException(String propriedade, String razao) {
        super(propriedade, razao);
    }
}
