package ecoa.dto;

import ecoa.regras.StatusVideo;
import java.time.LocalDate;
import java.util.List;

/**
 * Um vídeo do próprio criador, em qualquer status, com o motivo da recusa ou da desqualificação e
 * as leituras do gráfico (RF-31). O número atual é a leitura mais recente, e é nulo quando o vídeo
 * não tem leitura: o aprovado sem leitura conta zero (RN-43).
 */
public record MeuVideoDto(
    int id,
    String link,
    StatusVideo status,
    LocalDate dataPublicacao,
    String motivo,
    LeituraDto numeroAtual,
    List<LeituraDto> leituras) {}
