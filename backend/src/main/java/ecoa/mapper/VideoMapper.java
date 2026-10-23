package ecoa.mapper;

import ecoa.dto.LeituraDto;
import ecoa.dto.MeuVideoDto;
import ecoa.entity.Video;
import ecoa.regras.LeituraDoVideo;
import java.util.List;

public final class VideoMapper {

  private VideoMapper() {}

  /** O número atual vem da regra, e é nulo quando o vídeo não tem leitura. */
  public static MeuVideoDto meuVideo(
      Video video, List<LeituraDoVideo> leituras, LeituraDoVideo numeroAtual) {
    return new MeuVideoDto(
        video.idVideo(),
        video.link(),
        video.status(),
        video.dataPublicacao(),
        video.motivo(),
        numeroAtual == null ? null : leitura(numeroAtual),
        leituras.stream().map(VideoMapper::leitura).toList());
  }

  private static LeituraDto leitura(LeituraDoVideo leitura) {
    return new LeituraDto(leitura.data(), leitura.views());
  }
}
