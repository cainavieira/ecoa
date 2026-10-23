package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** O envio de vídeo e os vídeos do próprio criador (UC-09, UC-13, RF-26, RF-31). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class VideoControllerTest {

  private static final String LINK = "https://www.tiktok.com/@fulana/video/111";

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final LocalDate hoje;

  VideoControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  /** As duas rotas do criador, na campanha do id. */
  static Stream<IntFunction<MockHttpServletRequestBuilder>> rotasDoCriador() {
    return Stream.of(
        campanha ->
            post("/campanhas/{id}/videos", campanha)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"link\": \"" + LINK + "\"}"),
        campanha -> get("/campanhas/{id}/meus-videos", campanha));
  }

  @ParameterizedTest
  @MethodSource("rotasDoCriador")
  void operadorNaoAlcancaAsRotasDoCriador(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.OPERADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoCriador")
  void gravadoraNaoAlcancaAsRotasDoCriador(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoCriador")
  void campanhaQueNaoExisteRespondeNaoEncontrado(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    int fulana = criador("fulana@exemplo.com");

    mvc.perform(rota.apply(999_999).cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void participanteEnviaOLinkEOVideoFicaEmAnalise() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    int participacao = participacao(campanha, fulana);

    // O link colado com espaço em volta entra sem ele.
    enviar(campanha, fulana, " " + LINK + " ")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.link").value(LINK))
        .andExpect(jsonPath("$.status").value("em_analise"))
        .andExpect(jsonPath("$.leituras", hasSize(0)));

    assertThat(
            jdbc.sql("select xid_participacao from video where link = ?")
                .param(LINK)
                .query(Integer.class)
                .list())
        .containsExactly(participacao);
  }

  @Test
  void quemNaoParticipaNaoEnvia() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    participacao(campanha, criador("beltrana@exemplo.com"));

    enviar(campanha, fulana, LINK)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_PARTICIPANTE"));
  }

  @Test
  void linkQueNaoEhDoTiktokEhRecusado() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    participacao(campanha, fulana);

    enviar(campanha, fulana, "https://eviltiktok.com/@fulana/video/111")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("LINK_NAO_TIKTOK"));
  }

  @Test
  void linkJaEnviadoEmOutraCampanhaEhRecusado() throws Exception {
    // O mesmo link não entra de novo em campanha nenhuma (RN-35).
    int outra = aberta();
    video(participacao(outra, criador("beltrana@exemplo.com")), LINK, "aprovado", hoje, null);
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    participacao(campanha, fulana);

    enviar(campanha, fulana, LINK)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("LINK_JA_ENVIADO"));
  }

  @Test
  void linkRecusadoPodeSerEnviadoDeNovo() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    int participacao = participacao(campanha, fulana);
    video(participacao, LINK, "recusado", null, "vídeo privado");

    enviar(campanha, fulana, LINK).andExpect(status().isCreated());
  }

  @Test
  void campanhaEmApuracaoNaoRecebeVideo() throws Exception {
    int campanha = campanha(hoje.minusDays(10), hoje.minusDays(1), hoje.plusDays(5));
    int fulana = criador("fulana@exemplo.com");
    participacao(campanha, fulana);

    enviar(campanha, fulana, LINK)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CAMPANHA_NAO_ABERTA"));
  }

  @Test
  void meusVideosTemTodosOsStatusComMotivoLeiturasENumeroAtual() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    int participacao = participacao(campanha, fulana);
    int aprovado = video(participacao, LINK + "1", "aprovado", hoje.minusDays(5), null);
    // A leitura mais recente é o número atual, mesmo com menos views (RN-39, RN-43).
    leitura(aprovado, hoje.minusDays(1), 900);
    leitura(aprovado, hoje.minusDays(3), 1_000);
    int recusado = video(participacao, LINK + "2", "recusado", null, "vídeo privado");
    int emAnalise = video(participacao, LINK + "3", "em_analise", null, null);

    mvc.perform(get("/campanhas/{id}/meus-videos", campanha).cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id").value(contains(emAnalise, recusado, aprovado)))
        .andExpect(jsonPath("$[1].motivo").value("vídeo privado"))
        .andExpect(jsonPath("$[2].numeroAtual.views").value(900))
        .andExpect(jsonPath("$[2].numeroAtual.data").value(hoje.minusDays(1).toString()))
        .andExpect(jsonPath("$[2].leituras[*].views").value(contains(1_000, 900)))
        .andExpect(jsonPath("$[0].numeroAtual").value(nullValue()));
  }

  @Test
  void meusVideosNaoTemVideoDeOutroCriadorNemDeOutraCampanha() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com");
    int dela = video(participacao(campanha, fulana), LINK + "1", "em_analise", null, null);
    video(
        participacao(campanha, criador("beltrana@exemplo.com")),
        LINK + "2",
        "em_analise",
        null,
        null);
    video(participacao(aberta(), fulana), LINK + "3", "em_analise", null, null);

    mvc.perform(get("/campanhas/{id}/meus-videos", campanha).cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id").value(contains(dela)));
  }

  private ResultActions enviar(int campanha, int criador, String link) throws Exception {
    return mvc.perform(
        post("/campanhas/{id}/videos", campanha)
            .cookie(acesso(Funcao.CRIADOR, criador))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"link\": \"" + link + "\"}"));
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
            values (?, ?, ?, 'aprovada', now())
            """)
        .params(conta, email.substring(0, email.indexOf('@')), "ECOA-" + conta)
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

  private int video(
      int participacao, String link, String status, LocalDate publicacao, String motivo) {
    return jdbc.sql(
            """
            insert into video (xid_participacao, link, status, data_publicacao, motivo,
                enviado_em)
            values (?, ?, ?, ?::date, ?, now())
            returning id_video
            """)
        .params(participacao, link, status, publicacao, motivo)
        .query(Integer.class)
        .single();
  }

  private void leitura(int video, LocalDate data, long views) {
    jdbc.sql("insert into leitura (xid_video, data, views, registrada_em) values (?, ?, ?, now())")
        .params(video, data, views)
        .update();
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
