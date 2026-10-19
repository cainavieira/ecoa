package ecoa.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RelogioConfigTest {

  @Test
  void oDiaViraNaMeiaNoiteDeBrasilia() {
    // 01/12 à 1h em UTC ainda é 30/11, às 22h, em Brasília.
    Instant umaDaManhaEmUtc = Instant.parse("2026-12-01T01:00:00Z");
    Clock relogio = new RelogioConfig().relogio();
    assertThat(LocalDate.ofInstant(umaDaManhaEmUtc, relogio.getZone()))
        .isEqualTo(LocalDate.of(2026, 11, 30));
  }
}
