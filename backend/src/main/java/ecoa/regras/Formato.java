package ecoa.regras;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class Formato {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private Formato() {}

  /** Data como a pessoa lê (RN-64). */
  static String data(LocalDate data) {
    return DATA.format(data);
  }
}
