package ecoa.regras;

import static ecoa.regras.RegrasDaCampanha.statusDaCampanha;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RegrasDaCampanhaTest {

  private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
  private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

  @Test
  void vesperaDoInicioEstaAgendada() {
    assertThat(statusDaCampanha(INICIO, FIM, false, INICIO.minusDays(1)))
        .isEqualTo(StatusCampanha.AGENDADA);
  }

  @Test
  void diaDoInicioEstaAberta() {
    assertThat(statusDaCampanha(INICIO, FIM, false, INICIO)).isEqualTo(StatusCampanha.ABERTA);
  }

  @Test
  void diaDoFimEstaAberta() {
    assertThat(statusDaCampanha(INICIO, FIM, false, FIM)).isEqualTo(StatusCampanha.ABERTA);
  }

  @Test
  void diaSeguinteAoFimEstaEmApuracao() {
    assertThat(statusDaCampanha(INICIO, FIM, false, FIM.plusDays(1)))
        .isEqualTo(StatusCampanha.EM_APURACAO);
  }

  @Test
  void encerramentoVenceAsDatas() {
    // Com o fechamento igual ao fim (RN-24), dá para encerrar no próprio dia do fim.
    assertThat(statusDaCampanha(INICIO, FIM, true, FIM)).isEqualTo(StatusCampanha.ENCERRADA);
  }
}
