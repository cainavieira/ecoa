package ecoa.regras;

import static ecoa.regras.RegrasDoGanhador.validarEntregaDoPremio;
import static ecoa.regras.RegrasDoGanhador.validarGanhador;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class RegrasDoGanhadorTest {

  private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);

  @Test
  void aceitaGanhadorNoDiaDoInicio() {
    assertThatCode(() -> validarGanhador(StatusCriador.APROVADA, true, INICIO, INICIO))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaGanhadorNaVesperaDoInicio() {
    recusado(
        () -> validarGanhador(StatusCriador.APROVADA, true, INICIO, INICIO.minusDays(1)),
        Recusa.GANHADOR_ANTES_DO_INICIO);
  }

  @Test
  void recusaGanhadorQueNaoParticipa() {
    recusado(
        () -> validarGanhador(StatusCriador.APROVADA, false, INICIO, INICIO),
        Recusa.GANHADOR_NAO_PARTICIPANTE);
  }

  @Test
  void recusaGanhadorBloqueado() {
    recusado(
        () -> validarGanhador(StatusCriador.BLOQUEADA, true, INICIO, INICIO),
        Recusa.CRIADOR_BLOQUEADO);
  }

  @Test
  void aceitaEntregaHoje() {
    assertThatCode(() -> validarEntregaDoPremio(INICIO, INICIO)).doesNotThrowAnyException();
  }

  @Test
  void recusaEntregaAmanha() {
    recusado(() -> validarEntregaDoPremio(INICIO.plusDays(1), INICIO), Recusa.DATA_FUTURA);
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
