package io.github.henriteixeiranetto.cobrancas.pix.brcode;

import java.nio.charset.StandardCharsets;

/** CRC16-CCITT-FALSE (polinômio 0x1021, início 0xFFFF), exigido no campo 63 do BR Code. */
final class Crc16 {

    private static final int POLINOMIO = 0x1021;
    private static final int VALOR_INICIAL = 0xFFFF;

    private Crc16() {
    }

    /** @return o CRC em 4 dígitos hexadecimais maiúsculos, por exemplo "1D3D" */
    static String calcular(String conteudo) {
        int crc = VALOR_INICIAL;
        for (byte b : conteudo.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ POLINOMIO : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return "%04X".formatted(crc);
    }
}
