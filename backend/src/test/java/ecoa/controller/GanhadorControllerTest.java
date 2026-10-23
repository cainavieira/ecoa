package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

/** Os participantes, o registro de ganhador, a entrega e a exclusão (UC-15, RF-35 a RF-37). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class GanhadorControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;
  private final LocalDate hoje;

  GanhadorControllerTest(
      @Autowired MockMvc mvc,
      @Autowired JdbcClient jdbc,
      @Autowired EntityManager entidades,
      @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
    this.hoje = LocalDate.now(relogio);
  }

  /** As quatro rotas, na campanha ou no ganhador do id, com corpo válido. */
  static Stream<IntFunction<MockHttpServletRequestBuilder>> rotas() {
    return Stream.of(
        campanha -> get("/campanhas/{id}/participantes", campanha),
        campanha ->
            post("/campanhas/{id}/ganhadores", campanha)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idCriador\": 1, \"premio\": \"Camiseta\"}"),
        ganhador ->
            patch("/ganhadores/{id}", ganhador)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entregueEm\": \"2026-01-01\"}"),
        ganhador -> delete("/ganhadores/{id}", ganhador));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void criadorNaoAlcancaOsGanhadores(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.CRIADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void gravadoraNaoAlcancaOsGanhadores(IntFunction<MockHttpServletRequestBuilder> rota)
      throws Exception {
    mvc.perform(rota.apply(1).cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotas")
  void campanhaOuGanhadorQueNaoExisteRespondeNaoEncontrado(
      IntFunction<MockHttpServletRequestBuilder> rota) throws Exception {
    mvc.perform(rota.apply(999_999).cookie(operador()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void listaOsParticipantesComArrobaEStatusInclusiveSemVideoQueConta() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    participacao(campanha, fulana);
    int beltrana = criador("beltrana@exemplo.com", "bloqueada");
    participacao(campanha, beltrana);
    // O participante removido não tem conta, e não pode ganhar prêmio novo (RN-59).
    jdbc.sql("insert into participacao (xid_campanha, aceite_em) values (?, now())")
        .param(campanha)
        .update();
    participacao(aberta(), criador("ciclana@exemplo.com", "aprovada"));

    mvc.perform(get("/campanhas/{id}/participantes", campanha).cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].idCriador").value(contains(beltrana, fulana)))
        .andExpect(jsonPath("$[*].usuarioTiktok").value(contains("beltrana", "fulana")))
        .andExpect(jsonPath("$[*].status").value(contains("bloqueada", "aprovada")));
  }

  @Test
  void registraOGanhadorComOPremio() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    int participacao = participacao(campanha, fulana);

    registrar(campanha, fulana, "Fone de ouvido")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.usuarioTiktok").value("fulana"))
        .andExpect(jsonPath("$.premio").value("Fone de ouvido"))
        .andExpect(jsonPath("$.entregueEm").value(nullValue()));

    assertThat(
            jdbc.sql("select premio from ganhador where xid_participacao = ?")
                .param(participacao)
                .query(String.class)
                .list())
        .containsExactly("Fone de ouvido");
  }

  @Test
  void registraOutroPremioDoMesmoCriadorDepoisDoEncerramento() throws Exception {
    // Mais de um prêmio, e depois do encerramento (UC-15 1d e 1e).
    int campanha = campanha(hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(10));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(campanha)
        .update();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    ganhador(participacao(campanha, fulana), "Camiseta");

    registrar(campanha, fulana, "Fone de ouvido").andExpect(status().isCreated());
  }

  @Test
  void ganhadorAntesDoInicioEhRecusado() throws Exception {
    int campanha = campanha(hoje.plusDays(1), hoje.plusDays(10), hoje.plusDays(15));
    int fulana = criador("fulana@exemplo.com", "aprovada");
    participacao(campanha, fulana);

    registrar(campanha, fulana, "Camiseta")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("GANHADOR_ANTES_DO_INICIO"));
  }

  @Test
  void quemParticipaDeOutraCampanhaNaoGanhaNesta() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    participacao(aberta(), fulana);

    registrar(campanha, fulana, "Camiseta")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("GANHADOR_NAO_PARTICIPANTE"));
  }

  @Test
  void participanteBloqueadoNaoGanha() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "bloqueada");
    participacao(campanha, fulana);

    registrar(campanha, fulana, "Camiseta")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CRIADOR_BLOQUEADO"));
  }

  @Test
  void criadorQueNaoExisteRespondeNaoEncontrado() throws Exception {
    registrar(aberta(), 999_999, "Camiseta")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void premioEmBrancoEhRecusado() throws Exception {
    int campanha = aberta();
    int fulana = criador("fulana@exemplo.com", "aprovada");
    participacao(campanha, fulana);

    registrar(campanha, fulana, " ")
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.erro.codigo").value("CORPO_INVALIDO"))
        .andExpect(jsonPath("$.erro.campo").value("premio"));
  }

  @Test
  void informaADataDeEntrega() throws Exception {
    int campanha = aberta();
    int ganhador =
        ganhador(participacao(campanha, criador("fulana@exemplo.com", "aprovada")), "Camiseta");

    entregar(ganhador, hoje)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entregueEm").value(hoje.toString()));

    entidades.flush();
    assertThat(
            jdbc.sql("select entregue_em from ganhador where id_ganhador = ?")
                .param(ganhador)
                .query(LocalDate.class)
                .single())
        .isEqualTo(hoje);
  }

  @Test
  void entregaFuturaEhRecusada() throws Exception {
    int campanha = aberta();
    int ganhador =
        ganhador(participacao(campanha, criador("fulana@exemplo.com", "aprovada")), "Camiseta");

    entregar(ganhador, hoje.plusDays(1))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("DATA_FUTURA"));
  }

  @Test
  void apagaORegistro() throws Exception {
    int campanha = aberta();
    int ganhador =
        ganhador(participacao(campanha, criador("fulana@exemplo.com", "aprovada")), "Camiseta");

    mvc.perform(delete("/ganhadores/{id}", ganhador).cookie(operador()))
        .andExpect(status().isNoContent());

    entidades.flush();
    assertThat(jdbc.sql("select count(*) from ganhador").query(Integer.class).single()).isZero();
  }

  private ResultActions registrar(int campanha, int criador, String premio) throws Exception {
    return mvc.perform(
        post("/campanhas/{id}/ganhadores", campanha)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"idCriador\": %d, \"premio\": \"%s\"}".formatted(criador, premio)));
  }

  private ResultActions entregar(int ganhador, LocalDate data) throws Exception {
    return mvc.perform(
        patch("/ganhadores/{id}", ganhador)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entregueEm\": \"" + data + "\"}"));
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

  private int ganhador(int participacao, String premio) {
    return jdbc.sql(
            "insert into ganhador (xid_participacao, premio, registrado_em) values (?, ?, now())"
                + " returning id_ganhador")
        .params(participacao, premio)
        .query(Integer.class)
        .single();
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
