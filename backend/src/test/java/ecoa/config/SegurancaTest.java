package ecoa.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** A guarda de função, com token assinado de verdade num cookie (ADR-0008, ADR-0009). */
@Import({TestcontainersConfiguration.class, SegurancaTest.RotasDeTeste.class})
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
class SegurancaTest {

  @RestController
  static class RotasDeTeste {

    @GetMapping("/teste/operador")
    @PreAuthorize("hasRole('OPERADOR')")
    String operador() {
      return "ok";
    }
  }

  private final MockMvc mvc;

  SegurancaTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @Test
  void semCookieRespondeNaoAutenticado() throws Exception {
    mvc.perform(get("/teste/operador"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_AUTENTICADO"));
  }

  @Test
  void tokenDeOutraChaveRespondeNaoAutenticado() throws Exception {
    String token = ChaveDeTeste.tokenDeOutraChave(Funcao.OPERADOR, 1, daquiADezMinutos());
    mvc.perform(get("/teste/operador").cookie(acesso(token))).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenVencidoRespondeNaoAutenticado() throws Exception {
    // O Spring tolera um minuto de relógio adiantado; cinco já é vencido.
    String token =
        ChaveDeTeste.token(Funcao.OPERADOR, 1, Instant.now().minus(5, ChronoUnit.MINUTES));
    mvc.perform(get("/teste/operador").cookie(acesso(token))).andExpect(status().isUnauthorized());
  }

  @Test
  void funcaoErradaRespondeSemPermissao() throws Exception {
    String token = ChaveDeTeste.token(Funcao.CRIADOR, 1, daquiADezMinutos());
    mvc.perform(get("/teste/operador").cookie(acesso(token)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @Test
  void funcaoCertaPassa() throws Exception {
    String token = ChaveDeTeste.token(Funcao.OPERADOR, 1, daquiADezMinutos());
    mvc.perform(get("/teste/operador").cookie(acesso(token)))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));
  }

  private static Cookie acesso(String token) {
    return new Cookie("acesso", token);
  }

  private static Instant daquiADezMinutos() {
    return Instant.now().plus(10, ChronoUnit.MINUTES);
  }
}
