package ecoa.regras;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RegrasDaLeitura {

  private RegrasDaLeitura() {}

  /** A leitura de data mais recente, mesmo que outra tenha mais views (RN-39, RN-43). */
  public static Optional<LeituraDoVideo> numeroAtual(List<LeituraDoVideo> leituras) {
    return leituras.stream().max(Comparator.comparing(LeituraDoVideo::data));
  }

  /** A leitura com data igual ou posterior ao fechamento; o vídeo tem no máximo uma (RN-41). */
  public static Optional<LeituraDoVideo> leituraDeFechamento(
      List<LeituraDoVideo> leituras, LocalDate fechamento) {
    return leituras.stream().filter(leitura -> !leitura.data().isBefore(fechamento)).findFirst();
  }

  /**
   * Leitura nem futura nem anterior à publicação (RN-40) e, a partir do fechamento, só na data da
   * leitura de fechamento que o vídeo já tenha (RN-41). Pressupõe vídeo aprovado ({@link
   * RegrasDoVideo#validarVideoAprovado}).
   *
   * @param leituras as leituras que o vídeo já tem
   */
  public static void validarLeitura(
      LocalDate dataPublicacao,
      LocalDate fechamento,
      List<LeituraDoVideo> leituras,
      LocalDate data,
      LocalDate hoje) {
    if (data.isAfter(hoje)) {
      throw new RegraVioladaException(
          Recusa.DATA_FUTURA,
          "A data da leitura não pode ser depois de hoje, " + Formato.data(hoje) + ".");
    }
    if (data.isBefore(dataPublicacao)) {
      throw new RegraVioladaException(
          Recusa.LEITURA_ANTES_DA_PUBLICACAO,
          "A data da leitura não pode ser antes da publicação, "
              + Formato.data(dataPublicacao)
              + ".");
    }
    if (!data.isBefore(fechamento)) {
      Optional<LeituraDoVideo> deFechamento = leituraDeFechamento(leituras, fechamento);
      if (deFechamento.isPresent() && !deFechamento.get().data().equals(data)) {
        throw new RegraVioladaException(
            Recusa.LEITURA_DEPOIS_DO_FECHAMENTO,
            "O vídeo já tem leitura de fechamento em "
                + Formato.data(deFechamento.get().data())
                + ". Para trocar a data, apague essa leitura antes.");
      }
    }
  }
}
