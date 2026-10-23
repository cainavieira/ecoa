package ecoa.service;

import static org.assertj.core.api.Assertions.assertThat;

import ecoa.TestcontainersConfiguration;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
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
 * O duplo clique, ou duas abas (endpoints.md): as duas entradas passam pela conferência antes de
 * qualquer gravação, a segunda esbarra na chave única, e as duas recebem a mesma participação. Sem
 * transação de teste, porque as duas entradas precisam ser transações de verdade.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
class EntradaDuplaTest {

  private final ParticipacaoService participacoes;
  private final JdbcClient jdbc;
  private final LocalDate hoje;
  private int campanha;

  EntradaDuplaTest(
      @Autowired ParticipacaoService participacoes,
      @Autowired JdbcClient jdbc,
      @Autowired Clock relogio) {
    this.participacoes = participacoes;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @AfterEach
  void limpar() {
    jdbc.sql("delete from participacao where xid_campanha = ?").param(campanha).update();
    jdbc.sql("delete from campanha where id_campanha = ?").param(campanha).update();
    jdbc.sql("delete from conta where email like '%@entrada-dupla.exemplo'").update();
  }

  @RepeatedTest(5)
  void asDuasEntradasRecebemAMesmaParticipacao() throws Exception {
    campanha = campanha();
    int fulana = criador("fulana@entrada-dupla.exemplo");
    CyclicBarrier largada = new CyclicBarrier(2);

    List<Entrada> entradas;
    ExecutorService dois = Executors.newFixedThreadPool(2);
    try {
      Future<Entrada> primeira = dois.submit(entrada(largada, fulana));
      Future<Entrada> segunda = dois.submit(entrada(largada, fulana));
      entradas = List.of(primeira.get(), segunda.get());
    } finally {
      dois.shutdown();
    }

    assertThat(entradas)
        .extracting(entrada -> entrada.participacao().id())
        .containsOnly(entradas.getFirst().participacao().id());
    assertThat(entradas).filteredOn(Entrada::nova).hasSize(1);
  }

  private Callable<Entrada> entrada(CyclicBarrier largada, int criador) {
    return () -> {
      largada.await();
      return participacoes.entrar(campanha, criador);
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
        .params(hoje, hoje.plusDays(10), hoje.plusDays(15))
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
