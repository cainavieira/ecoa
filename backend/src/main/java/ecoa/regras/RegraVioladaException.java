package ecoa.regras;

import java.util.List;

/** Recusa de regra, que o tratador global converte no formato único de erro. */
public final class RegraVioladaException extends RuntimeException {

  private final Recusa recusa;
  private final List<Integer> itens;

  public RegraVioladaException(Recusa recusa, String mensagem) {
    this(recusa, mensagem, List.of());
  }

  public RegraVioladaException(Recusa recusa, String mensagem, List<Integer> itens) {
    super(mensagem);
    this.recusa = recusa;
    this.itens = List.copyOf(itens);
  }

  public Recusa recusa() {
    return recusa;
  }

  /** Os ids do que causou a recusa, quando ela é sobre vários objetos. */
  public List<Integer> itens() {
    return itens;
  }
}
