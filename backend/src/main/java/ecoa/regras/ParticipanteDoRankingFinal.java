package ecoa.regras;

import java.util.List;

/** Um participante com a posição e a soma que o encerramento gravou, e os vídeos dele (RN-48). */
public record ParticipanteDoRankingFinal(
    int idParticipacao, int posicao, long soma, List<VideoDaCampanha> videos) {}
