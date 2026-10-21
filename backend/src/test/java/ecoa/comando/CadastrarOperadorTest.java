package ecoa.comando;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class CadastrarOperadorTest {

  @Container
  private static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

  private static JdbcClient jdbc;

  @BeforeAll
  static void migrar() {
    Migrar.executar(
        "--ECOA_DB_URL=" + POSTGRES.getJdbcUrl(),
        "--ECOA_DB_USUARIO=" + POSTGRES.getUsername(),
        "--ECOA_DB_SENHA=" + POSTGRES.getPassword());
    jdbc =
        JdbcClient.create(
            new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
  }

  @BeforeEach
  void limpar() {
    jdbc.sql("delete from conta").update();
  }

  @Test
  void cadastraOperadorComOEmailEmMinusculas() {
    int codigo = cadastrar("Fulana@Exemplo.com");

    assertThat(codigo).isZero();
    assertThat(jdbc.sql("select email, funcao from conta").query().singleRow())
        .containsEntry("email", "fulana@exemplo.com")
        .containsEntry("funcao", "operador");
  }

  @Test
  void emailComContaDeCriadorNaoMudaNada() {
    jdbc.sql(
            "insert into conta (email, funcao, criado_em)"
                + " values ('fulana@exemplo.com', 'criador', now())")
        .update();

    int codigo = cadastrar("Fulana@Exemplo.com");

    assertThat(codigo).isEqualTo(1);
    assertThat(jdbc.sql("select funcao from conta").query(String.class).list())
        .containsExactly("criador");
  }

  private static int cadastrar(String email) {
    return CadastrarOperador.executar(
        email,
        "--ECOA_DB_URL=" + POSTGRES.getJdbcUrl(),
        "--ECOA_DB_USUARIO=" + POSTGRES.getUsername(),
        "--ECOA_DB_SENHA=" + POSTGRES.getPassword());
  }
}
