package io.github.henriteixeiranetto.cobrancas;

import org.springframework.boot.SpringApplication;

/**
 * Roda a aplicação localmente com um PostgreSQL criado pelo Testcontainers (precisa do Docker).
 * Útil para desenvolver sem configurar banco: execute este main pela IDE.
 */
public class TestPixPaymentProcessingApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(PixPaymentProcessingApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
