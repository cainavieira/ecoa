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
 * O mesmo vídeo aprovado duas vezes ao mesmo tempo, no duplo clique do operador: as duas aprovações
 * leem o vídeo em análise, e a segunda esbarraria na leitura do dia que a primeira gravou. Ela sai
 * como VIDEO_NAO_EM_ANALISE, e não como erro inesperado (RN-36, RN-40). Sem transação de teste,
 * porque as duas aprovações precisam ser transações de verdade.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
class AprovacaoDuplaTest {

  private final VideoService videos;
  private final JdbcClient jdbc;
  private final LocalDate hoje;
  private int campanha;

  AprovacaoDuplaTest(
      @Autowired VideoService videos, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.videos = videos;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @AfterEach
  void limpar() {
    // A participação leva junto o vídeo e as leituras dele.
    jdbc.sql("delete from participacao where xid_campanha = ?").param(campanha).update();
    jdbc.sql("delete from campanha where id_campanha = ?").param(campanha).update();
    jdbc.sql("delete from conta where email like '%@aprovacao-dupla.exemplo'").update();
  }

  @RepeatedTest(5)
  void aSegundaAprovacaoSaiComoVideoNaoEmAnalise() throws Exception {
    campanha = campanha();
    int video = videoEmAnalise(criador("fulana@aprovacao-dupla.exemplo"));
    CyclicBarrier largada = new CyclicBarrier(2);

    List<Throwable> recusas;
    ExecutorService dois = Executors.newFixedThreadPool(2);
    try {
      Future<Void> primeira = dois.submit(aprovacao(largada, video));
      Future<Void> segunda = dois.submit(aprovacao(largada, video));
      recusas = Stream.of(primeira, segunda).map(AprovacaoDuplaTest::recusa).toList();
    } finally {
      dois.shutdown();
    }

    assertThat(
            jdbc.sql("select count(*) from leitura where xid_video = ?")
                .param(video)
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
                        regra ->
                            assertThat(regra.recusa()).isEqualTo(Recusa.VIDEO_NAO_EM_ANALISE)));
  }

  private Callable<Void> aprovacao(CyclicBarrier largada, int video) {
    return () -> {
      largada.await();
      videos.aprovar(video, hoje.minusDays(1), 100);
      return null;
    };
  }

  /** A exceção que a aprovação lançou, ou nulo se ela passou. */
  private static Throwable recusa(Future<Void> aprovacao) {
    try {
      aprovacao.get();
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
        .params(hoje.minusDays(5), hoje.plusDays(10), hoje.plusDays(15))
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

  private int videoEmAnalise(int criador) {
    int participacao =
        jdbc.sql(
                "insert into participacao (xid_campanha, xid_criador, aceite_em)"
                    + " values (?, ?, now()) returning id_participacao")
            .params(campanha, criador)
            .query(Integer.class)
            .single();
    return jdbc.sql(
            "insert into video (xid_participacao, link, status, enviado_em)"
                + " values (?, 'https://www.tiktok.com/@fulana/video/333', 'em_analise', now())"
                + " returning id_video")
        .param(participacao)
        .query(Integer.class)
        .single();
  }
}
