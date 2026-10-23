package ecoa.mapper;

import ecoa.dto.LeituraDto;
import ecoa.dto.MeuVideoDto;
import ecoa.dto.VideoDto;
import ecoa.entity.Criador;
import ecoa.entity.Participacao;
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

  public static VideoDto paraOperador(Video video) {
    Participacao participacao = video.participacao();
    Criador criador = participacao.criador();
    return new VideoDto(
        video.idVideo(),
        participacao.campanha().idCampanha(),
        criador == null ? null : criador.usuarioTiktok(),
        video.link(),
        video.status(),
        video.dataPublicacao(),
        video.motivo());
  }

  private static LeituraDto leitura(LeituraDoVideo leitura) {
    return new LeituraDto(leitura.data(), leitura.views());
  }
}
