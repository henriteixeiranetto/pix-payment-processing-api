package io.github.henriteixeiranetto.cobrancas.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio da aplicação como um bean: o código pede a hora ao {@link Clock} em vez de
 * chamar {@code Instant.now()} diretamente, e os testes podem usar um relógio fixo.
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
