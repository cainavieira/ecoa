package ecoa.regras;

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
}
