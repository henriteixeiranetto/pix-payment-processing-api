package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaRepository;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.brcode.BrCode;
import io.github.henriteixeiranetto.cobrancas.shared.config.PixProperties;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de cobrança imediata. Orquestra o domínio e a persistência; as regras
 * de negócio em si ficam na classe {@link Cobranca}.
 */
@Service
@Transactional
public class CobrancaService {

    private final CobrancaRepository repository;
    private final PixProperties pix;
    private final Clock clock;

    public CobrancaService(CobrancaRepository repository, PixProperties pix, Clock clock) {
        this.repository = repository;
        this.pix = pix;
        this.clock = clock;
    }

    /** POST /cob: o sistema gera o txid. */
    public Cobranca criar(DadosCobranca dados) {
        return repository.salvar(novaCobranca(Txid.gerar(), dados));
    }

    /**
     * PUT /cob/{txid}: o cliente escolhe o txid. Se a cobrança ainda não existe, é criada;
     * se existe e está ATIVA, seus dados são substituídos.
     *
     * <p>Isso torna a operação idempotente: se a resposta se perder (timeout) e o cliente
     * repetir a mesma requisição, nenhuma cobrança duplicada é criada.
     */
    public ResultadoCriacao criarOuSubstituir(Txid txid, DadosCobranca dados) {
        return repository.buscarPorTxid(txid)
                .map(existente -> {
                    existente.substituirDados(dados);
                    return new ResultadoCriacao(repository.salvar(existente), false);
                })
                .orElseGet(() -> new ResultadoCriacao(repository.salvar(novaCobranca(txid, dados)), true));
    }

    @Transactional(readOnly = true)
    public Cobranca consultar(Txid txid) {
        return repository.buscarPorTxid(txid).orElseThrow(() -> new CobrancaNaoEncontradaException(txid));
    }

    /** PATCH /cob/{txid}. */
    public Cobranca revisar(Txid txid, RevisaoCobranca alteracoes) {
        Cobranca cobranca = consultar(txid);
        cobranca.revisar(alteracoes);
        return repository.salvar(cobranca);
    }

    @Transactional(readOnly = true)
    public Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao) {
        return repository.listar(filtro, paginacao);
    }

    private Cobranca novaCobranca(Txid txid, DadosCobranca dados) {
        String location = pix.locationBase() + "/" + UUID.randomUUID().toString().replace("-", "");
        String pixCopiaECola = BrCode.dinamico(location, pix.recebedor().nome(), pix.recebedor().cidade());
        // Milissegundos, como nos exemplos da API Pix (e o banco guarda exatamente o que foi respondido)
        Instant criacao = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        return Cobranca.criar(txid, dados, location, pixCopiaECola, criacao);
    }

    /** @param criada true se a cobrança foi criada agora; false se já existia */
    public record ResultadoCriacao(Cobranca cobranca, boolean criada) {
    }
}
