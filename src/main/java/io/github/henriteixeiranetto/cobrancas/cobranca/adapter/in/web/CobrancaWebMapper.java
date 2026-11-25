package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web;

import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CalendarioResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobGeradaResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobRevisadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobSolicitadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobsConsultadasResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.DevedorDto;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.HistoricoRevisoesResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.ValorDto;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Devedor;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.ChavePix;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cnpj;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Cpf;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import java.math.BigDecimal;
import java.util.List;

/** Converte entre o JSON da API (DTOs) e os objetos de domínio. */
final class CobrancaWebMapper {

    private CobrancaWebMapper() {
    }

    static DadosCobranca paraDados(CobSolicitadaRequest request) {
        Integer expiracao = request.calendario().expiracao();
        return new DadosCobranca(
                expiracao != null ? expiracao : DadosCobranca.EXPIRACAO_PADRAO,
                new BigDecimal(request.valor().original()),
                ChavePix.de(request.chave()),
                paraDevedor(request.devedor()),
                request.solicitacaoPagador());
    }

    static RevisaoCobranca paraRevisao(CobRevisadaRequest request) {
        return new RevisaoCobranca(
                request.calendario() != null ? request.calendario().expiracao() : null,
                request.valor() != null ? new BigDecimal(request.valor().original()) : null,
                request.chave() != null ? ChavePix.de(request.chave()) : null,
                paraDevedor(request.devedor()),
                request.solicitacaoPagador(),
                request.status() != null); // o DTO só aceita REMOVIDA_PELO_USUARIO_RECEBEDOR
    }

    static CobGeradaResponse paraResposta(Cobranca cobranca) {
        DadosCobranca dados = cobranca.dados();
        return new CobGeradaResponse(
                new CalendarioResponse(cobranca.criacao(), dados.expiracao()),
                cobranca.txid().valor(),
                cobranca.revisao(),
                cobranca.location(),
                cobranca.status(),
                paraDevedorDto(dados.devedor()),
                new ValorDto(dados.valorOriginal().toPlainString()),
                dados.chave().valor(),
                dados.solicitacaoPagador(),
                cobranca.pixCopiaECola());
    }

    static CobsConsultadasResponse paraResposta(FiltroCobrancas filtro, Pagina<Cobranca> pagina) {
        var paginacao = new CobsConsultadasResponse.PaginacaoResponse(
                pagina.paginacao().paginaAtual(),
                pagina.paginacao().itensPorPagina(),
                pagina.quantidadeDePaginas(),
                pagina.quantidadeTotalDeItens());
        var parametros = new CobsConsultadasResponse.Parametros(
                filtro.inicio(), filtro.fim(), filtro.cpf(), filtro.cnpj(), filtro.status(), paginacao);
        return new CobsConsultadasResponse(parametros, pagina.map(CobrancaWebMapper::paraResposta).itens());
    }

    static HistoricoRevisoesResponse paraHistorico(Txid txid, List<RegistroDeRevisao> registros) {
        var revisoes = registros.stream()
                .map(r -> new HistoricoRevisoesResponse.Revisao(r.revisao(), r.registradaEm(), r.status(), r.camposAlterados()))
                .toList();
        return new HistoricoRevisoesResponse(txid.valor(), revisoes);
    }

    private static Devedor paraDevedor(DevedorDto dto) {
        return dto == null ? null : Devedor.de(dto.cpf(), dto.cnpj(), dto.nome());
    }

    private static DevedorDto paraDevedorDto(Devedor devedor) {
        if (devedor == null) {
            return null;
        }
        return switch (devedor.documento()) {
            case Cpf cpf -> new DevedorDto(cpf.numero(), null, devedor.nome());
            case Cnpj cnpj -> new DevedorDto(null, cnpj.numero(), devedor.nome());
        };
    }
}
