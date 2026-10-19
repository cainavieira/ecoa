package ecoa.regras;

import static ecoa.regras.RegrasDaConta.normalizarEmail;
import static ecoa.regras.RegrasDaConta.normalizarUsuarioTiktok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RegrasDaContaTest {

  @Test
  void emailFicaEmMinusculas() {
    assertThat(normalizarEmail("Fulano.Silva@Gmail.COM")).isEqualTo("fulano.silva@gmail.com");
  }

  @ParameterizedTest
  @CsvSource({"@Fulano_Silva, fulano_silva", "@@fulano, fulano", "'  @fulano ', fulano"})
  void usuarioTiktokPerdeOArrobaEAsMaiusculas(String digitado, String guardado) {
    assertThat(normalizarUsuarioTiktok(digitado)).isEqualTo(guardado);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "@", "   ", "fulano silva", "@ fulano", "fulano\tsilva"})
  void recusaUsuarioTiktokVazioOuComEspacoNoMeio(String digitado) {
    recusado(() -> normalizarUsuarioTiktok(digitado), Recusa.USUARIO_TIKTOK_INVALIDO);
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
