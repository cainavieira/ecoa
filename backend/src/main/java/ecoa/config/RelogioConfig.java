package ecoa.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O único ponto que diz que dia é hoje: o relógio do sistema, no horário de Brasília (ADR-0014).
 */
@Configuration
public class RelogioConfig {

  @Bean
  public Clock relogio() {
    return Clock.system(ZoneId.of("America/Sao_Paulo"));
  }
}
