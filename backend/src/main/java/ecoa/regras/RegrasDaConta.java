package ecoa.regras;

import java.util.Locale;

public final class RegrasDaConta {

  private RegrasDaConta() {}

  /** O e-mail em minúsculas, para o mesmo e-mail não virar duas contas (RN-01). */
  public static String normalizarEmail(String email) {
    return email.toLowerCase(Locale.ROOT);
  }

  /**
   * O @ sem espaço nas pontas, sem o arroba da frente e em minúsculas (RN-11). Vazio, ou com espaço
   * no meio, é recusado.
   */
  public static String normalizarUsuarioTiktok(String usuario) {
    String semArroba = usuario.strip().replaceFirst("^@+", "");
    if (semArroba.isEmpty() || semArroba.chars().anyMatch(Character::isWhitespace)) {
      throw new RegraVioladaException(
          Recusa.USUARIO_TIKTOK_INVALIDO, "O @ não pode ficar vazio nem ter espaço no meio.");
    }
    return semArroba.toLowerCase(Locale.ROOT);
  }
}
