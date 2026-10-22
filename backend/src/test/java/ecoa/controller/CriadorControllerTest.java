package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.ChaveDeTeste;
import ecoa.TestcontainersConfiguration;
import ecoa.regras.Funcao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

/** A verificação das contas de criador pelo operador (UC-03, RF-11, RF-12, RF-14, RF-15). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class CriadorControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;

  CriadorControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired EntityManager entidades) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
  }

  static Stream<MockHttpServletRequestBuilder> rotasDoOperador() {
    return Stream.of(
        get("/criadores"),
        post("/criadores/1/aprovacao"),
        post("/criadores/1/recusa")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"motivo\": \"motivo\"}"),
        put("/criadores/1/usuario-tiktok")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuarioTiktok\": \"fulana\"}"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoOperador")
  void criadorNaoAlcancaAsRotasDoOperador(MockHttpServletRequestBuilder rota) throws Exception {
    mvc.perform(rota.cookie(acesso(Funcao.CRIADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @Test
  void filaDeVerificacaoTemSoPendentesComArrobaEOAvisoDeContaApagada() throws Exception {
    int fulana = criador("fulana@exemplo.com", "pendente", "fulana");
    criador("beltrana@exemplo.com", "pendente", null);
    criador("ciclana@exemplo.com", "aprovada", "ciclana");
    // 22h de 01/10 em Brasília, já 02/10 em UTC: o aviso leva a data de Brasília (ADR-0014).
    jdbc.sql(
            "insert into conta_bloqueada_apagada (usuario_tiktok, apagada_em)"
                + " values ('fulana', '2026-10-02T01:00:00Z')")
        .update();

    mvc.perform(get("/criadores").param("status", "pendente").cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(fulana))
        .andExpect(jsonPath("$[0].email").value("fulana@exemplo.com"))
        .andExpect(jsonPath("$[0].codigoVerificacao").value("ECOA-" + fulana))
        .andExpect(jsonPath("$[0].contaBloqueadaApagadaEm").value("2026-10-01"));
  }

  @Test
  void listaDeContasTemTodosOsStatus() throws Exception {
    criador("fulana@exemplo.com", "pendente", "fulana");
    criador("beltrana@exemplo.com", "pendente", null);
    criador("ciclana@exemplo.com", "aprovada", "ciclana");

    mvc.perform(get("/criadores").cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));
  }

  @Test
  void aprovaAContaPendente() throws Exception {
    criador("ciclana@exemplo.com", "aprovada", "ciclana");
    int fulana = criador("fulana@exemplo.com", "pendente", "fulana");

    mvc.perform(post("/criadores/{id}/aprovacao", fulana).cookie(operador()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("aprovada"));
  }

  @Test
  void aprovacaoComArrobaDeOutraAprovadaEhRecusada() throws Exception {
    criador("ciclana@exemplo.com", "aprovada", "fulana");
    int fulana = criador("fulana@exemplo.com", "pendente", "fulana");

    mvc.perform(post("/criadores/{id}/aprovacao", fulana).cookie(operador()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("USUARIO_TIKTOK_EM_USO"));
  }

  @Test
  void recusaComOMotivo() throws Exception {
    int fulana = criador("fulana@exemplo.com", "pendente", "fulana");

    recusar(fulana, "{\"motivo\": \"o código não está na bio\"}")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("recusada"));

    entidades.flush();
    assertThat(
            jdbc.sql("select motivo_recusa from criador where id_criador = ?")
                .param(fulana)
                .query(String.class)
                .single())
        .isEqualTo("o código não está na bio");
  }

  @Test
  void recusaSemMotivoNaoEhAceita() throws Exception {
    int fulana = criador("fulana@exemplo.com", "pendente", "fulana");

    recusar(fulana, "{\"motivo\": \"  \"}")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("MOTIVO_AUSENTE"))
        .andExpect(jsonPath("$.erro.campo").value("motivo"))
        .andExpect(jsonPath("$.erro.mensagem").value("Informe o motivo."));
  }

  @Test
  void recusarContaAprovadaEhRecusado() throws Exception {
    int fulana = criador("fulana@exemplo.com", "aprovada", "fulana");

    recusar(fulana, "{\"motivo\": \"motivo\"}")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CONTA_NAO_PENDENTE"));
  }

  @Test
  void operadorCorrigeOArrobaDaContaAprovada() throws Exception {
    int fulana = criador("fulana@exemplo.com", "aprovada", "antigo");

    corrigir(fulana, " @Novo ")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuarioTiktok").value("novo"))
        .andExpect(jsonPath("$.status").value("aprovada"));
  }

  @Test
  void correcaoParaArrobaDeOutraAprovadaEhRecusada() throws Exception {
    criador("ciclana@exemplo.com", "aprovada", "fulana");
    int fulana = criador("fulana@exemplo.com", "aprovada", "antigo");

    corrigir(fulana, "fulana")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("USUARIO_TIKTOK_EM_USO"));
  }

  @Test
  void contaQueNaoEhDeCriadorRespondeNaoEncontrado() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");

    mvc.perform(post("/criadores/{id}/aprovacao", operadora).cookie(operador()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  private ResultActions recusar(int conta, String corpo) throws Exception {
    return mvc.perform(
        post("/criadores/{id}/recusa", conta)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
  }

  private ResultActions corrigir(int conta, String usuarioTiktok) throws Exception {
    return mvc.perform(
        put("/criadores/{id}/usuario-tiktok", conta)
            .cookie(operador())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"usuarioTiktok\": \"" + usuarioTiktok + "\"}"));
  }

  private int conta(String email, String funcao) {
    return jdbc.sql(
            "insert into conta (email, nome, funcao, criado_em) values (?, 'Nome', ?, now())"
                + " returning id_conta")
        .params(email, funcao)
        .query(Integer.class)
        .single();
  }

  private int criador(String email, String status, String usuarioTiktok) {
    int conta = conta(email, "criador");
    jdbc.sql(
            """
            insert into criador (id_criador, usuario_tiktok, codigo_verificacao, status,
                aceite_termos_em)
            values (?, ?, ?, ?, now())
            """)
        .params(conta, usuarioTiktok, "ECOA-" + conta, status)
        .update();
    return conta;
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
