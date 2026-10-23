package ecoa.regras;

import static ecoa.regras.RegrasDoRanking.calcularRanking;
import static ecoa.regras.RegrasDoRanking.rankingFinal;
import static ecoa.regras.RegrasDoRanking.totalPorData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasDoRankingTest {

  private static final LocalDate DIA = LocalDate.of(2026, 11, 20);

  @Test
  void rankingFinalTemAPosicaoEASomaGravadasESoOsVideosAprovados() {
    // O desqualificado antes do encerramento não contava; a soma gravada não é refeita (RN-48).
    List<ParticipanteDoRankingFinal> gravados =
        List.of(
            new ParticipanteDoRankingFinal(2, 2, 200, List.of(aprovado(20, 999))),
            new ParticipanteDoRankingFinal(
                1, 1, 500, List.of(aprovado(10, 500), desqualificado(11, 900))));
    assertThat(rankingFinal(gravados))
        .extracting(
            PosicaoNoRanking::idParticipacao,
            PosicaoNoRanking::posicao,
            PosicaoNoRanking::soma,
            posicao -> posicao.videos().stream().map(VideoDaCampanha::idVideo).toList())
        .containsExactly(tuple(1, 1, 500L, List.of(10)), tuple(2, 2, 200L, List.of(20)));
  }

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

  @Test
  void totalDeCadaDataSomaALeituraMaisRecenteDeCadaVideo() {
    // O vídeo 10 caiu de 100 para 80; o 20 só tem leitura a partir de 15/11; os dois têm em 20/11.
    LocalDate dia10 = LocalDate.of(2026, 11, 10);
    LocalDate dia15 = LocalDate.of(2026, 11, 15);
    LocalDate dia20 = LocalDate.of(2026, 11, 20);
    VideoDaCampanha caiu =
        new VideoDaCampanha(
            10,
            StatusVideo.APROVADO,
            List.of(new LeituraDoVideo(dia10, 100), new LeituraDoVideo(dia20, 80)));
    VideoDaCampanha tardio =
        new VideoDaCampanha(
            20,
            StatusVideo.APROVADO,
            List.of(new LeituraDoVideo(dia20, 60), new LeituraDoVideo(dia15, 50)));
    assertThat(totalPorData(List.of(caiu, tardio)))
        .extracting(TotalNaData::data, TotalNaData::total)
        .containsExactly(tuple(dia10, 100L), tuple(dia15, 150L), tuple(dia20, 140L));
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
