package ecoa.regras;

import static ecoa.regras.RegrasDaCampanha.statusDaCampanha;
import static ecoa.regras.RegrasDaCampanha.validarDatasDaCampanha;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class RegrasDaCampanhaTest {

  private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
  private static final LocalDate FIM = LocalDate.of(2026, 11, 30);
  private static final LocalDate FECHAMENTO = LocalDate.of(2026, 12, 5);
  private static final DatasDaCampanha DATAS = new DatasDaCampanha(INICIO, FIM, FECHAMENTO);
  private static final LocalDate ANTES_DA_CAMPANHA = LocalDate.of(2026, 10, 20);

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

  @Test
  void recusaFimAntesDoInicio() {
    recusado(
        () ->
            validarDatasDaCampanha(
                new DatasDaCampanha(INICIO, INICIO.minusDays(1), FECHAMENTO), ANTES_DA_CAMPANHA),
        Recusa.DATAS_FORA_DE_ORDEM);
  }

  @Test
  void aceitaTresDatasIguais() {
    // Campanha de um dia, sem estabilização (RN-24, UC-05 2c).
    assertThatCode(
            () ->
                validarDatasDaCampanha(
                    new DatasDaCampanha(INICIO, INICIO, INICIO), ANTES_DA_CAMPANHA))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaFechamentoHoje() {
    recusado(() -> validarDatasDaCampanha(DATAS, FECHAMENTO), Recusa.FECHAMENTO_NAO_FUTURO);
  }

  @Test
  void aceitaFechamentoAmanha() {
    assertThatCode(() -> validarDatasDaCampanha(DATAS, FECHAMENTO.minusDays(1)))
        .doesNotThrowAnyException();
  }

  @Test
  void aceitaMudancaQueMantemOFechamentoPassado() {
    // Editar o regulamento de campanha em apuração reenvia as mesmas datas.
    assertThatCode(() -> validarDatasDaCampanha(DATAS, DATAS, List.of(), FECHAMENTO.plusDays(3)))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaMudarOFechamentoParaHoje() {
    LocalDate hoje = FIM.plusDays(2);
    recusado(
        () ->
            validarDatasDaCampanha(DATAS, new DatasDaCampanha(INICIO, FIM, hoje), List.of(), hoje),
        Recusa.FECHAMENTO_NAO_FUTURO);
  }

  @Test
  void recusaMudarOFimParaDepoisDoFechamento() {
    recusado(
        () ->
            validarDatasDaCampanha(
                DATAS,
                new DatasDaCampanha(INICIO, FECHAMENTO.plusDays(1), FECHAMENTO),
                List.of(),
                ANTES_DA_CAMPANHA),
        Recusa.DATAS_FORA_DE_ORDEM);
  }

  @Test
  void recusaFimNaVesperaDaPublicacaoEApontaOVideo() {
    List<VideoAprovado> aprovados =
        List.of(
            new VideoAprovado(1, LocalDate.of(2026, 11, 10)),
            new VideoAprovado(2, LocalDate.of(2026, 11, 20)));
    DatasDaCampanha novas = new DatasDaCampanha(INICIO, LocalDate.of(2026, 11, 19), FECHAMENTO);

    assertThatThrownBy(() -> validarDatasDaCampanha(DATAS, novas, aprovados, ANTES_DA_CAMPANHA))
        .isInstanceOfSatisfying(
            RegraVioladaException.class,
            e -> {
              assertThat(e.recusa()).isEqualTo(Recusa.DATAS_EXCLUEM_VIDEO);
              assertThat(e.itens()).containsExactly(2);
            });
  }

  @Test
  void aceitaFimNoDiaDaPublicacao() {
    LocalDate publicacao = LocalDate.of(2026, 11, 20);
    assertThatCode(
            () ->
                validarDatasDaCampanha(
                    DATAS,
                    new DatasDaCampanha(INICIO, publicacao, FECHAMENTO),
                    List.of(new VideoAprovado(2, publicacao)),
                    ANTES_DA_CAMPANHA))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaInicioDepoisDaPublicacao() {
    LocalDate publicacao = LocalDate.of(2026, 11, 10);
    recusado(
        () ->
            validarDatasDaCampanha(
                DATAS,
                new DatasDaCampanha(publicacao.plusDays(1), FIM, FECHAMENTO),
                List.of(new VideoAprovado(1, publicacao)),
                ANTES_DA_CAMPANHA),
        Recusa.DATAS_EXCLUEM_VIDEO);
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
