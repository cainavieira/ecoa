package ecoa;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/** Índice único parcial e CHECK vivem só na migração, e o validate não os vê (ADR-0005). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@Transactional
class EsquemaTest {

  private static final String LINK = "https://www.tiktok.com/@fulano/video/1";

  private final JdbcClient jdbc;

  EsquemaTest(@Autowired JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Test
  void usuarioTiktokNaoSeRepeteEntreAprovadaEBloqueada() {
    criador("a@exemplo.com", "fulano", "aprovada");

    assertThatThrownBy(() -> criador("b@exemplo.com", "fulano", "bloqueada"))
        .isInstanceOf(DuplicateKeyException.class)
        .hasMessageContaining("criador_usuario_tiktok_key");
  }

  @Test
  void usuarioTiktokSeRepeteNaContaPendente() {
    criador("a@exemplo.com", "fulano", "aprovada");

    assertThatCode(() -> criador("b@exemplo.com", "fulano", "pendente")).doesNotThrowAnyException();
  }

  @Test
  void linkNaoSeRepeteEntreEnviosNaoRecusados() {
    int participacao = participacao();
    video(participacao, LINK, "aprovado");

    assertThatThrownBy(() -> video(participacao, LINK, "em_analise"))
        .isInstanceOf(DuplicateKeyException.class)
        .hasMessageContaining("video_link_key");
  }

  @Test
  void linkRecusadoPodeVoltar() {
    int participacao = participacao();
    video(participacao, LINK, "recusado");

    assertThatCode(() -> video(participacao, LINK, "em_analise")).doesNotThrowAnyException();
  }

  @Test
  void emailComMaiusculaNaoEntra() {
    assertThatThrownBy(() -> criador("Maria@exemplo.com", "fulano", "pendente"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("conta_email_check");
  }

  @Test
  void usuarioTiktokComArrobaNaoEntra() {
    assertThatThrownBy(() -> criador("a@exemplo.com", "@fulano", "pendente"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("criador_usuario_tiktok_normalizado_check");
  }

  // Os motivos e a data de publicação vão sempre preenchidos: só alguns status
  // os exigem, e nenhum os proíbe.

  private int criador(String email, String usuarioTiktok, String status) {
    int idConta =
        jdbc.sql(
                "insert into conta (email, funcao, criado_em) values (?, 'criador', now())"
                    + " returning id_conta")
            .param(email)
            .query(Integer.class)
            .single();
    jdbc.sql(
            """
            insert into criador (id_criador, usuario_tiktok, codigo_verificacao, status,
                motivo_bloqueio, aceite_termos_em)
            values (?, ?, ?, ?, 'motivo', now())
            """)
        .params(idConta, usuarioTiktok, "codigo-" + idConta, status)
        .update();
    return idConta;
  }

  private int participacao() {
    int idCampanha =
        jdbc.sql(
                """
                insert into campanha (musica, artista, regulamento, data_inicio, data_fim,
                    data_fechamento, criado_em)
                values ('Música', 'Artista', 'Regulamento', '2026-11-01', '2026-11-30',
                    '2026-12-02', now())
                returning id_campanha
                """)
            .query(Integer.class)
            .single();
    int idCriador = criador("c@exemplo.com", "fulano", "aprovada");
    return jdbc.sql(
            "insert into participacao (xid_campanha, xid_criador, aceite_em)"
                + " values (?, ?, now()) returning id_participacao")
        .params(idCampanha, idCriador)
        .query(Integer.class)
        .single();
  }

  private void video(int idParticipacao, String link, String status) {
    jdbc.sql(
            """
            insert into video (xid_participacao, link, status, data_publicacao, motivo,
                enviado_em)
            values (?, ?, ?, '2026-11-10', 'motivo', now())
            """)
        .params(idParticipacao, link, status)
        .update();
  }
}
