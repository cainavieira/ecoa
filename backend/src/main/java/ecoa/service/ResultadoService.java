package ecoa.service;

import ecoa.dto.ResultadoDto;
import ecoa.entity.Campanha;
import ecoa.entity.Participacao;
import ecoa.entity.Video;
import ecoa.mapper.ResultadoMapper;
import ecoa.regras.Funcao;
import ecoa.regras.LeituraDoVideo;
import ecoa.regras.ParticipanteDaCampanha;
import ecoa.regras.ParticipanteDoRankingFinal;
import ecoa.regras.PosicaoNoRanking;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDoRanking;
import ecoa.regras.StatusCampanha;
import ecoa.regras.TotalNaData;
import ecoa.regras.VideoDaCampanha;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.GanhadorRepository;
import ecoa.repository.LeituraRepository;
import ecoa.repository.ParticipacaoRepository;
import ecoa.repository.VideoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultadoService {

  private final CampanhaRepository campanhas;
  private final ParticipacaoRepository participacoes;
  private final VideoRepository videos;
  private final LeituraRepository leituras;
  private final GanhadorRepository ganhadores;
  private final Clock relogio;

  public ResultadoService(
      CampanhaRepository campanhas,
      ParticipacaoRepository participacoes,
      VideoRepository videos,
      LeituraRepository leituras,
      GanhadorRepository ganhadores,
      Clock relogio) {
    this.campanhas = campanhas;
    this.participacoes = participacoes;
    this.videos = videos;
    this.leituras = leituras;
    this.ganhadores = ganhadores;
    this.relogio = relogio;
  }

  /**
   * O resultado que o participante, a gravadora e o operador veem, igual para os três (RN-47). Na
   * campanha encerrada, o ranking é o que o encerramento gravou, e não o do estado atual (RN-48). O
   * total de hoje é o último ponto do gráfico, porque leitura não tem data futura (RN-40, RN-61).
   */
  @Transactional(readOnly = true)
  public ResultadoDto resultado(int idCampanha, int idConta, Funcao funcao) {
    Campanha campanha =
        campanhas
            .findById(idCampanha)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
    if (funcao == Funcao.CRIADOR
        && !participacoes.existsByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idConta)) {
      throw new RegraVioladaException(
          Recusa.NAO_PARTICIPANTE, "Só quem participa da campanha vê o ranking.");
    }
    boolean encerrada = campanha.status(LocalDate.now(relogio)) == StatusCampanha.ENCERRADA;
    List<Participacao> daCampanha = participacoes.daCampanha(idCampanha);
    List<Video> videosDaCampanha = videos.daCampanha(idCampanha);
    Map<Integer, List<VideoDaCampanha>> videosPorParticipacao =
        videosPorParticipacao(videosDaCampanha);
    List<PosicaoNoRanking> ranking =
        encerrada
            ? RegrasDoRanking.rankingFinal(
                daCampanha.stream()
                    .filter(participacao -> participacao.posicaoFinal() != null)
                    .map(
                        participacao ->
                            new ParticipanteDoRankingFinal(
                                participacao.idParticipacao(),
                                participacao.posicaoFinal(),
                                participacao.totalFinal(),
                                videosPorParticipacao.getOrDefault(
                                    participacao.idParticipacao(), List.of())))
                    .toList())
            : RegrasDoRanking.calcularRanking(
                daCampanha.stream()
                    // O participante removido só existe na campanha encerrada (RN-59).
                    .filter(participacao -> participacao.criador() != null)
                    .map(
                        participacao ->
                            new ParticipanteDaCampanha(
                                participacao.idParticipacao(),
                                participacao.criador().status(),
                                videosPorParticipacao.getOrDefault(
                                    participacao.idParticipacao(), List.of())))
                    .toList());
    List<VideoDaCampanha> queContam =
        ranking.stream().flatMap(posicao -> posicao.videos().stream()).toList();
    List<TotalNaData> grafico = RegrasDoRanking.totalPorData(queContam);
    Map<Integer, Participacao> participacaoPorId =
        daCampanha.stream()
            .collect(Collectors.toMap(Participacao::idParticipacao, Function.identity()));
    Map<Integer, Video> videoPorId =
        videosDaCampanha.stream().collect(Collectors.toMap(Video::idVideo, Function.identity()));
    return new ResultadoDto(
        encerrada,
        grafico.isEmpty() ? 0 : grafico.getLast().total(),
        ranking.size(),
        queContam.size(),
        ranking.stream()
            .map(
                posicao ->
                    ResultadoMapper.posicao(
                        posicao, participacaoPorId.get(posicao.idParticipacao()), videoPorId))
            .toList(),
        grafico.stream().map(ResultadoMapper::ponto).toList(),
        ganhadores.daCampanha(idCampanha).stream().map(ResultadoMapper::ganhador).toList());
  }

  /** Os vídeos de cada participação, com as leituras, como a regra do ranking os recebe. */
  private Map<Integer, List<VideoDaCampanha>> videosPorParticipacao(List<Video> videosDaCampanha) {
    Map<Integer, List<LeituraDoVideo>> leiturasPorVideo =
        leituras.porVideo(videosDaCampanha.stream().map(Video::idVideo).toList());
    Map<Integer, List<VideoDaCampanha>> porParticipacao = new HashMap<>();
    for (Video video : videosDaCampanha) {
      porParticipacao
          .computeIfAbsent(video.participacao().idParticipacao(), id -> new ArrayList<>())
          .add(
              new VideoDaCampanha(
                  video.idVideo(),
                  video.status(),
                  leiturasPorVideo.getOrDefault(video.idVideo(), List.of())));
    }
    return porParticipacao;
  }
}
