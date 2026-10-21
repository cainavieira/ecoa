package ecoa.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.ChaveDeTeste;
import ecoa.TestcontainersConfiguration;
import ecoa.regras.Funcao;
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

/** O criador bloqueado só alcança a própria conta (RN-18, endpoints.md). */
@Import({TestcontainersConfiguration.class, BloqueioTest.RotasDeTeste.class})
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class BloqueioTest {

  @RestController
  static class RotasDeTeste {

    @GetMapping("/teste/criador")
    @PreAuthorize("hasRole('CRIADOR')")
    String criador() {
      return "ok";
    }

    @GetMapping("/eu")
    @PreAuthorize("isAuthenticated()")
    String eu() {
      return "ok";
    }
  }

  private final MockMvc mvc;
  private final JdbcClient jdbc;

  BloqueioTest(@Autowired MockMvc mvc, @Autowired JdbcClient jdbc) {
    this.mvc = mvc;
    this.jdbc = jdbc;
  }

  @Test
  void criadorBloqueadoRecebeContaBloqueada() throws Exception {
    int conta = criador("bloqueada");
    mvc.perform(get("/teste/criador").cookie(acesso(conta)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("CONTA_BLOQUEADA"));
  }

  @Test
  void criadorAprovadoPassa() throws Exception {
    int conta = criador("aprovada");
    mvc.perform(get("/teste/criador").cookie(acesso(conta))).andExpect(status().isOk());
  }

  @Test
  void criadorBloqueadoAindaVeAPropriaConta() throws Exception {
    int conta = criador("bloqueada");
    mvc.perform(get("/eu").cookie(acesso(conta))).andExpect(status().isOk());
  }

  private int criador(String status) {
    int conta =
        jdbc.sql(
                "insert into conta (email, funcao, criado_em) values (?, 'criador', now())"
                    + " returning id_conta")
            .param("fulano@exemplo.com")
            .query(Integer.class)
            .single();
    jdbc.sql(
            """
            insert into criador (id_criador, usuario_tiktok, codigo_verificacao, status,
                motivo_bloqueio, aceite_termos_em)
            values (?, 'fulano', ?, ?, 'motivo', now())
            """)
        .params(conta, "codigo-" + conta, status)
        .update();
    return conta;
  }

  private static Cookie acesso(int conta) {
    return new Cookie(
        "acesso",
        ChaveDeTeste.token(Funcao.CRIADOR, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
