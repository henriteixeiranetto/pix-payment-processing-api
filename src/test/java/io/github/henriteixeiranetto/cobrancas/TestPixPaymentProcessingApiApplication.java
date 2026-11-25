package io.github.henriteixeiranetto.cobrancas;

import org.springframework.boot.SpringApplication;

/** Sobe a aplicação com um PostgreSQL do Testcontainers (precisa de Docker). Para rodar pela IDE. */
public class TestPixPaymentProcessingApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(PixPaymentProcessingApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
