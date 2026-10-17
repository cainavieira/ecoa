package ecoa;

import ecoa.comando.Migrar;
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
    SpringApplication.run(ApiApplication.class, args);
  }
}
