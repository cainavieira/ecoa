package ecoa.comando;

import ecoa.ApiApplication;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.boot.SpringApplication;

/** Aplica as migrações e termina, sem subir a api (ADR-0006). */
public final class Migrar {

  private static final String[] PROPRIEDADES = {
    "--spring.flyway.enabled=true", "--spring.main.web-application-type=none"
  };

  private Migrar() {}

  public static void executar(String... argumentos) {
    String[] tudo =
        Stream.concat(Arrays.stream(PROPRIEDADES), Arrays.stream(argumentos))
            .toArray(String[]::new);
    SpringApplication.run(ApiApplication.class, tudo).close();
  }
}
