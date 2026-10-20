package ecoa.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class VariaveisObrigatoriasTest {

  @Test
  void aMensagemDizQuaisVariaveisFaltam() {
    MockEnvironment ambiente =
        new MockEnvironment().withProperty("ECOA_DB_URL", "jdbc:postgresql://localhost/ecoa");
    assertThatThrownBy(() -> VariaveisObrigatorias.conferir(ambiente))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ECOA_DB_USUARIO, ECOA_DB_SENHA")
        .hasMessageNotContaining("ECOA_DB_URL");
  }
}
