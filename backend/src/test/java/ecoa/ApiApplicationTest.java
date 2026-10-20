package ecoa;

import static org.assertj.core.api.Assertions.assertThat;

import ecoa.config.VariaveisObrigatorias;
import org.junit.jupiter.api.Test;

class ApiApplicationTest {

  @Test
  void aSubidaConfereAsVariaveisObrigatorias() {
    assertThat(ApiApplication.aplicacao().getListeners())
        .hasAtLeastOneElementOfType(VariaveisObrigatorias.class);
  }
}
