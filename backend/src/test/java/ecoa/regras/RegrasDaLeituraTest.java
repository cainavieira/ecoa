package ecoa.regras;

import static ecoa.regras.RegrasDaLeitura.numeroAtual;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasDaLeituraTest {

  @Test
  void numeroAtualVemDaLeituraDeDataMaisRecente() {
    // A mais recente fica no meio da lista e tem menos views que outra, porque as redes removem
    // views falsas (RN-39).
    LeituraDoVideo recente = new LeituraDoVideo(LocalDate.of(2026, 11, 20), 900);
    List<LeituraDoVideo> leituras =
        List.of(
            new LeituraDoVideo(LocalDate.of(2026, 11, 10), 500),
            recente,
            new LeituraDoVideo(LocalDate.of(2026, 11, 15), 1_000));
    assertThat(numeroAtual(leituras)).contains(recente);
  }
}
