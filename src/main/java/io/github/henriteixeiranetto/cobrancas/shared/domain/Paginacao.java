package io.github.henriteixeiranetto.cobrancas.shared.domain;

/** Página solicitada em uma consulta. A primeira página é a 0, como na API Pix. */
public record Paginacao(int paginaAtual, int itensPorPagina) {

    public static final int ITENS_POR_PAGINA_PADRAO = 100;
    public static final int ITENS_POR_PAGINA_MAXIMO = 1000;

    public Paginacao {
        if (paginaAtual < 0) {
            throw new ConsultaInvalidaException("paginacao.paginaAtual",
                    "O parâmetro paginacao.paginaAtual não pode ser negativo.");
        }
        if (itensPorPagina < 1 || itensPorPagina > ITENS_POR_PAGINA_MAXIMO) {
            throw new ConsultaInvalidaException("paginacao.itensPorPagina",
                    "O parâmetro paginacao.itensPorPagina deve estar entre 1 e " + ITENS_POR_PAGINA_MAXIMO + ".");
        }
    }
}
