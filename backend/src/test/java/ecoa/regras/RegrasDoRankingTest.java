package ecoa.regras;

import static ecoa.regras.RegrasDoRanking.calcularRanking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasDoRankingTest {

  private static final LocalDate DIA = LocalDate.of(2026, 11, 20);

  @Test
  void empateDivideAPosicaoEPulaAsEmpatadas() {
    List<ParticipanteDaCampanha> participantes =
        List.of(
            participante(3, aprovado(30, 300)),
            participante(1, aprovado(10, 500)),
            participante(2, aprovado(20, 500)));
    assertThat(calcularRanking(participantes))
        .extracting(PosicaoNoRanking::idParticipacao, PosicaoNoRanking::posicao)
        .containsExactly(tuple(1, 1), tuple(2, 1), tuple(3, 3));
  }

  @Test
  void somaONumeroAtualDeCadaVideo() {
    // O primeiro vídeo caiu de 100 para 80: entra o 80, da leitura mais recente.
    VideoDaCampanha caiu =
        new VideoDaCampanha(
            10,
            StatusVideo.APROVADO,
            List.of(new LeituraDoVideo(DIA.minusDays(10), 100), new LeituraDoVideo(DIA, 80)));
    assertThat(calcularRanking(List.of(participante(1, caiu, aprovado(20, 50)))))
        .extracting(PosicaoNoRanking::soma)
        .containsExactly(130L);
  }

  @Test
  void criadorBloqueadoFicaFora() {
    ParticipanteDaCampanha bloqueado =
        new ParticipanteDaCampanha(1, StatusCriador.BLOQUEADA, List.of(aprovado(10, 1_000)));
    assertThat(calcularRanking(List.of(bloqueado, participante(2, aprovado(20, 100)))))
        .extracting(PosicaoNoRanking::idParticipacao)
        .containsExactly(2);
  }

  @Test
  void videoDesqualificadoNaoEntraNaSoma() {
    assertThat(
            calcularRanking(List.of(participante(1, aprovado(10, 100), desqualificado(11, 5_000)))))
        .extracting(PosicaoNoRanking::soma)
        .containsExactly(100L);
  }

  @Test
  void participanteSemVideoQueContaFicaFora() {
    List<ParticipanteDaCampanha> participantes =
        List.of(
            participante(1, desqualificado(10, 5_000)),
            participante(2, new VideoDaCampanha(20, StatusVideo.EM_ANALISE, List.of())),
            participante(3),
            participante(4, aprovado(40, 100)));
    assertThat(calcularRanking(participantes))
        .extracting(PosicaoNoRanking::idParticipacao)
        .containsExactly(4);
  }

  @Test
  void videoAprovadoSemLeituraContaZero() {
    // Todas as leituras apagadas: continua no ranking, com zero (RN-43).
    VideoDaCampanha semLeitura = new VideoDaCampanha(10, StatusVideo.APROVADO, List.of());
    assertThat(calcularRanking(List.of(participante(1, semLeitura))))
        .extracting(PosicaoNoRanking::idParticipacao, PosicaoNoRanking::soma)
        .containsExactly(tuple(1, 0L));
  }

  private static ParticipanteDaCampanha participante(
      int idParticipacao, VideoDaCampanha... videos) {
    return new ParticipanteDaCampanha(idParticipacao, StatusCriador.APROVADA, List.of(videos));
  }

  private static VideoDaCampanha aprovado(int idVideo, long views) {
    return new VideoDaCampanha(
        idVideo, StatusVideo.APROVADO, List.of(new LeituraDoVideo(DIA, views)));
  }

  private static VideoDaCampanha desqualificado(int idVideo, long views) {
    return new VideoDaCampanha(
        idVideo, StatusVideo.DESQUALIFICADO, List.of(new LeituraDoVideo(DIA, views)));
  }
}
