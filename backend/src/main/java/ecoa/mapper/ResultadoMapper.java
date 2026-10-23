package ecoa.mapper;

import ecoa.dto.GanhadorDto;
import ecoa.dto.PontoDoGraficoDto;
import ecoa.dto.PosicaoDto;
import ecoa.dto.VideoNoRankingDto;
import ecoa.entity.Criador;
import ecoa.entity.Ganhador;
import ecoa.entity.Participacao;
import ecoa.entity.Video;
import ecoa.regras.PosicaoNoRanking;
import ecoa.regras.RegrasDaLeitura;
import ecoa.regras.TotalNaData;
import java.util.Map;

/** O resultado sem e-mail nem código de verificação: só o @, que o aceite deixa mostrar (RN-31). */
public final class ResultadoMapper {

  private ResultadoMapper() {}

  public static PosicaoDto posicao(
      PosicaoNoRanking posicao, Participacao participacao, Map<Integer, Video> videoPorId) {
    return new PosicaoDto(
        posicao.posicao(),
        usuarioTiktok(participacao),
        posicao.soma(),
        posicao.videos().stream()
            .map(
                video ->
                    new VideoNoRankingDto(
                        video.idVideo(),
                        videoPorId.get(video.idVideo()).link(),
                        RegrasDaLeitura.numeroAtual(video.leituras())
                            .map(LeituraMapper::paraDto)
                            .orElse(null)))
            .toList());
  }

  public static PontoDoGraficoDto ponto(TotalNaData total) {
    return new PontoDoGraficoDto(total.data(), total.total());
  }

  public static GanhadorDto ganhador(Ganhador ganhador) {
    return new GanhadorDto(
        ganhador.idGanhador(),
        usuarioTiktok(ganhador.participacao()),
        ganhador.premio(),
        ganhador.entregueEm());
  }

  /** Nulo para o participante removido (RN-59). */
  private static String usuarioTiktok(Participacao participacao) {
    Criador criador = participacao.criador();
    return criador == null ? null : criador.usuarioTiktok();
  }
}
