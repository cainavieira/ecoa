package ecoa.regras;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RegrasDoRanking {

  private RegrasDoRanking() {}

  /**
   * Conta o vídeo aprovado de criador não bloqueado, e fica no ranking quem tem ao menos um, em
   * ordem decrescente da soma do número atual; vídeo sem leitura soma zero (RN-43, RN-45). Empate
   * divide a posição, e a seguinte pula as empatadas (RN-46). Entre os empatados, fica a ordem de
   * entrada.
   */
  public static List<PosicaoNoRanking> calcularRanking(List<ParticipanteDaCampanha> participantes) {
    record Soma(int idParticipacao, long soma, List<VideoDaCampanha> videos) {}
    List<Soma> somas =
        participantes.stream()
            .filter(participante -> participante.criador() != StatusCriador.BLOQUEADA)
            .map(
                participante -> {
                  List<VideoDaCampanha> contam =
                      participante.videos().stream()
                          .filter(video -> video.status() == StatusVideo.APROVADO)
                          .toList();
                  long soma =
                      contam.stream()
                          .mapToLong(
                              video ->
                                  RegrasDaLeitura.numeroAtual(video.leituras())
                                      .map(LeituraDoVideo::views)
                                      .orElse(0L))
                          .sum();
                  return new Soma(participante.idParticipacao(), soma, contam);
                })
            .filter(soma -> !soma.videos().isEmpty())
            .sorted(Comparator.comparingLong(Soma::soma).reversed())
            .toList();
    List<PosicaoNoRanking> ranking = new ArrayList<>();
    for (int i = 0; i < somas.size(); i++) {
      Soma atual = somas.get(i);
      int posicao =
          i > 0 && atual.soma() == somas.get(i - 1).soma() ? ranking.get(i - 1).posicao() : i + 1;
      ranking.add(
          new PosicaoNoRanking(posicao, atual.idParticipacao(), atual.soma(), atual.videos()));
    }
    return ranking;
  }

  /**
   * Um ponto por data com leitura, com a soma, de cada vídeo, da leitura mais recente até aquela
   * data; vídeo sem leitura até a data não soma (RN-49). Recebe só os vídeos que contam: na
   * campanha encerrada, os do ranking final, mesmo de criador bloqueado depois (RN-48).
   */
  public static List<TotalNaData> totalPorData(List<VideoDaCampanha> videosQueContam) {
    return videosQueContam.stream()
        .flatMap(video -> video.leituras().stream())
        .map(LeituraDoVideo::data)
        .distinct()
        .sorted()
        .map(data -> new TotalNaData(data, totalNaData(videosQueContam, data)))
        .toList();
  }

  private static long totalNaData(List<VideoDaCampanha> videos, LocalDate data) {
    return videos.stream()
        .mapToLong(
            video ->
                RegrasDaLeitura.numeroAtual(
                        video.leituras().stream()
                            .filter(leitura -> !leitura.data().isAfter(data))
                            .toList())
                    .map(LeituraDoVideo::views)
                    .orElse(0L))
        .sum();
  }
}
