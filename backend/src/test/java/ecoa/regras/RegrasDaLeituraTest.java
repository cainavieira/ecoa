package ecoa.regras;

import static ecoa.regras.RegrasDaLeitura.leituraDeFechamento;
import static ecoa.regras.RegrasDaLeitura.numeroAtual;
import static ecoa.regras.RegrasDaLeitura.validarLeitura;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegrasDaLeituraTest {

  private static final LocalDate PUBLICACAO = LocalDate.of(2026, 11, 10);
  private static final LocalDate FECHAMENTO = LocalDate.of(2026, 11, 30);
  private static final LocalDate HOJE = LocalDate.of(2026, 12, 5);
  private static final LeituraDoVideo PARCIAL = new LeituraDoVideo(LocalDate.of(2026, 11, 25), 700);
  private static final LeituraDoVideo DE_FECHAMENTO =
      new LeituraDoVideo(LocalDate.of(2026, 12, 2), 900);

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

  @Test
  void leituraNoDiaDoFechamentoContaComoFechamento() {
    LeituraDoVideo noFechamento = new LeituraDoVideo(FECHAMENTO, 900);
    List<LeituraDoVideo> leituras =
        List.of(new LeituraDoVideo(FECHAMENTO.minusDays(1), 800), noFechamento);
    assertThat(leituraDeFechamento(leituras, FECHAMENTO)).contains(noFechamento);
  }

  @Test
  void leituraNaVesperaDoFechamentoFicaParcial() {
    List<LeituraDoVideo> leituras = List.of(new LeituraDoVideo(FECHAMENTO.minusDays(1), 800));
    assertThat(leituraDeFechamento(leituras, FECHAMENTO)).isEmpty();
  }

  @Test
  void aceitaLeituraDeHoje() {
    assertThatCode(() -> ler(HOJE, List.of())).doesNotThrowAnyException();
  }

  @Test
  void recusaLeituraDeAmanha() {
    recusado(() -> ler(HOJE.plusDays(1), List.of()), Recusa.DATA_FUTURA);
  }

  @Test
  void aceitaLeituraNaDataDaPublicacao() {
    assertThatCode(() -> ler(PUBLICACAO, List.of())).doesNotThrowAnyException();
  }

  @Test
  void recusaLeituraNaVesperaDaPublicacao() {
    recusado(() -> ler(PUBLICACAO.minusDays(1), List.of()), Recusa.LEITURA_ANTES_DA_PUBLICACAO);
  }

  @Test
  void aceitaPrimeiraLeituraAPartirDoFechamento() {
    assertThatCode(() -> ler(DE_FECHAMENTO.data(), List.of(PARCIAL))).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"2026-11-30", "2026-12-01", "2026-12-03"})
  void recusaOutraDataAPartirDoFechamento(LocalDate data) {
    recusado(() -> ler(data, List.of(PARCIAL, DE_FECHAMENTO)), Recusa.LEITURA_DEPOIS_DO_FECHAMENTO);
  }

  @Test
  void aceitaLeituraNaDataDaLeituraDeFechamento() {
    // Corrigir o número é gravar de novo na mesma data (UC-11 2f).
    assertThatCode(() -> ler(DE_FECHAMENTO.data(), List.of(PARCIAL, DE_FECHAMENTO)))
        .doesNotThrowAnyException();
  }

  @Test
  void aceitaParcialDepoisDeExistirLeituraDeFechamento() {
    assertThatCode(() -> ler(FECHAMENTO.minusDays(1), List.of(DE_FECHAMENTO)))
        .doesNotThrowAnyException();
  }

  private static void ler(LocalDate data, List<LeituraDoVideo> leituras) {
    validarLeitura(PUBLICACAO, FECHAMENTO, leituras, data, HOJE);
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
