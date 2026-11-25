package io.github.henriteixeiranetto.cobrancas.cobranca.application;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.HistoricoDeRevisoes;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.RegistroDeRevisao;
import io.github.henriteixeiranetto.cobrancas.cobranca.domain.Txid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Histórico em memória para testar o serviço sem banco. */
class HistoricoDeRevisoesEmMemoria implements HistoricoDeRevisoes {

    private final List<RegistroDeRevisao> registros = new ArrayList<>();

    @Override
    public void registrar(RegistroDeRevisao registro) {
        registros.add(registro);
    }

    @Override
    public Optional<RegistroDeRevisao> buscar(Txid txid, int revisao) {
        return registros.stream()
                .filter(r -> r.txid().equals(txid) && r.revisao() == revisao)
                .findFirst();
    }

    @Override
    public List<RegistroDeRevisao> listar(Txid txid) {
        return registros.stream()
                .filter(r -> r.txid().equals(txid))
                .sorted(Comparator.comparingInt(RegistroDeRevisao::revisao))
                .toList();
    }
}
