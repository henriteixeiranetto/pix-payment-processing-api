package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaRepository;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação em memória da porta {@link CobrancaRepository}. Graças à arquitetura hexagonal,
 * o serviço pode ser testado sem banco de dados, sem Spring e sem mocks.
 */
class CobrancaRepositoryEmMemoria implements CobrancaRepository {

    private final Map<Txid, Cobranca> cobrancas = new LinkedHashMap<>();

    @Override
    public Optional<Cobranca> buscarPorTxid(Txid txid) {
        return Optional.ofNullable(cobrancas.get(txid));
    }

    @Override
    public Cobranca salvar(Cobranca cobranca) {
        cobrancas.put(cobranca.txid(), cobranca);
        return cobranca;
    }

    @Override
    public Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao) {
        List<Cobranca> encontradas = cobrancas.values().stream()
                .filter(c -> !c.criacao().isBefore(filtro.inicio()) && !c.criacao().isAfter(filtro.fim()))
                .filter(c -> filtro.status() == null || c.status() == filtro.status())
                .filter(c -> filtro.cpf() == null || documentoDoDevedor(c).equals(filtro.cpf()))
                .filter(c -> filtro.cnpj() == null || documentoDoDevedor(c).equals(filtro.cnpj()))
                .sorted(Comparator.comparing(Cobranca::criacao).thenComparing(c -> c.txid().valor()))
                .toList();
        List<Cobranca> daPagina = encontradas.stream()
                .skip((long) paginacao.paginaAtual() * paginacao.itensPorPagina())
                .limit(paginacao.itensPorPagina())
                .toList();
        return new Pagina<>(daPagina, paginacao, encontradas.size());
    }

    private static String documentoDoDevedor(Cobranca cobranca) {
        var devedor = cobranca.dados().devedor();
        return devedor == null ? "" : devedor.documento().numero();
    }
}
