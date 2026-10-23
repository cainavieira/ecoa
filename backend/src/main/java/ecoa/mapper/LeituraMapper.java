package ecoa.mapper;

import ecoa.dto.LeituraDoHistoricoDto;
import ecoa.dto.LeituraDto;
import ecoa.dto.LinhaDaPlanilhaDto;
import ecoa.entity.Video;
import ecoa.regras.LeituraDoVideo;

public final class LeituraMapper {

  private LeituraMapper() {}

  public static LeituraDto paraDto(LeituraDoVideo leitura) {
    return new LeituraDto(leitura.data(), leitura.views());
  }

  public static LeituraDoHistoricoDto doHistorico(LeituraDoVideo leitura, boolean deFechamento) {
    return new LeituraDoHistoricoDto(leitura.data(), leitura.views(), deFechamento);
  }

  /** O número atual vem da regra, e é nulo quando o vídeo não tem leitura. */
  public static LinhaDaPlanilhaDto linha(
      Video video, LeituraDoVideo numeroAtual, boolean temLeituraDeFechamento) {
    return new LinhaDaPlanilhaDto(
        video.idVideo(),
        video.participacao().criador().usuarioTiktok(),
        video.link(),
        video.dataPublicacao(),
        numeroAtual == null ? null : paraDto(numeroAtual),
        temLeituraDeFechamento);
  }
}
