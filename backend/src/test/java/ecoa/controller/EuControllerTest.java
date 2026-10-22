package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** A própria conta e o @ do criador (UC-02, RF-05, RF-10, RF-13), achados pelo token. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class EuControllerTest {

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;

  EuControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired EntityManager entidades) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
  }

  @Test
  void criadorVeAPropriaContaENaoADeOutro() throws Exception {
    int fulana = criador("fulana@exemplo.com", "recusada", "fulana", "motivo da recusa");
    criador("beltrana@exemplo.com", "pendente", null, null);

    mvc.perform(get("/eu").cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("fulana@exemplo.com"))
        .andExpect(jsonPath("$.funcao").value("criador"))
        .andExpect(jsonPath("$.criador.status").value("recusada"))
        .andExpect(jsonPath("$.criador.usuarioTiktok").value("fulana"))
        .andExpect(jsonPath("$.criador.codigoVerificacao").value("ECOA-" + fulana))
        .andExpect(jsonPath("$.criador.motivoRecusa").value("motivo da recusa"));
  }

  @Test
  void operadorNaoTemBlocoDeCriador() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");

    mvc.perform(get("/eu").cookie(acesso(Funcao.OPERADOR, operadora)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.funcao").value("operador"))
        .andExpect(jsonPath("$.criador").value(nullValue()));
  }

  @Test
  void informaOArrobaNormalizadoSemMexerNaContaDeOutro() throws Exception {
    // A outra conta nasce antes, para uma consulta sem o filtro do token pegar a errada.
    int beltrana = criador("beltrana@exemplo.com", "pendente", null, null);
    int fulana = criador("fulana@exemplo.com", "pendente", null, null);

    informar(fulana, " @Fulana ")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.criador.usuarioTiktok").value("fulana"))
        .andExpect(jsonPath("$.criador.status").value("pendente"));

    entidades.flush();
    assertThat(usuarioTiktok(beltrana)).isEmpty();
  }

  @Test
  void corrigirOArrobaDaContaRecusadaADevolveAPendente() throws Exception {
    int fulana = criador("fulana@exemplo.com", "recusada", "antigo", "o código não estava na bio");

    informar(fulana, "fulana")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.criador.status").value("pendente"))
        .andExpect(jsonPath("$.criador.motivoRecusa").value(nullValue()));
  }

  @Test
  void arrobaDeOutraContaEhAceito() throws Exception {
    criador("beltrana@exemplo.com", "aprovada", "fulana", null);
    int fulana = criador("fulana@exemplo.com", "pendente", null, null);

    informar(fulana, "fulana").andExpect(status().isOk());
  }

  @Test
  void contaAprovadaNaoCorrigeOArroba() throws Exception {
    int fulana = criador("fulana@exemplo.com", "aprovada", "fulana", null);

    informar(fulana, "outra")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("USUARIO_TIKTOK_FIXO"));
  }

  @Test
  void operadorNaoInformaArroba() throws Exception {
    int operadora = conta("operadora@exemplo.com", "operador");

    mvc.perform(
            put("/eu/usuario-tiktok")
                .cookie(acesso(Funcao.OPERADOR, operadora))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioTiktok\": \"fulana\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  private ResultActions informar(int conta, String usuarioTiktok) throws Exception {
    return mvc.perform(
        put("/eu/usuario-tiktok")
            .cookie(acesso(Funcao.CRIADOR, conta))
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

  private int criador(String email, String status, String usuarioTiktok, String motivoRecusa) {
    int conta = conta(email, "criador");
    jdbc.sql(
            """
            insert into criador (id_criador, usuario_tiktok, codigo_verificacao, status,
                motivo_recusa, aceite_termos_em)
            values (?, ?, ?, ?, ?, now())
            """)
        .params(conta, usuarioTiktok, "ECOA-" + conta, status, motivoRecusa)
        .update();
    return conta;
  }

  private Optional<String> usuarioTiktok(int conta) {
    return jdbc.sql("select usuario_tiktok from criador where id_criador = ?")
        .param(conta)
        .query(String.class)
        .optional();
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
