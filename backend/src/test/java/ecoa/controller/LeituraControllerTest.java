package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.ChaveDeTeste;
import ecoa.TestcontainersConfiguration;
import ecoa.regras.Funcao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** A planilha e o histórico de leituras, e a gravação e a exclusão (UC-11, RF-32, RF-33). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class LeituraControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;
  private final LocalDate hoje;
  private int videos;

  LeituraControllerTest(
      @Autowired MockMvc mvc,
      @Autowired JdbcClient jdbc,
      @Autowired EntityManager entidades,
      @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
    this.hoje = LocalDate.now(relogio);
  }

  /** As quatro rotas, na campanha ou no vídeo do id, com corpo válido. */
  static Stream<IntFunction<MockHttpServletRequestBuilder>> rotas() {
    return Stream.of(
        campanha -> get("/campanhas/{id}/leituras", campanha),
        video -> get("/videos/{id}/leituras", video),
        video ->
            put("/videos/{id}/leituras/{data}", video, "2026-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"views\": 10}"),
        video -> delete("/videos/{id}/leituras/{data}", video, "2026-01-01"));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void criadorNaoAlcancaAsLeituras(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.CRIADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void gravadoraNaoAlcancaAsLeituras(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void campanhaOuVideoQueNaoExisteRespondeNaoEncontrado(
      IntFunction<MockHttpServletRequestBuilder> rota) throws Exception {
    mvc.perform(rota.apply(999_999).cookie(operador()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void planilhaTemSoOsVideosQueContamComONumeroAtualEOFechamento() throws Exception {
    // Fechamento ontem: a leitura de hoje é a de fechamento.
    int campanha = campanha(hoje.minusDays(20), hoje.minusDays(5), hoje.minusDays(1));
    int daFulana = participacao(campanha, criador("fulana@exemplo.com", "aprovada"));
    int lido = video(daFulana, "aprovado", hoje.minusDays(10));
    leitura(lido, hoje.minusDays(3), 100);
    leitura(lido, hoje, 300);
    int semLeitura = video(daFulana, "aprovado", hoje.minusDays(10));
    video(daFulana, "em_analise", null);
    video(daFulana, "desqualificado", hoje.minusDays(10));
    // O vídeo do criador bloqueado não conta, e não aparece (UC-11 1a).
    video(
        participacao(campanha, criador("beltrana@exemplo.com", "bloqueada")),
        "aprovado",
        hoje.minusDays(10));
    int daCiclana =
        video(
            participacao(campanha, criador("ciclana@exemplo.com", "aprovada")),
            "aprovado",
            hoje.minusDays(10));
    leitura(daCiclana, hoje.minusDays(3), 50);
    video(
        participacao(aberta(), criador("deltrana@exemplo.com", "aprovada")),
        "aprovado",
        hoje.minusDays(5));

    mvc.perform(get("/campanhas/{id}/leituras", campanha).cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].idVideo").value(contains(daCiclana, lido, semLeitura)))
        .andExpect(jsonPath("$[*].usuarioTiktok").value(contains("ciclana", "fulana", "fulana")))
        .andExpect(jsonPath("$[*].temLeituraDeFechamento").value(contains(false, true, false)))
        .andExpect(jsonPath("$[1].numeroAtual.views").value(300))
        .andExpect(jsonPath("$[1].numeroAtual.data").value(hoje.toString()))
        .andExpect(jsonPath("$[2].numeroAtual").value(nullValue()));
  }

  @Test
  void historicoMarcaALeituraDeFechamento() throws Exception {
    int campanha = campanha(hoje.minusDays(20), hoje.minusDays(5), hoje.minusDays(1));
    int video = aprovado(campanha);
    leitura(video, hoje, 300);
    leitura(video, hoje.minusDays(3), 100);

    mvc.perform(get("/videos/{id}/leituras", video).cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[*].data").value(contains(hoje.minusDays(3).toString(), hoje.toString())))
        .andExpect(jsonPath("$[*].deFechamento").value(contains(false, true)));
  }

  @Test
  void gravaALeituraDaData() throws Exception {
    int video = aprovado(aberta());

    gravar(video, hoje.minusDays(2), "500")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.views").value(500))
        .andExpect(jsonPath("$.deFechamento").value(false));

    entidades.flush();
    assertThat(leituras(video)).containsExactly(hoje.minusDays(2) + " 500");
  }

  @Test
  void gravarNaMesmaDataSubstituiMesmoComMenosViews() throws Exception {
    // As redes removem views falsas (UC-11 2c e 2d).
    int video = aprovado(aberta());
    leitura(video, hoje.minusDays(2), 100);

    gravar(video, hoje.minusDays(2), "50").andExpect(status().isOk());

    entidades.flush();
    assertThat(leituras(video)).containsExactly(hoje.minusDays(2) + " 50");
  }

  @Test
  void dataFuturaEhRecusada() throws Exception {
    int video = aprovado(aberta());

    gravar(video, hoje.plusDays(1), "500")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("DATA_FUTURA"));
  }

  @Test
  void dataAntesDaPublicacaoEhRecusada() throws Exception {
    // O vídeo aprovado foi publicado há cinco dias.
    int video = aprovado(aberta());

    gravar(video, hoje.minusDays(6), "500")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("LEITURA_ANTES_DA_PUBLICACAO"));
  }

  @Test
  void segundaLeituraDeFechamentoEmOutraDataEhRecusada() throws Exception {
    int campanha = campanha(hoje.minusDays(20), hoje.minusDays(5), hoje.minusDays(2));
    int video = aprovado(campanha);
    leitura(video, hoje.minusDays(1), 300);

    gravar(video, hoje, "400")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("LEITURA_DEPOIS_DO_FECHAMENTO"));
  }

  @Test
  void leituraEmVideoNaoAprovadoEhRecusada() throws Exception {
    int participacao = participacao(aberta(), criador("fulana@exemplo.com", "aprovada"));
    int video = video(participacao, "desqualificado", hoje.minusDays(5));

    gravar(video, hoje, "500")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("VIDEO_NAO_APROVADO"));
  }

  @Test
  void viewsNegativasSaoRecusadas() throws Exception {
    int video = aprovado(aberta());

    gravar(video, hoje, "-1")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("VIEWS_INVALIDAS"))
        .andExpect(jsonPath("$.erro.campo").value("views"));
  }

  @Test
  void campanhaEncerradaNaoRecebeLeituraNemApaga() throws Exception {
    int campanha = campanha(hoje.minusDays(20), hoje.minusDays(10), hoje.minusDays(5));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(campanha)
        .update();
    int video = aprovado(campanha);
    leitura(video, hoje.minusDays(5), 300);

    gravar(video, hoje.minusDays(5), "400")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CAMPANHA_ENCERRADA"));
    apagar(video, hoje.minusDays(5))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CAMPANHA_ENCERRADA"));
  }

  @Test
  void apagaSoALeituraDaData() throws Exception {
    int video = aprovado(aberta());
    leitura(video, hoje.minusDays(2), 100);
    leitura(video, hoje.minusDays(1), 200);

    apagar(video, hoje.minusDays(1)).andExpect(status().isNoContent());

    entidades.flush();
    assertThat(leituras(video)).containsExactly(hoje.minusDays(2) + " 100");
  }

  @Test
  void apagarLeituraQueNaoExisteRespondeNaoEncontrado() throws Exception {
    int video = aprovado(aberta());

    apagar(video, hoje.minusDays(1))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  private ResultActions gravar(int video, LocalDate data, String views) throws Exception {
    return mvc.perform(
        put("/videos/{id}/leituras/{data}", video, data)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"views\": " + views + "}"));
  }

  private ResultActions apagar(int video, LocalDate data) throws Exception {
    return mvc.perform(delete("/videos/{id}/leituras/{data}", video, data).cookie(operador()));
  }

  private List<String> leituras(int video) {
    return jdbc.sql("select data || ' ' || views from leitura where xid_video = ? order by data")
        .param(video)
        .query(String.class)
        .list();
  }

  private int aberta() {
    return campanha(hoje.minusDays(10), hoje.plusDays(10), hoje.plusDays(15));
  }

  /** Vídeo aprovado, publicado há cinco dias, de um criador novo. */
  private int aprovado(int campanha) {
    int participacao =
        participacao(campanha, criador("criador" + (videos + 1) + "@exemplo.com", "aprovada"));
    return video(participacao, "aprovado", hoje.minusDays(5));
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

  /** O link é único entre os envios não recusados (RN-35), e cada vídeo leva um número. */
  private int video(int participacao, String status, LocalDate publicacao) {
    videos++;
    return jdbc.sql(
            """
            insert into video (xid_participacao, link, status, data_publicacao, motivo,
                enviado_em)
            values (?, ?, ?, ?::date, ?, now())
            returning id_video
            """)
        .params(
            participacao,
            "https://www.tiktok.com/@exemplo/video/" + videos,
            status,
            publicacao,
            "desqualificado".equals(status) ? "saiu do ar" : null)
        .query(Integer.class)
        .single();
  }

  private void leitura(int video, LocalDate data, long views) {
    jdbc.sql("insert into leitura (xid_video, data, views, registrada_em) values (?, ?, ?, now())")
        .params(video, data, views)
        .update();
  }

  /** A rota do operador não consulta a conta do token, só a função dentro dele. */
  private static Cookie operador() {
    return acesso(Funcao.OPERADOR, 1);
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
