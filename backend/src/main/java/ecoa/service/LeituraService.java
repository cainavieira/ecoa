package ecoa.service;

import ecoa.dto.LeituraDoHistoricoDto;
import ecoa.dto.LinhaDaPlanilhaDto;
import ecoa.entity.Campanha;
import ecoa.entity.Leitura;
import ecoa.entity.Participacao;
import ecoa.entity.Video;
import ecoa.mapper.LeituraMapper;
import ecoa.regras.LeituraDoVideo;
import ecoa.regras.ParticipanteDaCampanha;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaCampanha;
import ecoa.regras.RegrasDaLeitura;
import ecoa.regras.RegrasDoRanking;
import ecoa.regras.RegrasDoVideo;
import ecoa.regras.StatusCriador;
import ecoa.regras.VideoDaCampanha;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.LeituraRepository;
import ecoa.repository.VideoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeituraService {

  private final CampanhaRepository campanhas;
  private final VideoRepository videos;
  private final LeituraRepository leituras;
  private final Clock relogio;

  public LeituraService(
      CampanhaRepository campanhas,
      VideoRepository videos,
      LeituraRepository leituras,
      Clock relogio) {
    this.campanhas = campanhas;
    this.videos = videos;
    this.leituras = leituras;
    this.relogio = relogio;
  }

  /**
   * A planilha da campanha: uma linha por vídeo que conta, com o número atual e se ele já tem
   * leitura de fechamento (RF-32, RN-41, RN-43). Quem conta é a regra do ranking: o vídeo aprovado
   * de criador não bloqueado (RN-45, UC-11 1a).
   */
  @Transactional(readOnly = true)
  public List<LinhaDaPlanilhaDto> planilha(int idCampanha) {
    Campanha campanha =
        campanhas
            .findById(idCampanha)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
    List<Video> daCampanha = videos.daCampanhaPeloCriador(idCampanha);
    Map<Integer, List<LeituraDoVideo>> leiturasPorVideo =
        leituras.porVideo(daCampanha.stream().map(Video::idVideo).toList());
    Set<Integer> contam =
        RegrasDoRanking.calcularRanking(participantes(daCampanha, leiturasPorVideo)).stream()
            .flatMap(posicao -> posicao.videos().stream())
            .map(VideoDaCampanha::idVideo)
            .collect(Collectors.toSet());
    LocalDate fechamento = campanha.datas().fechamento();
    return daCampanha.stream()
        .filter(video -> contam.contains(video.idVideo()))
        .map(
            video -> {
              List<LeituraDoVideo> doVideo =
                  leiturasPorVideo.getOrDefault(video.idVideo(), List.of());
              return LeituraMapper.linha(
                  video,
                  RegrasDaLeitura.numeroAtual(doVideo).orElse(null),
                  RegrasDaLeitura.leituraDeFechamento(doVideo, fechamento).isPresent());
            })
        .toList();
  }

  /** O histórico do vídeo, da leitura mais antiga para a mais nova (RF-33). */
  @Transactional(readOnly = true)
  public List<LeituraDoHistoricoDto> historico(int idVideo) {
    Video video =
        videos
            .findById(idVideo)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Vídeo não encontrado."));
    List<LeituraDoVideo> doVideo = leiturasDe(idVideo);
    Optional<LeituraDoVideo> deFechamento =
        RegrasDaLeitura.leituraDeFechamento(
            doVideo, video.participacao().campanha().datas().fechamento());
    return doVideo.stream()
        .map(
            leitura ->
                LeituraMapper.doHistorico(
                    leitura, deFechamento.filter(leitura::equals).isPresent()))
        .toList();
  }

  /**
   * Grava a leitura da data, ou substitui a que existia (RN-40), no vídeo aprovado de campanha não
   * encerrada (RN-28, RN-39): nem futura, nem antes da publicação, e só uma a partir do fechamento
   * (RN-41).
   */
  @Transactional
  public LeituraDoHistoricoDto gravar(int idVideo, LocalDate data, long views) {
    Video video = video(idVideo);
    Campanha campanha = video.participacao().campanha();
    LocalDate hoje = LocalDate.now(relogio);
    RegrasDaCampanha.validarMudancaNaCampanha(campanha.status(hoje));
    RegrasDoVideo.validarVideoAprovado(video.status());
    LocalDate fechamento = campanha.datas().fechamento();
    RegrasDaLeitura.validarLeitura(
        video.dataPublicacao(), fechamento, leiturasDe(idVideo), data, hoje);
    // A leitura que já existe na data é atualizada; a que não existe, criada.
    leituras.save(new Leitura(video, data, views, Instant.now(relogio)));
    LeituraDoVideo gravada = new LeituraDoVideo(data, views);
    return LeituraMapper.doHistorico(
        gravada, RegrasDaLeitura.leituraDeFechamento(List.of(gravada), fechamento).isPresent());
  }

  /** Apaga a leitura da data, até o encerramento (RN-28, RN-42). */
  @Transactional
  public void apagar(int idVideo, LocalDate data) {
    Video video = video(idVideo);
    RegrasDaCampanha.validarMudancaNaCampanha(
        video.participacao().campanha().status(LocalDate.now(relogio)));
    Leitura leitura =
        leituras
            .findByVideoIdVideoAndIdData(idVideo, data)
            .orElseThrow(
                () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Leitura não encontrada."));
    leituras.delete(leitura);
  }

  /** Os participantes como a regra do ranking os recebe, com os vídeos e as leituras. */
  private static List<ParticipanteDaCampanha> participantes(
      List<Video> daCampanha, Map<Integer, List<LeituraDoVideo>> leiturasPorVideo) {
    Map<Integer, StatusCriador> statusPorParticipacao = new HashMap<>();
    Map<Integer, List<VideoDaCampanha>> videosPorParticipacao = new LinkedHashMap<>();
    for (Video video : daCampanha) {
      Participacao participacao = video.participacao();
      statusPorParticipacao.put(participacao.idParticipacao(), participacao.criador().status());
      videosPorParticipacao
          .computeIfAbsent(participacao.idParticipacao(), id -> new ArrayList<>())
          .add(
              new VideoDaCampanha(
                  video.idVideo(),
                  video.status(),
                  leiturasPorVideo.getOrDefault(video.idVideo(), List.of())));
    }
    return videosPorParticipacao.entrySet().stream()
        .map(
            entrada ->
                new ParticipanteDaCampanha(
                    entrada.getKey(),
                    statusPorParticipacao.get(entrada.getKey()),
                    entrada.getValue()))
        .toList();
  }

  private List<LeituraDoVideo> leiturasDe(int idVideo) {
    return leituras.porVideo(List.of(idVideo)).getOrDefault(idVideo, List.of());
  }

  /** Com a linha do vídeo travada: o duplo clique na planilha grava uma vez e depois substitui. */
  private Video video(int id) {
    return videos
        .travar(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Vídeo não encontrado."));
  }
}
