package ecoa.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

@JsonTest
class ConversaoDoJsonTest {

  private final JsonMapper conversor;

  @Autowired
  ConversaoDoJsonTest(JsonMapper conversor) {
    this.conversor = conversor;
  }

  record ComViews(Long views) {}

  @Test
  void recusaParteDecimalEmCampoInteiro() {
    // Sem a configuração, o Jackson corta: 12.5 vira 12, e -0.7 vira 0 (UC-10 3b).
    assertThatThrownBy(() -> conversor.readValue("{\"views\": 12.5}", ComViews.class))
        .isInstanceOf(MismatchedInputException.class);
  }
}
