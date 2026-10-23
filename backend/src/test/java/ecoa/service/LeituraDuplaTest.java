package ecoa.service;

import static org.assertj.core.api.Assertions.assertThat;

import ecoa.TestcontainersConfiguration;
import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * A mesma linha da planilha gravada duas vezes ao mesmo tempo, no duplo clique: as duas gravações
 * leem o vídeo sem leitura na data, e a segunda esbarraria na chave da leitura. Ela substitui a
 * primeira, como qualquer gravação na mesma data (RN-40). Sem transação de teste, porque as duas
 * gravações precisam ser transações de verdade.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
class LeituraDuplaTest {

  private final LeituraService leituras;
  private final JdbcClient jdbc;
  private final LocalDate hoje;
  private int campanha;

  LeituraDuplaTest(
      @Autowired LeituraService leituras, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.leituras = leituras;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @AfterEach
  void limpar() {
    // A participação leva junto o vídeo e as leituras dele.
    jdbc.sql("delete from participacao where xid_campanha = ?").param(campanha).update();
    jdbc.sql("delete from campanha where id_campanha = ?").param(campanha).update();
    jdbc.sql("delete from conta where email like '%@leitura-dupla.exemplo'").update();
  }

  @RepeatedTest(5)
  void asDuasGravacoesPassamEFicaUmaLeitura() throws Exception {
    campanha = campanha();
    int video = videoAprovado(criador("fulana@leitura-dupla.exemplo"));
    CyclicBarrier largada = new CyclicBarrier(2);

    ExecutorService dois = Executors.newFixedThreadPool(2);
    try {
      Future<Void> primeira = dois.submit(gravacao(largada, video));
      Future<Void> segunda = dois.submit(gravacao(largada, video));
      primeira.get();
      segunda.get();
    } finally {
      dois.shutdown();
    }

    assertThat(
            jdbc.sql("select count(*) from leitura where xid_video = ?")
                .param(video)
                .query(Integer.class)
                .single())
        .isOne();
  }

  private Callable<Void> gravacao(CyclicBarrier largada, int video) {
    return () -> {
      largada.await();
      leituras.gravar(video, hoje, 100);
      return null;
    };
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

  private int videoAprovado(int criador) {
    int participacao =
        jdbc.sql(
                "insert into participacao (xid_campanha, xid_criador, aceite_em)"
                    + " values (?, ?, now()) returning id_participacao")
            .params(campanha, criador)
            .query(Integer.class)
            .single();
    return jdbc.sql(
            """
            insert into video (xid_participacao, link, status, data_publicacao, enviado_em)
            values (?, 'https://www.tiktok.com/@fulana/video/444', 'aprovado', ?, now())
            returning id_video
            """)
        .params(participacao, hoje.minusDays(3))
        .query(Integer.class)
        .single();
  }
}
