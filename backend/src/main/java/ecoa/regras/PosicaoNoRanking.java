package ecoa.regras;

import java.util.List;

/** Uma linha do ranking, com os vídeos que contam (RN-45). */
public record PosicaoNoRanking(
    int posicao, int idParticipacao, long soma, List<VideoDaCampanha> videos) {}
