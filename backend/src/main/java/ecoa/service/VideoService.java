package ecoa.service;

import ecoa.dto.MeuVideoDto;
import ecoa.entity.Campanha;
import ecoa.entity.Leitura;
import ecoa.entity.Participacao;
import ecoa.entity.Video;
import ecoa.mapper.VideoMapper;
import ecoa.regras.LeituraDoVideo;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
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
import java.util.stream.Collectors;
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
    if (dele.isEmpty()) {
      return List.of();
    }
    Map<Integer, List<LeituraDoVideo>> leiturasPorVideo =
        leituras.dosVideos(dele.stream().map(Video::idVideo).toList()).stream()
            .collect(
                Collectors.groupingBy(
                    Leitura::idVideo,
                    Collectors.mapping(
                        leitura -> new LeituraDoVideo(leitura.data(), leitura.views()),
                        Collectors.toList())));
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

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }
}
