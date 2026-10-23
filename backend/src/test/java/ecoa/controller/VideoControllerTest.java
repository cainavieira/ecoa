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
import jakarta.persistence.EntityManager;
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
import org.junit.jupiter.params.provider.ValueSource;
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

/**
 * O envio de vídeo e os vídeos do próprio criador (UC-09, UC-13, RF-26, RF-31), e a conferência
 * pelo operador (UC-10, UC-12, RF-27 a RF-30).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class VideoControllerTest {

  private static final String LINK = "https://www.tiktok.com/@fulana/video/111";

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;
  private final LocalDate hoje;

  VideoControllerTest(
      @Autowired MockMvc mvc,
      @Autowired JdbcClient jdbc,
      @Autowired EntityManager entidades,
      @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
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

  /** As quatro rotas do operador, no vídeo do id, com corpo válido. */
  static Stream<IntFunction<MockHttpServletRequestBuilder>> rotasDoOperador() {
    return Stream.concat(Stream.of(video -> get("/videos")), transicoes());
  }

  /** As três rotas que mudam o status do vídeo. */
  static Stream<IntFunction<MockHttpServletRequestBuilder>> transicoes() {
    return Stream.of(
        video ->
            post("/videos/{id}/aprovacao", video)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataPublicacao\": \"2026-01-01\", \"views\": 10}"),
        video ->
            post("/videos/{id}/recusa", video)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"motivo\"}"),
        video ->
            post("/videos/{id}/desqualificacao", video)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"motivo\"}"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoOperador")
  void criadorNaoAlcancaAsRotasDoOperador(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.CRIADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoOperador")
  void gravadoraNaoAlcancaAsRotasDoOperador(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("transicoes")
  void videoQueNaoExisteRespondeNaoEncontrado(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(999_999).cookie(operador()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void filaDeConferenciaTemSoOsEmAnaliseDoMaisAntigoAoMaisNovo() throws Exception {
    int campanha = aberta();
    int daFulana = participacao(campanha, criador("fulana@exemplo.com"));
    int daBeltrana = participacao(aberta(), criador("beltrana@exemplo.com"));
    int primeiro = video(daBeltrana, LINK + "1", "em_analise", null, null);
    video(daFulana, LINK + "2", "aprovado", hoje, null);
    int segundo = video(daFulana, LINK + "3", "em_analise", null, null);

    mvc.perform(get("/videos").param("status", "em_analise").cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id").value(contains(primeiro, segundo)))
        .andExpect(jsonPath("$[*].usuarioTiktok").value(contains("beltrana", "fulana")))
        .andExpect(jsonPath("$[1].idCampanha").value(campanha))
        .andExpect(jsonPath("$[1].link").value(LINK + "3"));
  }

  @Test
  void aprovaComAPrimeiraLeituraNaDataDeHoje() throws Exception {
    int video = emAnalise(aberta(), criador("fulana@exemplo.com"));

    aprovar(video, hoje.minusDays(2), "1500")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("aprovado"))
        .andExpect(jsonPath("$.dataPublicacao").value(hoje.minusDays(2).toString()));

    entidades.flush();
    assertThat(
            jdbc.sql("select data || ' ' || views from leitura where xid_video = ?")
                .param(video)
                .query(String.class)
                .list())
        .containsExactly(hoje + " 1500");
  }

  @Test
  void publicacaoAntesDoInicioEhRecusada() throws Exception {
    int video = emAnalise(aberta(), criador("fulana@exemplo.com"));

    aprovar(video, hoje.minusDays(11), "1500")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("PUBLICACAO_FORA_DO_PRAZO"));
  }

  @Test
  void publicacaoDepoisDeHojeEhRecusada() throws Exception {
    // Dentro do prazo, mas a primeira leitura, de hoje, ficaria antes dela (UC-10 3d).
    int video = emAnalise(aberta(), criador("fulana@exemplo.com"));

    aprovar(video, hoje.plusDays(1), "1500")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("LEITURA_ANTES_DA_PUBLICACAO"));
  }

  @Test
  void videoDeCriadorBloqueadoNaoEhAprovado() throws Exception {
    int fulana = criador("fulana@exemplo.com");
    int video = emAnalise(aberta(), fulana);
    bloquear(fulana);

    aprovar(video, hoje.minusDays(2), "1500")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CRIADOR_BLOQUEADO"));
  }

  @Test
  void aprovarVideoJaAprovadoEhRecusado() throws Exception {
    int participacao = participacao(aberta(), criador("fulana@exemplo.com"));
    int video = video(participacao, LINK, "aprovado", hoje.minusDays(2), null);

    aprovar(video, hoje.minusDays(2), "1500")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("VIDEO_NAO_EM_ANALISE"));
  }

  @Test
  void videoDeParticipanteRemovidoEmCampanhaEncerradaNaoMuda() throws Exception {
    // O participante removido não tem conta, e o vídeo dele não tem link (RN-59).
    int campanha = campanha(hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(10));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(campanha)
        .update();
    int removido =
        jdbc.sql(
                "insert into participacao (xid_campanha, aceite_em) values (?, now())"
                    + " returning id_participacao")
            .param(campanha)
            .query(Integer.class)
            .single();
    int video = video(removido, null, "aprovado", hoje.minusDays(25), null);

    aprovar(video, hoje.minusDays(25), "1500")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CAMPANHA_ENCERRADA"));
  }

  @Test
  void viewsNegativasSaoRecusadas() throws Exception {
    int video = emAnalise(aberta(), criador("fulana@exemplo.com"));

    aprovar(video, hoje.minusDays(2), "-1")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("VIEWS_INVALIDAS"))
        .andExpect(jsonPath("$.erro.campo").value("views"))
        .andExpect(jsonPath("$.erro.mensagem").value("As views precisam ser zero ou mais."));
  }

  @Test
  void recusaOVideoDeCriadorBloqueadoComOMotivo() throws Exception {
    // O bloqueio impede a aprovação, não a recusa (UC-04 3a).
    int fulana = criador("fulana@exemplo.com");
    int video = emAnalise(aberta(), fulana);
    bloquear(fulana);

    comMotivo("recusa", video, "vídeo privado")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("recusado"));

    entidades.flush();
    assertThat(motivo(video)).isEqualTo("vídeo privado");
  }

  @Test
  void recusarVideoAprovadoEhRecusado() throws Exception {
    int participacao = participacao(aberta(), criador("fulana@exemplo.com"));
    int video = video(participacao, LINK, "aprovado", hoje.minusDays(2), null);

    comMotivo("recusa", video, "vídeo privado")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("VIDEO_NAO_EM_ANALISE"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"recusa", "desqualificacao"})
  void semMotivoNaoEhAceito(String acao) throws Exception {
    comMotivo(acao, 1, " ")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("MOTIVO_AUSENTE"));
  }

  @Test
  void desqualificaOVideoAprovadoComOMotivo() throws Exception {
    int participacao = participacao(aberta(), criador("fulana@exemplo.com"));
    int video = video(participacao, LINK, "aprovado", hoje.minusDays(2), null);

    comMotivo("desqualificacao", video, "saiu do ar")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("desqualificado"));

    entidades.flush();
    assertThat(motivo(video)).isEqualTo("saiu do ar");
  }

  @Test
  void desqualificarVideoEmAnaliseEhRecusado() throws Exception {
    int video = emAnalise(aberta(), criador("fulana@exemplo.com"));

    comMotivo("desqualificacao", video, "saiu do ar")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("VIDEO_NAO_APROVADO"));
  }

  @Test
  void desqualificarEmCampanhaEncerradaEhRecusado() throws Exception {
    int campanha = campanha(hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(10));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(campanha)
        .update();
    int participacao = participacao(campanha, criador("fulana@exemplo.com"));
    int video = video(participacao, LINK, "aprovado", hoje.minusDays(25), null);

    comMotivo("desqualificacao", video, "saiu do ar")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CAMPANHA_ENCERRADA"));
  }

  private ResultActions aprovar(int video, LocalDate publicacao, String views) throws Exception {
    return mvc.perform(
        post("/videos/{id}/aprovacao", video)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dataPublicacao\": \"%s\", \"views\": %s}".formatted(publicacao, views)));
  }

  private ResultActions comMotivo(String acao, int video, String motivo) throws Exception {
    return mvc.perform(
        post("/videos/{id}/" + acao, video)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"motivo\": \"" + motivo + "\"}"));
  }

  private int emAnalise(int campanha, int criador) {
    return video(participacao(campanha, criador), LINK, "em_analise", null, null);
  }

  private void bloquear(int criador) {
    jdbc.sql(
            "update criador set status = 'bloqueada', motivo_bloqueio = 'vídeo comprado'"
                + " where id_criador = ?")
        .param(criador)
        .update();
  }

  private String motivo(int video) {
    return jdbc.sql("select motivo from video where id_video = ?")
        .param(video)
        .query(String.class)
        .single();
  }

  /** A rota do operador não consulta a conta do token, só a função dentro dele. */
  private static Cookie operador() {
    return acesso(Funcao.OPERADOR, 1);
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
