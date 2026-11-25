package io.github.henriteixeiranetto.cobrancas.cobranca.domain;

import java.util.List;
import java.util.Optional;

/** Porta do histórico de revisões (auditoria). */
public interface HistoricoDeRevisoes {

    void registrar(RegistroDeRevisao registro);

    Optional<RegistroDeRevisao> buscar(Txid txid, int revisao);

    /** Todas as revisões da cobrança, da mais antiga para a mais recente. */
    List<RegistroDeRevisao> listar(Txid txid);
}
