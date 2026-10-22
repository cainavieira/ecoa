package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.ChaveDeTeste;
import ecoa.TestcontainersConfiguration;
import ecoa.regras.Funcao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** A renovação e o logout, com o token de renovação inserido direto no banco (ADR-0013). */
@Import({TestcontainersConfiguration.class, AutenticacaoControllerTest.RotasDeTeste.class})
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class AutenticacaoControllerTest {

  @RestController
  static class RotasDeTeste {

    @GetMapping("/teste/operador")
    @PreAuthorize("hasRole('OPERADOR')")
    String operador() {
      return "ok";
    }
  }

  private static final String TOKEN = "token-de-renovacao-de-teste";
  // SHA-256 do TOKEN, calculado fora do código testado
  private static final String HASH =
      "77e5f8b5c971d7797235199190dbf8eb24fb52bb4b574bcc21500b556e21293f";

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;

  AutenticacaoControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired EntityManager entidades) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
  }

  @Test
  void renovacaoValidaEmiteAcessoQuePassaEEmpurraAValidade() throws Exception {
    renovacaoQueVenceEm("1 day");

    String acesso =
        mvc.perform(post("/auth/renovacao").cookie(renovacao()))
            .andExpect(status().isNoContent())
            .andExpect(cookie().httpOnly("acesso", true))
            .andExpect(cookie().secure("acesso", true))
            .andExpect(cookie().sameSite("acesso", "Lax"))
            .andExpect(cookie().path("renovacao", "/auth"))
            .andExpect(cookie().maxAge("renovacao", 30 * 24 * 60 * 60))
            .andReturn()
            .getResponse()
            .getCookie("acesso")
            .getValue();

    mvc.perform(get("/teste/operador").cookie(new Cookie("acesso", acesso)))
        .andExpect(status().isOk());
    // O service participa da transação do teste, e o Hibernate só grava no fim dela.
    entidades.flush();
    assertThat(
            jdbc.sql("select expira_em > now() + interval '29 days' from token_renovacao")
                .query(Boolean.class)
                .single())
        .isTrue();
  }

  @Test
  void renovacaoVencidaRespondeNaoAutenticado() throws Exception {
    renovacaoQueVenceEm("-1 minute");

    mvc.perform(post("/auth/renovacao").cookie(renovacao()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_AUTENTICADO"));
  }

  @Test
  void semCookieRespondeNaoAutenticado() throws Exception {
    mvc.perform(post("/auth/renovacao"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_AUTENTICADO"));
  }

  @Test
  void acessoInvalidoNaoImpedeARenovacao() throws Exception {
    renovacaoQueVenceEm("1 day");
    String deOutraChave =
        ChaveDeTeste.tokenDeOutraChave(
            Funcao.OPERADOR, 1, Instant.now().plus(10, ChronoUnit.MINUTES));

    mvc.perform(post("/auth/renovacao").cookie(renovacao(), new Cookie("acesso", deOutraChave)))
        .andExpect(status().isNoContent());
  }

  @Test
  void logoutApagaARenovacaoEExpiraOsDoisCookies() throws Exception {
    renovacaoQueVenceEm("1 day");

    mvc.perform(post("/auth/logout").cookie(renovacao()))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge("acesso", 0))
        .andExpect(cookie().path("acesso", "/"))
        .andExpect(cookie().maxAge("renovacao", 0))
        .andExpect(cookie().path("renovacao", "/auth"));

    mvc.perform(post("/auth/renovacao").cookie(renovacao())).andExpect(status().isUnauthorized());
  }

  private void renovacaoQueVenceEm(String intervalo) {
    int conta =
        jdbc.sql(
                "insert into conta (email, funcao, criado_em)"
                    + " values ('operador@exemplo.com', 'operador', now()) returning id_conta")
            .query(Integer.class)
            .single();
    jdbc.sql(
            "insert into token_renovacao (xid_conta, hash, criado_em, expira_em)"
                + " values (?, ?, now(), now() + cast(? as interval))")
        .params(conta, HASH, intervalo)
        .update();
  }

  private static Cookie renovacao() {
    return new Cookie("renovacao", TOKEN);
  }
}
