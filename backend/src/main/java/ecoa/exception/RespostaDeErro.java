package ecoa.exception;

import java.util.List;

/**
 * O formato único de erro (convencoes-backend.md). O campo e a regra ficam nulos quando a recusa
 * não é de um campo, ou não vem de uma regra.
 */
public record RespostaDeErro(Erro erro) {

  public record Erro(
      String codigo, String campo, String mensagem, String regra, List<Integer> itens) {}
}
