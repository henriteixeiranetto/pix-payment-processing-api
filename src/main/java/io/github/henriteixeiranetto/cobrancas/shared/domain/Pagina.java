package io.github.henriteixeiranetto.cobrancas.shared.domain;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado de uma consulta, independente de framework de persistência. */
public record Pagina<T>(List<T> itens, Paginacao paginacao, long quantidadeTotalDeItens) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public int quantidadeDePaginas() {
        long porPagina = paginacao.itensPorPagina();
        return (int) ((quantidadeTotalDeItens + porPagina - 1) / porPagina);
    }

    public <R> Pagina<R> map(Function<? super T, ? extends R> conversor) {
        List<R> convertidos = itens.stream().<R>map(conversor).toList();
        return new Pagina<>(convertidos, paginacao, quantidadeTotalDeItens);
    }
}
