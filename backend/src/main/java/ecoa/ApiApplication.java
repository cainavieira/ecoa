package ecoa;

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
    aplicacao().run(args);
  }

  /** A api e o modo migrar sobem por aqui, com a conferência das variáveis obrigatórias. */
  public static SpringApplication aplicacao() {
    SpringApplication aplicacao = new SpringApplication(ApiApplication.class);
    aplicacao.addListeners(new VariaveisObrigatorias());
    return aplicacao;
  }
}
