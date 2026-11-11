package io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web;

import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobGeradaResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobRevisadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobSolicitadaRequest;
import io.github.henriteixeiranetto.cobrancas.cobranca.adapter.in.web.dto.CobsConsultadasResponse;
import io.github.henriteixeiranetto.cobrancas.cobranca.application.CobrancaService;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.StatusCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de cobrança imediata, com os mesmos caminhos e formatos da API Pix (/cob).
 * A documentação OpenAPI de cada endpoint está em {@link CobrancaApi}.
 */
@RestController
@RequestMapping("/cob")
public class CobrancaController implements CobrancaApi {

    private final CobrancaService service;

    public CobrancaController(CobrancaService service) {
        this.service = service;
    }

    @Override
    @PutMapping("/{txid}")
    public ResponseEntity<CobGeradaResponse> criarComTxid(@PathVariable String txid,
                                                          @Valid @RequestBody CobSolicitadaRequest request) {
        var resultado = service.criarOuSubstituir(new Txid(txid), CobrancaWebMapper.paraDados(request));
        CobGeradaResponse resposta = CobrancaWebMapper.paraResposta(resultado.cobranca());
        return resultado.criada()
                ? ResponseEntity.created(URI.create("/cob/" + txid)).body(resposta)
                : ResponseEntity.ok(resposta);
    }

    @Override
    @PostMapping
    public ResponseEntity<CobGeradaResponse> criar(@Valid @RequestBody CobSolicitadaRequest request) {
        Cobranca cobranca = service.criar(CobrancaWebMapper.paraDados(request));
        return ResponseEntity.created(URI.create("/cob/" + cobranca.txid()))
                .body(CobrancaWebMapper.paraResposta(cobranca));
    }

    @Override
    @GetMapping("/{txid}")
    public CobGeradaResponse consultar(@PathVariable String txid) {
        return CobrancaWebMapper.paraResposta(service.consultar(new Txid(txid)));
    }

    @Override
    @PatchMapping("/{txid}")
    public CobGeradaResponse revisar(@PathVariable String txid, @Valid @RequestBody CobRevisadaRequest request) {
        return CobrancaWebMapper.paraResposta(service.revisar(new Txid(txid), CobrancaWebMapper.paraRevisao(request)));
    }

    @Override
    @GetMapping
    public CobsConsultadasResponse listar(
            @RequestParam Instant inicio,
            @RequestParam Instant fim,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) StatusCobranca status,
            @RequestParam(name = "paginacao.paginaAtual", defaultValue = "0") int paginaAtual,
            @RequestParam(name = "paginacao.itensPorPagina", defaultValue = "100") int itensPorPagina) {
        var filtro = new FiltroCobrancas(inicio, fim, cpf, cnpj, status);
        var pagina = service.listar(filtro, new Paginacao(paginaAtual, itensPorPagina));
        return CobrancaWebMapper.paraResposta(filtro, pagina);
    }
}
