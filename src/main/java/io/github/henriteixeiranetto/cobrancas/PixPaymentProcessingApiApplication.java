package io.github.henriteixeiranetto.cobrancas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PixPaymentProcessingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixPaymentProcessingApiApplication.class, args);
    }
}
