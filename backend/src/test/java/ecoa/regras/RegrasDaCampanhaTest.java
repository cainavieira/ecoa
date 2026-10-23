package ecoa.regras;

import static ecoa.regras.RegrasDaCampanha.statusDaCampanha;
import static ecoa.regras.RegrasDaCampanha.validarDatasDaCampanha;
import static ecoa.regras.RegrasDaCampanha.validarEncerramento;
import static ecoa.regras.RegrasDaCampanha.validarEntrada;
import static ecoa.regras.RegrasDaCampanha.validarMudancaNaCampanha;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

  @Test
  void recusaMudancaEmCampanhaEncerrada() {
    recusado(() -> validarMudancaNaCampanha(StatusCampanha.ENCERRADA), Recusa.CAMPANHA_ENCERRADA);
  }

  @Test
  void aceitaMudancaEmCampanhaEmApuracao() {
    // Depois do fim, a campanha ainda muda até ser encerrada: é como o fim se adia (UC-06 2f).
    assertThatCode(() -> validarMudancaNaCampanha(StatusCampanha.EM_APURACAO))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(value = StatusCriador.class, names = "APROVADA", mode = EnumSource.Mode.EXCLUDE)
  void recusaEntradaDeContaQueNaoEstaAprovada(StatusCriador conta) {
    recusado(() -> validarEntrada(conta, StatusCampanha.ABERTA), Recusa.CONTA_NAO_APROVADA);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCampanha.class,
      names = {"AGENDADA", "ABERTA"})
  void aceitaEntradaEmCampanhaAgendadaOuAberta(StatusCampanha campanha) {
    assertThatCode(() -> validarEntrada(StatusCriador.APROVADA, campanha))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCampanha.class,
      names = {"EM_APURACAO", "ENCERRADA"})
  void recusaEntradaEmCampanhaEmApuracaoOuEncerrada(StatusCampanha campanha) {
    recusado(() -> validarEntrada(StatusCriador.APROVADA, campanha), Recusa.ENTRADA_ENCERRADA);
  }

  @Test
  void aceitaEncerrarNoDiaDoFechamento() {
    assertThatCode(
            () ->
                validarEncerramento(
                    List.of(participante(StatusCriador.APROVADA, aprovado(10, FECHAMENTO))),
                    FECHAMENTO,
                    FECHAMENTO))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaEncerrarNaVesperaDoFechamento() {
    recusado(
        () -> validarEncerramento(List.of(), FECHAMENTO, FECHAMENTO.minusDays(1)),
        Recusa.ENCERRAMENTO_ANTES_DO_FECHAMENTO);
  }

  @Test
  void videoEmAnalisePendeMesmoDeCriadorBloqueado() {
    List<ParticipanteDaCampanha> participantes =
        List.of(
            participante(StatusCriador.APROVADA, emAnalise(10)),
            participante(StatusCriador.BLOQUEADA, emAnalise(20)));
    assertThatThrownBy(() -> validarEncerramento(participantes, FECHAMENTO, FECHAMENTO))
        .isInstanceOfSatisfying(
            RegraVioladaException.class,
            e -> {
              assertThat(e.recusa()).isEqualTo(Recusa.ENCERRAMENTO_COM_PENDENCIA);
              assertThat(e.itens()).containsExactlyInAnyOrder(10, 20);
            });
  }

  @Test
  void videoQueContaSemLeituraDeFechamentoPende() {
    // O vídeo 10 só tem leitura parcial, da véspera do fechamento.
    List<ParticipanteDaCampanha> participantes =
        List.of(
            participante(
                StatusCriador.APROVADA,
                aprovado(10, FECHAMENTO.minusDays(1)),
                aprovado(11, FECHAMENTO)));
    assertThatThrownBy(() -> validarEncerramento(participantes, FECHAMENTO, FECHAMENTO))
        .isInstanceOfSatisfying(
            RegraVioladaException.class,
            e -> {
              assertThat(e.recusa()).isEqualTo(Recusa.ENCERRAMENTO_COM_PENDENCIA);
              assertThat(e.itens()).containsExactly(10);
            });
  }

  @Test
  void videoQueNaoContaNaoPedeLeituraDeFechamento() {
    // Aprovado de criador bloqueado e desqualificado não contam (RN-45, UC-14 2d).
    List<ParticipanteDaCampanha> participantes =
        List.of(
            participante(StatusCriador.BLOQUEADA, aprovado(10, FECHAMENTO.minusDays(1))),
            participante(
                StatusCriador.APROVADA,
                desqualificado(20, FECHAMENTO.minusDays(1)),
                aprovado(21, FECHAMENTO)));
    assertThatCode(() -> validarEncerramento(participantes, FECHAMENTO, FECHAMENTO))
        .doesNotThrowAnyException();
  }

  private static ParticipanteDaCampanha participante(
      StatusCriador criador, VideoDaCampanha... videos) {
    return new ParticipanteDaCampanha(1, criador, List.of(videos));
  }

  private static VideoDaCampanha aprovado(int idVideo, LocalDate dataDaLeitura) {
    return new VideoDaCampanha(
        idVideo, StatusVideo.APROVADO, List.of(new LeituraDoVideo(dataDaLeitura, 100)));
  }

  private static VideoDaCampanha desqualificado(int idVideo, LocalDate dataDaLeitura) {
    return new VideoDaCampanha(
        idVideo, StatusVideo.DESQUALIFICADO, List.of(new LeituraDoVideo(dataDaLeitura, 100)));
  }

  private static VideoDaCampanha emAnalise(int idVideo) {
    return new VideoDaCampanha(idVideo, StatusVideo.EM_ANALISE, List.of());
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
