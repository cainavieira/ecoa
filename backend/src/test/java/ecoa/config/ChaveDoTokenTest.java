package ecoa.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ChaveDoTokenTest {

  @Test
  void chaveQueNaoEhRsaDerrubaComONomeDaVariavel() {
    assertThatThrownBy(() -> ChaveDoToken.publica("isso não é uma chave"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ECOA_JWT_CHAVE");
  }
}
