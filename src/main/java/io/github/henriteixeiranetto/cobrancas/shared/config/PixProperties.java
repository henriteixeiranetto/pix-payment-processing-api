package io.github.henriteixeiranetto.cobrancas.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriedades pix.* do application.yml, validadas na subida.
 *
 * @param locationBase até 44 caracteres: com "/" e o id de 32, o location fica no limite de 77 da API
 */
@Validated
@ConfigurationProperties(prefix = "pix")
public record PixProperties(
        @Valid @NotNull Recebedor recebedor,
        @NotBlank @Size(max = 44) String locationBase) {

    /** Limites do BR Code: nome até 25, cidade até 15. */
    public record Recebedor(
            @NotBlank @Size(max = 25) String nome,
            @NotBlank @Size(max = 15) String cidade) {
    }
}
