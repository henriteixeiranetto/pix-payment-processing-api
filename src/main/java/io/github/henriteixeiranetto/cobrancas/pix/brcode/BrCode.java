package io.github.henriteixeiranetto.cobrancas.pix.brcode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;

/**
 * Gera o "Pix Copia e Cola" (BR Code): o texto que vai dentro do QR Code do Pix.
 *
 * <p>O BR Code segue o padrão EMV QRCPS-MPM. Ele é uma sequência de campos no formato TLV:
 * ID com 2 dígitos + tamanho com 2 dígitos + valor. Exemplo: {@code 5802BR} é o campo 58
 * (país), com 2 caracteres, valendo "BR". O último campo (63) é um CRC16 de todo o texto.
 *
 * <p>Referência: Manual de Padrões para Iniciação do Pix, do Banco Central do Brasil.
 */
public final class BrCode {

    static final int TAMANHO_MAXIMO_NOME = 25;
    static final int TAMANHO_MAXIMO_CIDADE = 15;
    private static final String GUI_PIX = "br.gov.bcb.pix";
    private static final String SEM_TXID = "***";
    private static final String QR_DE_USO_UNICO = "12";

    private BrCode() {
    }

    /**
     * BR Code de QR Code dinâmico, usado nas cobranças imediatas: em vez de levar os dados
     * da cobrança, leva a URL (location) onde o aplicativo do pagador busca esses dados.
     */
    public static String dinamico(String location, String nomeRecebedor, String cidadeRecebedor) {
        String contaPix = campo("00", GUI_PIX) + campo("25", location);
        return montar(QR_DE_USO_UNICO, contaPix, null, nomeRecebedor, cidadeRecebedor, SEM_TXID);
    }

    /**
     * BR Code de QR Code estático: leva a própria chave Pix e, opcionalmente, valor e txid.
     *
     * @param valor opcional; quando nulo, o pagador digita o valor no aplicativo
     * @param txid  opcional (até 25 caracteres); quando nulo, usa "***"
     */
    public static String estatico(String chave, BigDecimal valor, String txid,
                                  String nomeRecebedor, String cidadeRecebedor) {
        String contaPix = campo("00", GUI_PIX) + campo("01", chave);
        return montar(null, contaPix, valor, nomeRecebedor, cidadeRecebedor, txid == null ? SEM_TXID : txid);
    }

    private static String montar(String metodoDeIniciacao, String contaPix, BigDecimal valor,
                                 String nome, String cidade, String txid) {
        StringBuilder payload = new StringBuilder(campo("00", "01")); // versão do payload
        if (metodoDeIniciacao != null) {
            payload.append(campo("01", metodoDeIniciacao));
        }
        payload.append(campo("26", contaPix))  // dados da conta Pix
                .append(campo("52", "0000"))   // categoria do estabelecimento (não informada)
                .append(campo("53", "986"));   // moeda: real (ISO 4217)
        if (valor != null) {
            payload.append(campo("54", valor.setScale(2, RoundingMode.UNNECESSARY).toPlainString()));
        }
        payload.append(campo("58", "BR"))
                .append(campo("59", normalizar(nome, TAMANHO_MAXIMO_NOME)))
                .append(campo("60", normalizar(cidade, TAMANHO_MAXIMO_CIDADE)))
                .append(campo("62", campo("05", txid)))
                .append("6304"); // o CRC é calculado incluindo o ID e o tamanho do próprio campo 63
        return payload.append(Crc16.calcular(payload.toString())).toString();
    }

    static String campo(String id, String valor) {
        if (valor.length() > 99) {
            throw new IllegalArgumentException("O campo " + id + " do BR Code excede 99 caracteres.");
        }
        return id + "%02d".formatted(valor.length()) + valor;
    }

    /** Remove acentos e símbolos (o BR Code aceita apenas caracteres simples) e corta no tamanho máximo. */
    static String normalizar(String texto, int tamanhoMaximo) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String simples = semAcentos.replaceAll("[^A-Za-z0-9 .\\-]", "").replaceAll("\\s+", " ").trim();
        return simples.length() <= tamanhoMaximo ? simples : simples.substring(0, tamanhoMaximo).trim();
    }
}
