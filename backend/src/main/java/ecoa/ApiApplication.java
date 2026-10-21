package ecoa;

import ecoa.comando.CadastrarOperador;
import ecoa.comando.Migrar;
import ecoa.config.VariaveisObrigatorias;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equals("migrar")) {
      Migrar.executar(Arrays.copyOfRange(args, 1, args.length));
      return;
    }
    if (args.length > 0 && args[0].equals("cadastrar-operador")) {
      // O código de saída diz a quem rodou se a conta foi criada (UC-20 2a)
      System.exit(CadastrarOperador.executar(Arrays.copyOfRange(args, 1, args.length)));
    }
    aplicacao().run(args);
  }

  /** A api e os modos do .jar sobem por aqui, com a conferência das variáveis obrigatórias. */
  public static SpringApplication aplicacao() {
    SpringApplication aplicacao = new SpringApplication(ApiApplication.class);
    aplicacao.addListeners(new VariaveisObrigatorias());
    return aplicacao;
  }
}
