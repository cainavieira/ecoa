package ecoa.regras;

import java.util.List;
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

  /**
   * O criador corrige o @ da conta pendente ou recusada (RN-14); o da aprovada ou bloqueada, não
   * (RN-15).
   */
  public static void validarCorrecaoDoUsuarioTiktokPeloCriador(StatusCriador conta) {
    if (conta == StatusCriador.APROVADA || conta == StatusCriador.BLOQUEADA) {
      throw new RegraVioladaException(
          Recusa.USUARIO_TIKTOK_FIXO, "O @ da conta aprovada ou bloqueada só muda pelo operador.");
    }
  }

  /**
   * Só a conta pendente que já informou o @, e o @ sem outra conta aprovada ou bloqueada (RN-11,
   * RN-13).
   *
   * @param outrasContasComOUsuarioTiktok os status das outras contas com o mesmo @
   */
  public static void validarAprovacaoDaConta(
      StatusCriador conta,
      boolean temUsuarioTiktok,
      List<StatusCriador> outrasContasComOUsuarioTiktok) {
    validarPendenteComUsuarioTiktok(conta, temUsuarioTiktok);
    validarUsuarioTiktokLivre(outrasContasComOUsuarioTiktok);
  }

  /** Só a conta pendente que já informou o @ (RN-13). */
  public static void validarRecusaDaConta(StatusCriador conta, boolean temUsuarioTiktok) {
    validarPendenteComUsuarioTiktok(conta, temUsuarioTiktok);
  }

  /**
   * O operador corrige o @ da conta aprovada ou bloqueada, para um @ sem outra conta aprovada ou
   * bloqueada (RN-11, RN-15). A pendente ou recusada corrige pelo próprio criador.
   *
   * @param outrasContasComOUsuarioTiktok os status das outras contas com o @ novo
   */
  public static void validarCorrecaoDoUsuarioTiktokPeloOperador(
      StatusCriador conta, List<StatusCriador> outrasContasComOUsuarioTiktok) {
    if (conta != StatusCriador.APROVADA && conta != StatusCriador.BLOQUEADA) {
      throw new RegraVioladaException(
          Recusa.CONTA_NAO_APROVADA, "O operador só corrige o @ de conta aprovada ou bloqueada.");
    }
    validarUsuarioTiktokLivre(outrasContasComOUsuarioTiktok);
  }

  /** Só a conta aprovada é bloqueada (RN-17). */
  public static void validarBloqueioDaConta(StatusCriador conta) {
    if (conta != StatusCriador.APROVADA) {
      throw new RegraVioladaException(
          Recusa.CONTA_NAO_APROVADA, "Só a conta aprovada pode ser bloqueada.");
    }
  }

  /** Só a conta bloqueada é desbloqueada (RN-21). */
  public static void validarDesbloqueioDaConta(StatusCriador conta) {
    if (conta != StatusCriador.BLOQUEADA) {
      throw new RegraVioladaException(
          Recusa.CONTA_NAO_BLOQUEADA, "Só a conta bloqueada pode ser desbloqueada.");
    }
  }

  private static void validarPendenteComUsuarioTiktok(
      StatusCriador conta, boolean temUsuarioTiktok) {
    if (conta != StatusCriador.PENDENTE) {
      throw new RegraVioladaException(
          Recusa.CONTA_NAO_PENDENTE, "Só a conta pendente pode ser aprovada ou recusada.");
    }
    if (!temUsuarioTiktok) {
      throw new RegraVioladaException(
          Recusa.CONTA_SEM_USUARIO_TIKTOK, "A conta ainda não informou o @.");
    }
  }

  private static void validarUsuarioTiktokLivre(List<StatusCriador> outrasContasComOUsuarioTiktok) {
    if (outrasContasComOUsuarioTiktok.stream()
        .anyMatch(outra -> outra == StatusCriador.APROVADA || outra == StatusCriador.BLOQUEADA)) {
      throw new RegraVioladaException(
          Recusa.USUARIO_TIKTOK_EM_USO, "Este @ já é de outra conta aprovada ou bloqueada.");
    }
  }
}
