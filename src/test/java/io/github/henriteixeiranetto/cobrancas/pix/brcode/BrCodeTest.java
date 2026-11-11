package io.github.henriteixeiranetto.cobrancas.pix.brcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BrCodeTest {

    @Test
    void crc16ConfereComOVetorDeTestePadrao() {
        // "123456789" é o valor de referência usado para validar implementações de CRC
        assertThat(Crc16.calcular("123456789")).isEqualTo("29B1");
    }

    @Test
    void reproduzOExemploDoManualDoBancoCentral() {
        String exemploDoManual = "00020126580014br.gov.bcb.pix0136123e4567-e12b-12d1-a456-426655440000"
                + "5204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***63041D3D";

        String gerado = BrCode.estatico("123e4567-e12b-12d1-a456-426655440000", null, null, "Fulano de Tal", "BRASILIA");

        assertThat(gerado).isEqualTo(exemploDoManual);
    }

    @Test
    void geraBrCodeDinamicoApontandoParaOLocation() {
        String location = "pix.example.com/qr/v2/2353c790eefb11eaadc10242ac120002";

        String brCode = BrCode.dinamico(location, "Loja Exemplo LTDA", "Sao Paulo");

        assertThat(brCode)
                .startsWith("000201" + "010212") // versão do payload + QR de uso único
                .contains("26760014br.gov.bcb.pix2554" + location)
                .contains("5303986")
                .contains("5802BR")
                .contains("5917Loja Exemplo LTDA")
                .contains("6009Sao Paulo")
                .contains("62070503***")
                .doesNotContain("5406"); // QR dinâmico não leva o valor: o app busca no location
        String semCrc = brCode.substring(0, brCode.length() - 4);
        assertThat(semCrc).endsWith("6304");
        assertThat(brCode).endsWith(Crc16.calcular(semCrc));
    }

    @Test
    void incluiValorETxidNoBrCodeEstatico() {
        String brCode = BrCode.estatico("fulano@example.com", new BigDecimal("10.5"), "PEDIDO123", "Fulano", "Recife");

        assertThat(brCode)
                .contains("540510.50")
                .contains("62130509PEDIDO123");
    }

    @Test
    void removeAcentosESimbolosECortaTextosLongos() {
        assertThat(BrCode.normalizar("Padaria São João & Cia", BrCode.TAMANHO_MAXIMO_NOME))
                .isEqualTo("Padaria Sao Joao Cia");
        assertThat(BrCode.normalizar("Comércio de Alimentos Brasileiros", BrCode.TAMANHO_MAXIMO_NOME))
                .isEqualTo("Comercio de Alimentos Bra");
        assertThat(BrCode.normalizar("São José dos Campos", BrCode.TAMANHO_MAXIMO_CIDADE))
                .isEqualTo("Sao Jose dos Ca");
    }

    @Test
    void campoNaoPodePassarDe99Caracteres() {
        assertThatThrownBy(() -> BrCode.campo("25", "x".repeat(100)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
