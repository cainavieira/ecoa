package ecoa.dto;

import java.time.LocalDate;

/**
 * Uma linha da planilha de leituras: um vídeo que conta, com o @, o link, a publicação, o número
 * atual e se ele já tem leitura de fechamento (RF-32, RNF-26). O número atual é nulo quando o vídeo
 * não tem leitura, e então ele conta zero (RN-43).
 */
public record LinhaDaPlanilhaDto(
    int idVideo,
    String usuarioTiktok,
    String link,
    LocalDate dataPublicacao,
    LeituraDto numeroAtual,
    boolean temLeituraDeFechamento) {}
