package io.github.henriteixeiranetto.cobrancas.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados da documentação OpenAPI, exibida em /swagger-ui.html. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Plataforma de Cobrança Pix")
                .version("0.1.0")
                .description("""
                        API de cobranças imediatas (cob) inspirada na API Pix do Banco Central.
                        Ambiente simulado: nenhuma cobrança gerada aqui movimenta dinheiro de verdade."""));
    }
}
