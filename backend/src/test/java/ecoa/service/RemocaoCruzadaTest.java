package ecoa.service;

import static org.assertj.core.api.Assertions.assertThat;

import ecoa.TestcontainersConfiguration;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Dois operadores que removem um ao outro ao mesmo tempo (UC-18 4d): sem o bloqueio das linhas, as
 * duas transações contam dois operadores, e o sistema fica sem nenhum (RN-06). Sem transação de
 * teste, porque as duas remoções precisam ser transações de verdade.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
class RemocaoCruzadaTest {

  private final AcessoService acessos;
  private final JdbcClient jdbc;

  RemocaoCruzadaTest(@Autowired AcessoService acessos, @Autowired JdbcClient jdbc) {
    this.acessos = acessos;
    this.jdbc = jdbc;
  }

  @AfterEach
  void limpar() {
    jdbc.sql("delete from conta where email like '%@remocao-cruzada.exemplo'").update();
  }

  @RepeatedTest(5)
  void sobraUmOperador() throws Exception {
    int fulana = operador("fulana@remocao-cruzada.exemplo");
    int beltrana = operador("beltrana@remocao-cruzada.exemplo");
    CyclicBarrier largada = new CyclicBarrier(2);

    List<Throwable> recusas;
    ExecutorService dois = Executors.newFixedThreadPool(2);
    try {
      Future<Void> fulanaRemove = dois.submit(remocao(largada, fulana, beltrana));
      Future<Void> beltranaRemove = dois.submit(remocao(largada, beltrana, fulana));
      recusas = Stream.of(fulanaRemove, beltranaRemove).map(RemocaoCruzadaTest::recusa).toList();
    } finally {
      dois.shutdown();
    }

    assertThat(
            jdbc.sql("select count(*) from conta where email like '%@remocao-cruzada.exemplo'")
                .query(Integer.class)
                .single())
        .isOne();
    assertThat(recusas)
        .filteredOn(RegraVioladaException.class::isInstance)
        .singleElement()
        .satisfies(
            e ->
                assertThat(((RegraVioladaException) e).recusa()).isEqualTo(Recusa.ULTIMO_OPERADOR));
  }

  private Callable<Void> remocao(CyclicBarrier largada, int quemRemove, int removida) {
    return () -> {
      largada.await();
      acessos.remover(quemRemove, removida);
      return null;
    };
  }

  /** A exceção que a remoção lançou, ou nulo se ela passou. */
  private static Throwable recusa(Future<Void> remocao) {
    try {
      remocao.get();
      return null;
    } catch (ExecutionException e) {
      return e.getCause();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private int operador(String email) {
    return jdbc.sql(
            "insert into conta (email, funcao, criado_em) values (?, 'operador', now())"
                + " returning id_conta")
        .param(email)
        .query(Integer.class)
        .single();
  }
}
