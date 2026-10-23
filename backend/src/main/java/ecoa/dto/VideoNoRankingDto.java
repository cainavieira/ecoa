package ecoa.dto;

/**
 * Um vídeo que conta, com o número atual e a data dele (RN-43, RN-45). O link é nulo no vídeo do
 * participante removido (RN-59), e o número atual, no vídeo sem leitura, que conta zero.
 */
public record VideoNoRankingDto(int idVideo, String link, LeituraDto numeroAtual) {}
