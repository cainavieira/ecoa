package ecoa.comando;

import ecoa.ApiApplication;
import ecoa.regras.Funcao;
import ecoa.regras.RegraVioladaException;
import ecoa.service.AcessoService;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Cadastra a conta de operador e termina, sem subir a api (UC-20). A saída não repete o e-mail,
 * porque no contêiner ela é o log (RNF-16).
 */
public final class CadastrarOperador {

  private CadastrarOperador() {}

  /** O primeiro argumento é o e-mail, e os outros vão para o Spring. Devolve o código de saída. */
  public static int executar(String... argumentos) {
    if (argumentos.length == 0 || argumentos[0].isBlank()) {
      System.err.println("Uso: cadastrar-operador <e-mail>");
      return 1;
    }
    String[] propriedades =
        Stream.concat(
                Stream.of("--spring.main.web-application-type=none"),
                Arrays.stream(argumentos, 1, argumentos.length))
            .toArray(String[]::new);
    try (ConfigurableApplicationContext contexto = ApiApplication.aplicacao().run(propriedades)) {
      contexto.getBean(AcessoService.class).cadastrar(argumentos[0], Funcao.OPERADOR);
      System.out.println(
          "Conta de operador cadastrada. A pessoa entra pelo Google com esse e-mail.");
      return 0;
    } catch (RegraVioladaException e) {
      // Texto próprio, sem acento: a codificação do terminal varia (instrucao.md, seção 4)
      System.err.println("Esse e-mail tem conta. Nada mudou.");
      return 1;
    }
  }
}
