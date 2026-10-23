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
import ecoa.regras.RegrasDaCampanha;
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
import java.time.Instant;
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

  /** O resultado que o participante, a gravadora e o operador veem, igual para os três (RN-47). */
  @Transactional(readOnly = true)
  public ResultadoDto resultado(int idCampanha, int idConta, Funcao funcao) {
    Campanha campanha = campanha(idCampanha);
    if (funcao == Funcao.CRIADOR
        && !participacoes.existsByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idConta)) {
      throw new RegraVioladaException(
          Recusa.NAO_PARTICIPANTE, "Só quem participa da campanha vê o ranking.");
    }
    return montar(campanha, retratar(idCampanha));
  }

  /**
   * Encerra a campanha a partir do fechamento, sem vídeo em análise e com leitura de fechamento em
   * todo vídeo que conta (RN-50), e grava o ranking final, calculado uma última vez (RN-48). Com as
   * leituras de fechamento presentes, o número atual de cada vídeo que conta é a dele (RN-41).
   * Encerrar é definitivo (RN-28).
   */
  @Transactional
  public ResultadoDto encerrar(int idCampanha) {
    Campanha campanha = campanha(idCampanha);
    LocalDate hoje = LocalDate.now(relogio);
    RegrasDaCampanha.validarMudancaNaCampanha(campanha.status(hoje));
    Retrato retrato = retratar(idCampanha);
    List<ParticipanteDaCampanha> participantes = retrato.participantes();
    RegrasDaCampanha.validarEncerramento(participantes, campanha.datas().fechamento(), hoje);
    for (PosicaoNoRanking posicao : RegrasDoRanking.calcularRanking(participantes)) {
      retrato
          .participacao(posicao.idParticipacao())
          .gravarRankingFinal(posicao.posicao(), posicao.soma());
    }
    campanha.encerrar(Instant.now(relogio));
    return montar(campanha, retrato);
  }

  /**
   * Na campanha encerrada, o ranking é o que o encerramento gravou, e não o do estado atual
   * (RN-48). O total de hoje é o último ponto do gráfico, porque leitura não tem data futura
   * (RN-40, RN-61).
   */
  private ResultadoDto montar(Campanha campanha, Retrato retrato) {
    boolean encerrada = campanha.status(LocalDate.now(relogio)) == StatusCampanha.ENCERRADA;
    List<PosicaoNoRanking> ranking =
        encerrada
            ? RegrasDoRanking.rankingFinal(retrato.doRankingFinal())
            : RegrasDoRanking.calcularRanking(retrato.participantes());
    List<VideoDaCampanha> queContam =
        ranking.stream().flatMap(posicao -> posicao.videos().stream()).toList();
    List<TotalNaData> grafico = RegrasDoRanking.totalPorData(queContam);
    Map<Integer, Video> videoPorId =
        retrato.videos().stream().collect(Collectors.toMap(Video::idVideo, Function.identity()));
    return new ResultadoDto(
        encerrada,
        grafico.isEmpty() ? 0 : grafico.getLast().total(),
        ranking.size(),
        queContam.size(),
        ranking.stream()
            .map(
                posicao ->
                    ResultadoMapper.posicao(
                        posicao, retrato.participacao(posicao.idParticipacao()), videoPorId))
            .toList(),
        grafico.stream().map(ResultadoMapper::ponto).toList(),
        ganhadores.daCampanha(campanha.idCampanha()).stream()
            .map(ResultadoMapper::ganhador)
            .toList());
  }

  /** As participações e os vídeos da campanha, com as leituras, lidos de uma vez. */
  private Retrato retratar(int idCampanha) {
    List<Video> videosDaCampanha = videos.daCampanha(idCampanha);
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
    return new Retrato(participacoes.daCampanha(idCampanha), videosDaCampanha, porParticipacao);
  }

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }

  /**
   * A campanha como a regra do ranking a recebe. As participações vêm na ordem do ranking final e
   * do @, que desempata o ranking calculado.
   */
  private record Retrato(
      List<Participacao> participacoes,
      List<Video> videos,
      Map<Integer, List<VideoDaCampanha>> videosPorParticipacao) {

    List<ParticipanteDaCampanha> participantes() {
      return participacoes.stream()
          // O participante removido só existe na campanha encerrada (RN-59).
          .filter(participacao -> participacao.criador() != null)
          .map(
              participacao ->
                  new ParticipanteDaCampanha(
                      participacao.idParticipacao(),
                      participacao.criador().status(),
                      videosDe(participacao)))
          .toList();
    }

    /** Quem ficou fora do ranking final não tem posição gravada. */
    List<ParticipanteDoRankingFinal> doRankingFinal() {
      return participacoes.stream()
          .filter(participacao -> participacao.posicaoFinal() != null)
          .map(
              participacao ->
                  new ParticipanteDoRankingFinal(
                      participacao.idParticipacao(),
                      participacao.posicaoFinal(),
                      participacao.totalFinal(),
                      videosDe(participacao)))
          .toList();
    }

    Participacao participacao(int idParticipacao) {
      return participacoes.stream()
          .filter(participacao -> participacao.idParticipacao() == idParticipacao)
          .findFirst()
          .orElseThrow();
    }

    private List<VideoDaCampanha> videosDe(Participacao participacao) {
      return videosPorParticipacao.getOrDefault(participacao.idParticipacao(), List.of());
    }
  }
}
