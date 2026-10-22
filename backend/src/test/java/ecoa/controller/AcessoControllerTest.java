package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** O cadastro e a remoção de acessos de operador e de gravadora (UC-18, RN-03 a RN-06). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class AcessoControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;

  AcessoControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired EntityManager entidades) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
  }

  static Stream<MockHttpServletRequestBuilder> rotasDeAcesso() {
    return Stream.of(
        get("/acessos"),
        post("/acessos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\": \"fulana@exemplo.com\", \"funcao\": \"gravadora\"}"),
        delete("/acessos/1"));
  }

  @ParameterizedTest
  @MethodSource("rotasDeAcesso")
  void gravadoraNaoAlcancaAsRotasDeAcesso(MockHttpServletRequestBuilder rota) throws Exception {
    mvc.perform(rota.cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @Test
  void listaSoOperadorEGravadora() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");
    conta("gravadora@exemplo.com", "gravadora");
    conta("criadora@exemplo.com", "criador");

    mvc.perform(get("/acessos").cookie(acesso(Funcao.OPERADOR, operadora)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].funcao").value("operador"))
        .andExpect(jsonPath("$[1].funcao").value("gravadora"));
  }

  @Test
  void cadastraGravadoraComOEmailEmMinusculas() throws Exception {
    cadastrar("{\"email\": \"Fulana@Exemplo.com\", \"funcao\": \"gravadora\"}")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("fulana@exemplo.com"))
        .andExpect(jsonPath("$.funcao").value("gravadora"));
  }

  @Test
  void emailDeCriadorNaoViraAcesso() throws Exception {
    conta("fulana@exemplo.com", "criador");

    cadastrar("{\"email\": \"fulana@exemplo.com\", \"funcao\": \"operador\"}")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("EMAIL_JA_CADASTRADO"));
  }

  @Test
  void acessoNaoCriaContaDeCriador() throws Exception {
    cadastrar("{\"email\": \"fulana@exemplo.com\", \"funcao\": \"criador\"}")
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.erro.codigo").value("CORPO_INVALIDO"))
        .andExpect(jsonPath("$.erro.campo").value("funcao"));
  }

  @Test
  void removerAcessoApagaAConta() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");
    int gravadora = conta("gravadora@exemplo.com", "gravadora");

    remover(operadora, gravadora).andExpect(status().isNoContent());

    entidades.flush();
    assertThat(jdbc.sql("select count(*) from conta").query(Integer.class).single()).isOne();
  }

  @Test
  void operadorNaoRemoveAPropriaConta() throws Exception {
    conta("outra@exemplo.com", "operador");
    int operadora = conta("operadora@exemplo.com", "operador");

    remover(operadora, operadora)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("PROPRIA_CONTA"));
  }

  @Test
  void naoRemoveOUltimoOperador() throws Exception {
    // Quem remove teve a conta apagada, e o token dele ainda vale (ADR-0008).
    int operadora = conta("operadora@exemplo.com", "operador");

    remover(operadora + 1000, operadora)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("ULTIMO_OPERADOR"));
  }

  @Test
  void contaDeCriadorNaoEhAcesso() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");
    int criadora = conta("criadora@exemplo.com", "criador");

    remover(operadora, criadora)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  private ResultActions cadastrar(String corpo) throws Exception {
    return mvc.perform(
        post("/acessos")
            .cookie(acesso(Funcao.OPERADOR, 1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
  }

  private ResultActions remover(int quemRemove, int removida) throws Exception {
    return mvc.perform(
        delete("/acessos/{id}", removida).cookie(acesso(Funcao.OPERADOR, quemRemove)));
  }

  private int conta(String email, String funcao) {
    return jdbc.sql(
            "insert into conta (email, funcao, criado_em) values (?, ?, now()) returning id_conta")
        .params(email, funcao)
        .query(Integer.class)
        .single();
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
