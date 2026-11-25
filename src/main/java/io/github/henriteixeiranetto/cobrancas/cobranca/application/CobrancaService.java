package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Cobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaRepository;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.DadosCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.FiltroCobrancas;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.HistoricoDeRevisoes;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RevisaoCobranca;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import io.github.henriteixeiranetto.cobrancas.pix.brcode.BrCode;
import io.github.henriteixeiranetto.cobrancas.shared.config.PixProperties;
import io.github.henriteixeiranetto.cobrancas.shared.domain.ConsultaInvalidaException;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Pagina;
import io.github.henriteixeiranetto.cobrancas.shared.domain.Paginacao;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de cobrança. As regras ficam em {@link Cobranca}; aqui é só orquestração.
 * Cobrança e histórico são gravados na mesma transação.
 */
@Service
@Transactional
public class CobrancaService {

    private final CobrancaRepository repository;
    private final HistoricoDeRevisoes historico;
    private final PixProperties pix;
    private final Clock clock;

    public CobrancaService(CobrancaRepository repository, HistoricoDeRevisoes historico,
                           PixProperties pix, Clock clock) {
        this.repository = repository;
        this.historico = historico;
        this.pix = pix;
        this.clock = clock;
    }

    /** POST /cob: o sistema gera o txid. */
    public Cobranca criar(DadosCobranca dados) {
        return registrarCriacao(repository.salvar(novaCobranca(Txid.gerar(), dados)));
    }

    /**
     * PUT /cob/{txid}: cria, ou substitui os dados se a cobrança já existe.
     * Idempotente: repetir a requisição (ex.: depois de um timeout) não duplica a cobrança.
     */
    public ResultadoCriacao criarOuSubstituir(Txid txid, DadosCobranca dados) {
        Optional<Cobranca> existente = repository.buscarPorTxid(txid);
        if (existente.isEmpty()) {
            Cobranca criada = registrarCriacao(repository.salvar(novaCobranca(txid, dados)));
            return new ResultadoCriacao(criada, true);
        }
        Cobranca cobranca = existente.get();
        List<String> alterados = cobranca.substituirDados(dados);
        return new ResultadoCriacao(registrarAlteracao(repository.salvar(cobranca), alterados), false);
    }

    @Transactional(readOnly = true)
    public Cobranca consultar(Txid txid) {
        return repository.buscarPorTxid(txid).orElseThrow(() -> new CobrancaNaoEncontradaException(txid));
    }

    /** GET /cob/{txid}?revisao=N: a cobrança como ela era na revisão pedida. */
    @Transactional(readOnly = true)
    public Cobranca consultar(Txid txid, int revisao) {
        Cobranca atual = consultar(txid);
        if (revisao == atual.revisao()) {
            return atual;
        }
        return historico.buscar(txid, revisao)
                .map(atual::naRevisao)
                .orElseThrow(() -> new ConsultaInvalidaException("revisao",
                        "O parâmetro revisao corresponde a uma revisão inexistente para esta cobrança."));
    }

    /** GET /cob/{txid}/revisoes: o histórico completo, da criação até a revisão atual. */
    @Transactional(readOnly = true)
    public List<RegistroDeRevisao> listarRevisoes(Txid txid) {
        consultar(txid); // 404 se a cobrança não existe
        return historico.listar(txid);
    }

    /** PATCH /cob/{txid}. */
    public Cobranca revisar(Txid txid, RevisaoCobranca alteracoes) {
        Cobranca cobranca = consultar(txid);
        List<String> alterados = cobranca.revisar(alteracoes);
        return registrarAlteracao(repository.salvar(cobranca), alterados);
    }

    @Transactional(readOnly = true)
    public Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao) {
        return repository.listar(filtro, paginacao);
    }

    private Cobranca novaCobranca(Txid txid, DadosCobranca dados) {
        String location = pix.locationBase() + "/" + UUID.randomUUID().toString().replace("-", "");
        String pixCopiaECola = BrCode.dinamico(location, pix.recebedor().nome(), pix.recebedor().cidade());
        return Cobranca.criar(txid, dados, location, pixCopiaECola, agora());
    }

    /** Revisão 0, com o mesmo instante da criação. */
    private Cobranca registrarCriacao(Cobranca cobranca) {
        historico.registrar(RegistroDeRevisao.de(cobranca, List.of(), cobranca.criacao()));
        return cobranca;
    }

    /** Só registra revisão nova se algum campo mudou. */
    private Cobranca registrarAlteracao(Cobranca cobranca, List<String> alterados) {
        if (!alterados.isEmpty()) {
            historico.registrar(RegistroDeRevisao.de(cobranca, alterados, agora()));
        }
        return cobranca;
    }

    /** Em milissegundos, como nos exemplos da API Pix, para o banco guardar o mesmo valor da resposta. */
    private Instant agora() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    /** @param criada true se a cobrança foi criada agora; false se já existia */
    public record ResultadoCriacao(Cobranca cobranca, boolean criada) {
    }
}
