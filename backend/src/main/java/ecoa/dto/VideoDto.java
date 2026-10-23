package ecoa.dto;

import ecoa.regras.StatusVideo;
import java.time.LocalDate;

/**
 * Um vídeo para o operador, na fila de conferência e depois de cada transição: o link e o @ do
 * criador, que ele confere no TikTok (RF-27). O @ é nulo no vídeo de participante removido (RN-59).
 */
public record VideoDto(
    int id,
    int idCampanha,
    String usuarioTiktok,
    String link,
    StatusVideo status,
    LocalDate dataPublicacao,
    String motivo) {}
