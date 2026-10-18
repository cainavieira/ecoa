package ecoa.regras;

import java.time.LocalDate;

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
}
