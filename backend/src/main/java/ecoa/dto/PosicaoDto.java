package ecoa.dto;

import java.util.List;

/**
 * Uma linha do ranking, com os vídeos que contam embaixo (RN-45, RN-46). O @ é nulo para o
 * participante removido, que a tela mostra como tal (RN-59).
 */
public record PosicaoDto(
    int posicao, String usuarioTiktok, long soma, List<VideoNoRankingDto> videos) {}
