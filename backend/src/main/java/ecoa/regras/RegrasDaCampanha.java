package ecoa.regras;

import java.time.LocalDate;
import java.util.List;

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
