package ecoa.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.ChaveDeTeste;
import ecoa.TestcontainersConfiguration;
import ecoa.regras.Funcao;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** O resultado da campanha numa resposta só (UC-13, UC-16, RF-34, RF-38, RF-39). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class ResultadoControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final LocalDate hoje;
  private int videos;

  ResultadoControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @Test
  void rankingComEmpateTotalGraficoEContagens() throws Exception {
    int campanha = aberta();
    int daFulana = participacao(campanha, criador("fulana@exemplo.com", "aprovada"));
    int lido = video(daFulana, "aprovado");
    leitura(lido, hoje.minusDays(3), 100);
    leitura(lido, hoje.minusDays(1), 300);
    // Sem leitura, conta zero (RN-43).
    int semLeitura = video(daFulana, "aprovado");
    int daBeltrana =
        video(participacao(campanha, criador("beltrana@exemplo.com", "aprovada")), "aprovado");
    leitura(daBeltrana, hoje.minusDays(2), 300);
    int daCiclana =
        video(participacao(campanha, criador("ciclana@exemplo.com", "aprovada")), "aprovado");
    leitura(daCiclana, hoje.minusDays(2), 50);
    // Bloqueado sai do ranking (RN-19), e quem não tem vídeo que conta nem aparece (RN-45).
    leitura(
        video(participacao(campanha, criador("deltrana@exemplo.com", "bloqueada")), "aprovado"),
        hoje.minusDays(2),
        1_000);
    video(participacao(campanha, criador("ernesta@exemplo.com", "aprovada")), "em_analise");

    resultado(campanha, Funcao.GRAVADORA, 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rankingFinal").value(false))
        .andExpect(jsonPath("$.ranking[*].posicao").value(contains(1, 1, 3)))
        .andExpect(
            jsonPath("$.ranking[*].usuarioTiktok").value(contains("beltrana", "fulana", "ciclana")))
        .andExpect(jsonPath("$.ranking[*].soma").value(contains(300, 300, 50)))
        .andExpect(jsonPath("$.ranking[1].videos[*].idVideo").value(contains(lido, semLeitura)))
        .andExpect(jsonPath("$.ranking[1].videos[0].numeroAtual.views").value(300))
        .andExpect(jsonPath("$.ranking[1].videos[1].numeroAtual").value(nullValue()))
        .andExpect(jsonPath("$.totalHoje").value(650))
        .andExpect(jsonPath("$.criadoresNoRanking").value(3))
        .andExpect(jsonPath("$.videosQueContam").value(4))
        .andExpect(jsonPath("$.grafico[*].total").value(contains(100, 450, 650)))
        // Nenhum campo tem e-mail ou código de verificação (RN-57).
        .andExpect(content().string(not(containsString("exemplo.com\""))))
        .andExpect(content().string(not(containsString("ECOA-"))));
  }

  @Test
  void encerradaMostraORankingFinalGravadoMesmoComBloqueioDepois() throws Exception {
    int campanha = campanha(hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(10));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(campanha)
        .update();
    // Fulana foi bloqueada depois do encerramento e continua no ranking final (RN-48).
    int daFulana = participacao(campanha, criador("fulana@exemplo.com", "bloqueada"));
    rankingFinal(daFulana, 1, 500);
    leitura(video(daFulana, "aprovado"), hoje.minusDays(10), 500);
    int daBeltrana = participacao(campanha, criador("beltrana@exemplo.com", "aprovada"));
    rankingFinal(daBeltrana, 2, 200);
    leitura(video(daBeltrana, "aprovado"), hoje.minusDays(10), 200);
    // Ciclana estava bloqueada no encerramento e ficou fora; o desbloqueio não a traz de volta.
    int daCiclana = participacao(campanha, criador("ciclana@exemplo.com", "aprovada"));
    leitura(video(daCiclana, "aprovado"), hoje.minusDays(10), 1_000);
    // O participante removido fica, sem @ e com o vídeo sem link (RN-59).
    int removido = participacaoRemovida(campanha);
    rankingFinal(removido, 3, 100);
    int semLink = videoSemLink(removido);
    leitura(semLink, hoje.minusDays(10), 100);

    resultado(campanha, Funcao.OPERADOR, 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rankingFinal").value(true))
        .andExpect(jsonPath("$.ranking[*].posicao").value(contains(1, 2, 3)))
        .andExpect(jsonPath("$.ranking[*].soma").value(contains(500, 200, 100)))
        .andExpect(jsonPath("$.ranking[0].usuarioTiktok").value("fulana"))
        .andExpect(jsonPath("$.ranking[2].usuarioTiktok").value(nullValue()))
        .andExpect(jsonPath("$.ranking[2].videos[0].link").value(nullValue()))
        .andExpect(jsonPath("$.totalHoje").value(800))
        .andExpect(jsonPath("$.videosQueContam").value(3));
  }

  @Test
  void participanteVeOResultadoEQuemNaoParticipaNao() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    participacao(campanha, fulana);
    int beltrana = criador("beltrana@exemplo.com", "aprovada");

    resultado(campanha, Funcao.CRIADOR, fulana).andExpect(status().isOk());
    resultado(campanha, Funcao.CRIADOR, beltrana)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_PARTICIPANTE"));
  }

  @Test
  void ganhadoresVemComPremioEEntrega() throws Exception {
    int campanha = aberta();
    int daFulana = participacao(campanha, criador("fulana@exemplo.com", "aprovada"));
    int daBeltrana = participacao(campanha, criador("beltrana@exemplo.com", "aprovada"));
    ganhador(daBeltrana, "Camiseta", null);
    ganhador(daFulana, "Fone de ouvido", hoje.minusDays(1));

    resultado(campanha, Funcao.GRAVADORA, 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ganhadores[*].usuarioTiktok").value(contains("beltrana", "fulana")))
        .andExpect(jsonPath("$.ganhadores[*].premio").value(contains("Camiseta", "Fone de ouvido")))
        .andExpect(jsonPath("$.ganhadores[0].entregueEm").value(nullValue()))
        .andExpect(jsonPath("$.ganhadores[1].entregueEm").value(hoje.minusDays(1).toString()));
  }

  @Test
  void campanhaSemVideoQueContaTemNumerosZerados() throws Exception {
    // Agendada, ou sem vídeo que conta: zero e ranking vazio, em vez de erro (UC-16 2a).
    int campanha = campanha(hoje.plusDays(1), hoje.plusDays(10), hoje.plusDays(15));

    resultado(campanha, Funcao.GRAVADORA, 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalHoje").value(0))
        .andExpect(jsonPath("$.criadoresNoRanking").value(0))
        .andExpect(jsonPath("$.ranking", hasSize(0)))
        .andExpect(jsonPath("$.grafico", hasSize(0)));
  }

  @Test
  void campanhaQueNaoExisteRespondeNaoEncontrado() throws Exception {
    resultado(999_999, Funcao.GRAVADORA, 1)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  private ResultActions resultado(int campanha, Funcao funcao, int conta) throws Exception {
    return mvc.perform(get("/campanhas/{id}/resultado", campanha).cookie(acesso(funcao, conta)));
  }

  private int aberta() {
    return campanha(hoje.minusDays(10), hoje.plusDays(10), hoje.plusDays(15));
  }

  private int campanha(LocalDate inicio, LocalDate fim, LocalDate fechamento) {
    return jdbc.sql(
            """
            insert into campanha (musica, artista, regulamento, data_inicio, data_fim,
                data_fechamento, criado_em)
            values ('Som do Verão', 'Banda Exemplo', 'Poste um vídeo com o som.', ?, ?, ?, now())
            returning id_campanha
            """)
        .params(inicio, fim, fechamento)
        .query(Integer.class)
        .single();
  }

  private int criador(String email, String status) {
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
                motivo_bloqueio, aceite_termos_em)
            values (?, ?, ?, ?, 'motivo', now())
            """)
        .params(conta, email.substring(0, email.indexOf('@')), "ECOA-" + conta, status)
        .update();
    return conta;
  }

  private int participacao(int campanha, int criador) {
    return jdbc.sql(
            "insert into participacao (xid_campanha, xid_criador, aceite_em) values (?, ?, now())"
                + " returning id_participacao")
        .params(campanha, criador)
        .query(Integer.class)
        .single();
  }

  private int participacaoRemovida(int campanha) {
    return jdbc.sql(
            "insert into participacao (xid_campanha, aceite_em) values (?, now())"
                + " returning id_participacao")
        .param(campanha)
        .query(Integer.class)
        .single();
  }

  private void rankingFinal(int participacao, int posicao, long total) {
    jdbc.sql("update participacao set posicao_final = ?, total_final = ? where id_participacao = ?")
        .params(posicao, total, participacao)
        .update();
  }

  /** Publicado há vinte e cinco dias, com um link novo; o em análise sem publicação. */
  private int video(int participacao, String status) {
    videos++;
    return jdbc.sql(
            """
            insert into video (xid_participacao, link, status, data_publicacao, enviado_em)
            values (?, ?, ?, ?::date, now())
            returning id_video
            """)
        .params(
            participacao,
            "https://www.tiktok.com/@exemplo/video/" + videos,
            status,
            "em_analise".equals(status) ? null : hoje.minusDays(25))
        .query(Integer.class)
        .single();
  }

  private int videoSemLink(int participacao) {
    return jdbc.sql(
            """
            insert into video (xid_participacao, status, data_publicacao, enviado_em)
            values (?, 'aprovado', ?, now())
            returning id_video
            """)
        .params(participacao, hoje.minusDays(25))
        .query(Integer.class)
        .single();
  }

  private void leitura(int video, LocalDate data, long views) {
    jdbc.sql("insert into leitura (xid_video, data, views, registrada_em) values (?, ?, ?, now())")
        .params(video, data, views)
        .update();
  }

  private void ganhador(int participacao, String premio, LocalDate entregueEm) {
    jdbc.sql(
            "insert into ganhador (xid_participacao, premio, entregue_em, registrado_em)"
                + " values (?, ?, ?::date, now())")
        .params(participacao, premio, entregueEm)
        .update();
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
