package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** A entrada do criador na campanha, com o aceite (UC-08, RF-25, RN-16, RN-31, RN-32). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class ParticipacaoControllerTest {

  private static final String ACEITE = "{\"aceite\": true}";

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final LocalDate hoje;

  ParticipacaoControllerTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.hoje = LocalDate.now(relogio);
  }

  @ParameterizedTest
  @EnumSource(value = Funcao.class, names = "CRIADOR", mode = EnumSource.Mode.EXCLUDE)
  void soOCriadorEntraEmCampanha(Funcao funcao) throws Exception {
    mvc.perform(
            post("/campanhas/1/participacao")
                .cookie(acesso(funcao, 1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACEITE))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @Test
  void criadorAprovadoEntraNaCampanhaAberta() throws Exception {
    int campanha = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    int fulana = criador("fulana@exemplo.com", "aprovada");

    entrar(campanha, fulana, ACEITE)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.idCampanha").value(campanha))
        .andExpect(jsonPath("$.aceiteEm").value(hoje.toString()));

    assertThat(
            jdbc.sql("select xid_criador from participacao where xid_campanha = ?")
                .param(campanha)
                .query(Integer.class)
                .list())
        .containsExactly(fulana);
  }

  @Test
  void quemJaParticipaRecebeAParticipacaoQueExisteMesmoComAEntradaFechada() throws Exception {
    // Em apuração, ninguém mais entra (RN-32), mas o segundo clique de quem já entrou não é recusa.
    int campanha = campanha(hoje.minusDays(10), hoje.minusDays(1), hoje.plusDays(5));
    int fulana = criador("fulana@exemplo.com", "aprovada");
    int participacao = participacao(campanha, fulana);

    entrar(campanha, fulana, ACEITE)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(participacao));
  }

  @Test
  void participacaoDeOutroCriadorNaoContaComoDele() throws Exception {
    int campanha = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    int fulana = criador("fulana@exemplo.com", "aprovada");
    int daBeltrana = participacao(campanha, criador("beltrana@exemplo.com", "aprovada"));

    entrar(campanha, fulana, ACEITE)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(not(daBeltrana)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"aceite\": false}", "{}"})
  void entrarSemOAceiteEhRecusado(String corpo) throws Exception {
    int campanha = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    int fulana = criador("fulana@exemplo.com", "aprovada");

    entrar(campanha, fulana, corpo)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("ACEITE_AUSENTE"))
        .andExpect(jsonPath("$.erro.campo").value("aceite"))
        .andExpect(
            jsonPath("$.erro.mensagem")
                .value(
                    "Para entrar, aceite o regulamento e a exibição do seu @ e das suas views."));
  }

  @Test
  void contaPendenteNaoEntra() throws Exception {
    int campanha = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    int fulana = criador("fulana@exemplo.com", "pendente");

    entrar(campanha, fulana, ACEITE)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("CONTA_NAO_APROVADA"));
  }

  @Test
  void campanhaEmApuracaoNaoRecebeQuemNaoParticipa() throws Exception {
    int campanha = campanha(hoje.minusDays(10), hoje.minusDays(1), hoje.plusDays(5));
    int fulana = criador("fulana@exemplo.com", "aprovada");

    entrar(campanha, fulana, ACEITE)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("ENTRADA_ENCERRADA"));
  }

  @Test
  void campanhaQueNaoExisteRespondeNaoEncontrado() throws Exception {
    int fulana = criador("fulana@exemplo.com", "aprovada");

    entrar(999_999, fulana, ACEITE)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  private ResultActions entrar(int campanha, int criador, String corpo) throws Exception {
    return mvc.perform(
        post("/campanhas/{id}/participacao", campanha)
            .cookie(acesso(Funcao.CRIADOR, criador))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
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
                aceite_termos_em)
            values (?, ?, ?, ?, now())
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

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
