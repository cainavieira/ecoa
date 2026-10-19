package ecoa.regras;

import java.time.LocalDate;

public final class RegrasDoGanhador {

  private RegrasDoGanhador() {}

  /**
   * O ganhador é participante e não está bloqueado, e o registro vale a partir do início da
   * campanha, inclusive depois do encerramento (RN-52).
   */
  public static void validarGanhador(
      StatusCriador criador, boolean participaDaCampanha, LocalDate inicio, LocalDate hoje) {
    if (hoje.isBefore(inicio)) {
      throw new RegraVioladaException(
          Recusa.GANHADOR_ANTES_DO_INICIO,
          "O ganhador só pode ser registrado a partir do início da campanha, "
              + Formato.data(inicio)
              + ".");
    }
    if (!participaDaCampanha) {
      throw new RegraVioladaException(
          Recusa.GANHADOR_NAO_PARTICIPANTE, "O ganhador precisa ser participante da campanha.");
    }
    if (criador == StatusCriador.BLOQUEADA) {
      throw new RegraVioladaException(
          Recusa.CRIADOR_BLOQUEADO,
          "O criador está bloqueado e não pode ser registrado como ganhador enquanto durar o"
              + " bloqueio.");
    }
  }

  /** A data de entrega do prêmio não é futura (RN-52). */
  public static void validarEntregaDoPremio(LocalDate entrega, LocalDate hoje) {
    if (entrega.isAfter(hoje)) {
      throw new RegraVioladaException(
          Recusa.DATA_FUTURA,
          "A data de entrega não pode ser depois de hoje, " + Formato.data(hoje) + ".");
    }
  }
}
