package ecoa.regras;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public final class RegrasDoVideo {

  private RegrasDoVideo() {}

  /**
   * Envio com a campanha aberta, por link https do TikTok (RN-34), que não esteja em outro envio
   * ainda não recusado, em campanha nenhuma (RN-35).
   *
   * @param enviosDoLink os status dos envios anteriores do mesmo link
   */
  public static void validarEnvioDeVideo(
      String link, StatusCampanha campanha, List<StatusVideo> enviosDoLink) {
    if (campanha != StatusCampanha.ABERTA) {
      throw new RegraVioladaException(
          Recusa.CAMPANHA_NAO_ABERTA, "O envio de vídeo só acontece com a campanha aberta.");
    }
    if (!linkDoTiktok(link)) {
      throw new RegraVioladaException(
          Recusa.LINK_NAO_TIKTOK, "O link precisa ser https e do tiktok.com.");
    }
    if (enviosDoLink.stream().anyMatch(status -> status != StatusVideo.RECUSADO)) {
      throw new RegraVioladaException(Recusa.LINK_JA_ENVIADO, "Este vídeo já está na plataforma.");
    }
  }

  /**
   * Só o vídeo em análise, de criador que não está bloqueado (RN-19), com a publicação entre o
   * início e o fim (RN-36).
   */
  public static void validarAprovacaoDeVideo(
      StatusVideo video,
      StatusCriador criador,
      LocalDate dataPublicacao,
      LocalDate inicio,
      LocalDate fim) {
    if (video != StatusVideo.EM_ANALISE) {
      throw new RegraVioladaException(
          Recusa.VIDEO_NAO_EM_ANALISE, "Só o vídeo em análise pode ser aprovado.");
    }
    if (criador == StatusCriador.BLOQUEADA) {
      throw new RegraVioladaException(
          Recusa.CRIADOR_BLOQUEADO,
          "O criador está bloqueado, e o vídeo dele não pode ser aprovado enquanto durar o"
              + " bloqueio.");
    }
    if (dataPublicacao.isBefore(inicio) || dataPublicacao.isAfter(fim)) {
      throw new RegraVioladaException(
          Recusa.PUBLICACAO_FORA_DO_PRAZO,
          "A data de publicação precisa estar entre "
              + Formato.data(inicio)
              + " e "
              + Formato.data(fim)
              + ".");
    }
  }

  /** Só o vídeo aprovado é desqualificado ou recebe leitura (RN-38, RN-39). */
  public static void validarVideoAprovado(StatusVideo video) {
    if (video != StatusVideo.APROVADO) {
      throw new RegraVioladaException(
          Recusa.VIDEO_NAO_APROVADO, "Esta ação só vale para vídeo aprovado.");
    }
  }

  /** O domínio é tiktok.com ou termina em .tiktok.com, com o ponto (RN-34). */
  private static boolean linkDoTiktok(String link) {
    URI uri;
    try {
      uri = new URI(link);
    } catch (URISyntaxException e) {
      return false;
    }
    String host = uri.getHost();
    if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) {
      return false;
    }
    String dominio = host.toLowerCase(Locale.ROOT);
    return dominio.equals("tiktok.com") || dominio.endsWith(".tiktok.com");
  }
}
