package ecoa.service;

import static org.assertj.core.api.Assertions.assertThat;

import ecoa.TestcontainersConfiguration;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import java.time.Clock;
import java.time.LocalDate;
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
 * O mesmo link enviado duas vezes ao mesmo tempo, no duplo clique ou em duas abas: os dois envios
 * passam pela conferência antes de qualquer gravação, e o segundo esbarra no índice único do link
 * (RN-35). Ele sai como LINK_JA_ENVIADO, e não como erro inesperado. Sem transação de teste, porque
 * os dois envios precisam ser transações de verdade.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
class EnvioDuploTest {

  private static final String LINK = "https://www.tiktok.com/@fulana/video/222";

  private final VideoService videos;
  private final JdbcClient jdbc;
  private final LocalDate hoje;
  private int campanha;

  EnvioDuploTest(
      @Autowired VideoService videos, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.videos = videos;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @AfterEach
  void limpar() {
    jdbc.sql(
            "delete from video where xid_participacao in"
                + " (select id_participacao from participacao where xid_campanha = ?)")
        .param(campanha)
        .update();
    jdbc.sql("delete from participacao where xid_campanha = ?").param(campanha).update();
    jdbc.sql("delete from campanha where id_campanha = ?").param(campanha).update();
    jdbc.sql("delete from conta where email like '%@envio-duplo.exemplo'").update();
  }

  @RepeatedTest(5)
  void oSegundoEnvioSaiComoLinkJaEnviado() throws Exception {
    campanha = campanha();
    int fulana = criador("fulana@envio-duplo.exemplo");
    jdbc.sql("insert into participacao (xid_campanha, xid_criador, aceite_em) values (?, ?, now())")
        .params(campanha, fulana)
        .update();
    CyclicBarrier largada = new CyclicBarrier(2);

    List<Throwable> recusas;
    ExecutorService dois = Executors.newFixedThreadPool(2);
    try {
      Future<Void> primeiro = dois.submit(envio(largada, fulana));
      Future<Void> segundo = dois.submit(envio(largada, fulana));
      recusas = Stream.of(primeiro, segundo).map(EnvioDuploTest::recusa).toList();
    } finally {
      dois.shutdown();
    }

    assertThat(
            jdbc.sql("select count(*) from video where link = ?")
                .param(LINK)
                .query(Integer.class)
                .single())
        .isOne();
    assertThat(recusas)
        .filteredOn(recusa -> recusa != null)
        .singleElement()
        .satisfies(
            e ->
                assertThat(e)
                    .isInstanceOfSatisfying(
                        RegraVioladaException.class,
                        regra -> assertThat(regra.recusa()).isEqualTo(Recusa.LINK_JA_ENVIADO)));
  }

  private Callable<Void> envio(CyclicBarrier largada, int criador) {
    return () -> {
      largada.await();
      videos.enviar(campanha, criador, LINK);
      return null;
    };
  }

  /** A exceção que o envio lançou, ou nulo se ele passou. */
  private static Throwable recusa(Future<Void> envio) {
    try {
      envio.get();
      return null;
    } catch (ExecutionException e) {
      return e.getCause();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private int campanha() {
    return jdbc.sql(
            """
            insert into campanha (musica, artista, regulamento, data_inicio, data_fim,
                data_fechamento, criado_em)
            values ('Som do Verão', 'Banda Exemplo', 'Poste um vídeo com o som.', ?, ?, ?, now())
            returning id_campanha
            """)
        .params(hoje.minusDays(1), hoje.plusDays(10), hoje.plusDays(15))
        .query(Integer.class)
        .single();
  }

  private int criador(String email) {
    int conta =
        jdbc.sql(
                "insert into conta (email, nome, funcao, criado_em)"
                    + " values (?, 'Nome', 'criador', now()) returning id_conta")
            .param(email)
            .query(Integer.class)
            .single();
    jdbc.sql(
            """
            insert into criador (id_criador, usuario_tiktok, codigo_verificacao, status,
                aceite_termos_em)
            values (?, 'fulana', ?, 'aprovada', now())
            """)
        .params(conta, "ECOA-" + conta)
        .update();
    return conta;
  }
}
