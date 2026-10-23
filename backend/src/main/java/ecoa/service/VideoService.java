package ecoa.service;

import ecoa.dto.MeuVideoDto;
import ecoa.dto.VideoDto;
import ecoa.entity.Campanha;
import ecoa.entity.Leitura;
import ecoa.entity.Participacao;
import ecoa.entity.Video;
import ecoa.mapper.VideoMapper;
import ecoa.regras.DatasDaCampanha;
import ecoa.regras.LeituraDoVideo;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaCampanha;
import ecoa.regras.RegrasDaLeitura;
import ecoa.regras.RegrasDoVideo;
import ecoa.regras.StatusVideo;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.LeituraRepository;
import ecoa.repository.ParticipacaoRepository;
import ecoa.repository.VideoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VideoService {

  private final CampanhaRepository campanhas;
  private final ParticipacaoRepository participacoes;
  private final VideoRepository videos;
  private final LeituraRepository leituras;
  private final Clock relogio;

  public VideoService(
      CampanhaRepository campanhas,
      ParticipacaoRepository participacoes,
      VideoRepository videos,
      LeituraRepository leituras,
      Clock relogio) {
    this.campanhas = campanhas;
    this.participacoes = participacoes;
    this.videos = videos;
    this.leituras = leituras;
    this.relogio = relogio;
  }

  /**
   * O participante envia o link com a campanha aberta, e o vídeo fica em análise (RN-33, RN-34). O
   * link que já está noutro envio não recusado é recusado, inclusive no duplo clique, em que o
   * segundo envio esbarra no índice único (RN-35).
   */
  @Transactional
  public MeuVideoDto enviar(int idCampanha, int idConta, String link) {
    Campanha campanha = campanha(idCampanha);
    Participacao participacao =
        participacoes
            .findByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idConta)
            .orElseThrow(
                () ->
                    new RegraVioladaException(
                        Recusa.NAO_PARTICIPANTE, "Só quem participa da campanha envia vídeo."));
    // O link colado costuma vir com espaço ou quebra de linha em volta.
    String colado = link.strip();
    RegrasDoVideo.validarEnvioDeVideo(
        colado, campanha.status(LocalDate.now(relogio)), videos.statusDosEnvios(colado));
    if (videos.enviar(participacao.idParticipacao(), colado, Instant.now(relogio)) == 0) {
      throw new RegraVioladaException(Recusa.LINK_JA_ENVIADO, "Este vídeo já está na plataforma.");
    }
    Video video = videos.findByLinkAndStatusNot(colado, StatusVideo.RECUSADO).orElseThrow();
    return VideoMapper.meuVideo(video, List.of(), null);
  }

  /**
   * Os vídeos do criador na campanha, em todos os status, com as leituras (RF-31). De outro criador
   * não vem nenhum, e quem não participa recebe a lista vazia (ADR-0009).
   */
  @Transactional(readOnly = true)
  public List<MeuVideoDto> meusVideos(int idCampanha, int idConta) {
    campanha(idCampanha);
    List<Video> dele = videos.doCriadorNaCampanha(idCampanha, idConta);
    Map<Integer, List<LeituraDoVideo>> leiturasPorVideo =
        leituras.porVideo(dele.stream().map(Video::idVideo).toList());
    return dele.stream()
        .map(
            video -> {
              List<LeituraDoVideo> doVideo =
                  leiturasPorVideo.getOrDefault(video.idVideo(), List.of());
              return VideoMapper.meuVideo(
                  video, doVideo, RegrasDaLeitura.numeroAtual(doVideo).orElse(null));
            })
        .toList();
  }

  /** Todos os vídeos, do mais antigo ao mais novo, com o link e o @ (RF-27). */
  @Transactional(readOnly = true)
  public List<VideoDto> listar() {
    return videos.listarParaOperador().stream().map(VideoMapper::paraOperador).toList();
  }

  /** Os vídeos de um status; com em análise, a fila de conferência (RF-27). */
  @Transactional(readOnly = true)
  public List<VideoDto> listar(StatusVideo status) {
    return videos.listarParaOperador(status).stream().map(VideoMapper::paraOperador).toList();
  }

  /**
   * Aprova o vídeo em análise de criador não bloqueado, com a publicação entre o início e o fim, e
   * grava a primeira leitura, com a data de hoje (RN-19, RN-36, RN-39). Por isso a publicação não
   * pode ser depois de hoje: a leitura ficaria antes dela (RN-40, UC-10 3d).
   */
  @Transactional
  public VideoDto aprovar(int id, LocalDate dataPublicacao, long views) {
    Video video = video(id);
    LocalDate hoje = LocalDate.now(relogio);
    Participacao participacao = video.participacao();
    Campanha campanha = participacao.campanha();
    // Na campanha encerrada pode haver participante removido, sem criador (RN-59).
    RegrasDaCampanha.validarMudancaNaCampanha(campanha.status(hoje));
    DatasDaCampanha datas = campanha.datas();
    RegrasDoVideo.validarAprovacaoDeVideo(
        video.status(),
        participacao.criador().status(),
        dataPublicacao,
        datas.inicio(),
        datas.fim());
    RegrasDaLeitura.validarLeitura(dataPublicacao, datas.fechamento(), List.of(), hoje, hoje);
    video.aprovar(dataPublicacao);
    leituras.save(new Leitura(video, hoje, views, Instant.now(relogio)));
    return VideoMapper.paraOperador(video);
  }

  /** Recusa o vídeo em análise, inclusive o de criador bloqueado (RN-37, UC-04 3a). */
  @Transactional
  public VideoDto recusar(int id, String motivo) {
    Video video = video(id);
    RegrasDaCampanha.validarMudancaNaCampanha(
        video.participacao().campanha().status(LocalDate.now(relogio)));
    RegrasDoVideo.validarRecusaDeVideo(video.status());
    video.recusar(motivo);
    return VideoMapper.paraOperador(video);
  }

  /** Desqualifica o vídeo aprovado, em campanha não encerrada (RN-28, RN-38). */
  @Transactional
  public VideoDto desqualificar(int id, String motivo) {
    Video video = video(id);
    RegrasDaCampanha.validarMudancaNaCampanha(
        video.participacao().campanha().status(LocalDate.now(relogio)));
    RegrasDoVideo.validarVideoAprovado(video.status());
    video.desqualificar(motivo);
    return VideoMapper.paraOperador(video);
  }

  private Video video(int id) {
    return videos
        .travar(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Vídeo não encontrado."));
  }

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }
}
