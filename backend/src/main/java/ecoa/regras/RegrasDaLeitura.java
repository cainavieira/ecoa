package ecoa.regras;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RegrasDaLeitura {

  private RegrasDaLeitura() {}

  /** A leitura de data mais recente, mesmo que outra tenha mais views (RN-39, RN-43). */
  public static Optional<LeituraDoVideo> numeroAtual(List<LeituraDoVideo> leituras) {
    return leituras.stream().max(Comparator.comparing(LeituraDoVideo::data));
  }
}
