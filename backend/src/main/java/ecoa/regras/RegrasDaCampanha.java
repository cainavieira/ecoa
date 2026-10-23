package ecoa.regras;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public final class RegrasDaCampanha {

  private RegrasDaCampanha() {}

  /** O encerramento vence as datas (RN-25). */
  public static StatusCampanha statusDaCampanha(
      LocalDate inicio, LocalDate fim, boolean encerrada, LocalDate hoje) {
    if (encerrada) {
      return StatusCampanha.ENCERRADA;
    }
    if (hoje.isBefore(inicio)) {
      return StatusCampanha.AGENDADA;
    }
    if (hoje.isAfter(fim)) {
      return StatusCampanha.EM_APURACAO;
    }
    return StatusCampanha.ABERTA;
  }

  /** Datas de campanha nova (RN-24, RN-26). */
  public static void validarDatasDaCampanha(DatasDaCampanha datas, LocalDate hoje) {
    validarOrdem(datas);
    validarFechamentoFuturo(datas.fechamento(), hoje);
  }

  /**
   * Datas de campanha que muda: o fechamento só precisa ser futuro se mudou (RN-26), e nenhum vídeo
   * aprovado fica fora do início e do fim (RN-27).
   */
  public static void validarDatasDaCampanha(
      DatasDaCampanha atuais,
      DatasDaCampanha novas,
      List<VideoAprovado> aprovados,
      LocalDate hoje) {
    validarOrdem(novas);
    if (!novas.fechamento().equals(atuais.fechamento())) {
      validarFechamentoFuturo(novas.fechamento(), hoje);
    }
    List<Integer> deFora =
        aprovados.stream()
            .filter(
                v ->
                    v.dataPublicacao().isBefore(novas.inicio())
                        || v.dataPublicacao().isAfter(novas.fim()))
            .map(VideoAprovado::idVideo)
            .toList();
    if (!deFora.isEmpty()) {
      throw new RegraVioladaException(
          Recusa.DATAS_EXCLUEM_VIDEO,
          "Há vídeo aprovado com a publicação fora das datas novas.",
          deFora);
    }
  }

  /**
   * Campanha encerrada não muda: dados, datas, vídeos e leituras. Só os ganhadores continuam
   * editáveis (RN-28).
   */
  public static void validarMudancaNaCampanha(StatusCampanha campanha) {
    if (campanha == StatusCampanha.ENCERRADA) {
      throw new RegraVioladaException(
          Recusa.CAMPANHA_ENCERRADA, "A campanha está encerrada e não muda mais.");
    }
  }

  /** Só a conta aprovada entra, e só em campanha agendada ou aberta (RN-16, RN-32). */
  public static void validarEntrada(StatusCriador conta, StatusCampanha campanha) {
    if (conta != StatusCriador.APROVADA) {
      throw new RegraVioladaException(
          Recusa.CONTA_NAO_APROVADA, "Só a conta aprovada entra em campanha.");
    }
    if (campanha != StatusCampanha.AGENDADA && campanha != StatusCampanha.ABERTA) {
      throw new RegraVioladaException(
          Recusa.ENTRADA_ENCERRADA, "A campanha não aceita mais participantes.");
    }
  }

  /**
   * A partir do fechamento, sem vídeo em análise, nem de criador bloqueado, e com leitura de
   * fechamento em todo vídeo que conta, que são os do ranking (RN-45, RN-50).
   */
  public static void validarEncerramento(
      List<ParticipanteDaCampanha> participantes, LocalDate fechamento, LocalDate hoje) {
    if (hoje.isBefore(fechamento)) {
      throw new RegraVioladaException(
          Recusa.ENCERRAMENTO_ANTES_DO_FECHAMENTO,
          "A campanha só pode ser encerrada a partir do fechamento, "
              + Formato.data(fechamento)
              + ".");
    }
    Stream<VideoDaCampanha> emAnalise =
        participantes.stream()
            .flatMap(participante -> participante.videos().stream())
            .filter(video -> video.status() == StatusVideo.EM_ANALISE);
    Stream<VideoDaCampanha> semLeituraDeFechamento =
        RegrasDoRanking.calcularRanking(participantes).stream()
            .flatMap(posicao -> posicao.videos().stream())
            .filter(
                video ->
                    RegrasDaLeitura.leituraDeFechamento(video.leituras(), fechamento).isEmpty());
    List<Integer> pendentes =
        Stream.concat(emAnalise, semLeituraDeFechamento).map(VideoDaCampanha::idVideo).toList();
    if (!pendentes.isEmpty()) {
      throw new RegraVioladaException(
          Recusa.ENCERRAMENTO_COM_PENDENCIA,
          "Há vídeo em análise, ou vídeo que conta sem leitura de fechamento.",
          pendentes);
    }
  }

  private static void validarOrdem(DatasDaCampanha datas) {
    if (datas.fim().isBefore(datas.inicio()) || datas.fechamento().isBefore(datas.fim())) {
      throw new RegraVioladaException(
          Recusa.DATAS_FORA_DE_ORDEM,
          "O início não pode ser depois do fim, nem o fim depois do fechamento.");
    }
  }

  private static void validarFechamentoFuturo(LocalDate fechamento, LocalDate hoje) {
    if (!fechamento.isAfter(hoje)) {
      throw new RegraVioladaException(
          Recusa.FECHAMENTO_NAO_FUTURO,
          "O fechamento precisa ser depois de hoje, " + Formato.data(hoje) + ".");
    }
  }
}
