package io.github.henriteixeiranetto.cobrancas.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configurações do Pix lidas do application.yml (prefixo {@code pix}).
 * São validadas na inicialização: com um valor inválido a aplicação nem sobe.
 *
 * @param recebedor    dados do recebedor exibidos no BR Code
 * @param locationBase endereço base do payload; o location final (base + "/" + 32 caracteres)
 *                     deve ter no máximo 77 caracteres, por isso a base tem no máximo 44
 */
@Validated
@ConfigurationProperties(prefix = "pix")
public record PixProperties(
        @Valid @NotNull Recebedor recebedor,
        @NotBlank @Size(max = 44) String locationBase) {

    /** Os limites de tamanho vêm do BR Code: nome com até 25 e cidade com até 15 caracteres. */
    public record Recebedor(
            @NotBlank @Size(max = 25) String nome,
            @NotBlank @Size(max = 15) String cidade) {
    }
}
