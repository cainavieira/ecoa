package ecoa.comando;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

class MigrarTest {

  @Test
  void aplicaTodasAsMigracoes() {
    try (var postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:18"))) {
      postgres.start();

      Migrar.executar(
          "--ECOA_DB_URL=" + postgres.getJdbcUrl(),
          "--ECOA_DB_USUARIO=" + postgres.getUsername(),
          "--ECOA_DB_SENHA=" + postgres.getPassword());

      var pendentes =
          Flyway.configure()
              .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
              .load()
              .info()
              .pending();
      assertThat(pendentes).isEmpty();
    }
  }
}
