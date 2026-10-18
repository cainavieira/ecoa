package ecoa.regras;

import java.net.URI;
import java.net.URISyntaxException;
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
