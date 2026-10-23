package ecoa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

/** As campanhas: lista, página, criação, edição e exclusão (UC-05, UC-06, UC-07). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class CampanhaControllerTest {

  private static final String UM_LINK = "{\"url\": \"https://www.tiktok.com/music/som-1\"}";

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;
  private final LocalDate hoje;

  CampanhaControllerTest(
      @Autowired MockMvc mvc,
      @Autowired JdbcClient jdbc,
      @Autowired EntityManager entidades,
      @Autowired Clock relogio) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
    this.hoje = LocalDate.now(relogio);
  }

  /** Corpo válido: com corpo inválido, a recusa de forma viria antes da guarda de função. */
  static Stream<MockHttpServletRequestBuilder> rotasDoOperador() {
    LocalDate inicio = LocalDate.of(2099, 1, 1);
    return Stream.of(
        post("/campanhas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo(inicio, inicio, inicio.plusDays(1), UM_LINK)));
  }

  @ParameterizedTest
  @MethodSource("rotasDoOperador")
  void criadorNaoAlcancaAsRotasDoOperador(MockHttpServletRequestBuilder rota) throws Exception {
    mvc.perform(rota.cookie(acesso(Funcao.CRIADOR, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @MethodSource("rotasDoOperador")
  void gravadoraNaoAlcancaAsRotasDoOperador(MockHttpServletRequestBuilder rota) throws Exception {
    mvc.perform(rota.cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro.codigo").value("SEM_PERMISSAO"));
  }

  @ParameterizedTest
  @EnumSource(Funcao.class)
  void listaComOStatusDerivadoDasDatas(Funcao funcao) throws Exception {
    int encerrada = campanha(hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(10));
    jdbc.sql("update campanha set encerrada_em = now() where id_campanha = ?")
        .param(encerrada)
        .update();
    campanha(hoje.minusDays(10), hoje.minusDays(1), hoje.plusDays(5));
    campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    campanha(hoje.plusDays(1), hoje.plusDays(10), hoje.plusDays(15));

    mvc.perform(get("/campanhas").cookie(acesso(funcao, 1)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[*].status")
                .value(contains("agendada", "aberta", "em_apuracao", "encerrada")));
  }

  @Test
  void marcaParaOCriadorSoAsCampanhasDeQueEleParticipa() throws Exception {
    int fulana = criador("fulana@exemplo.com");
    int beltrana = criador("beltrana@exemplo.com");
    int daFulana = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    int daBeltrana = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    participacao(daFulana, fulana);
    participacao(daBeltrana, beltrana);

    mvc.perform(get("/campanhas").cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id").value(contains(daBeltrana, daFulana)))
        .andExpect(jsonPath("$[*].participa").value(contains(false, true)));
  }

  @Test
  void mostraOsDadosOsLinksEOStatusDaCampanha() throws Exception {
    int campanha = campanha(hoje, hoje.plusDays(10), hoje.plusDays(15));
    link(campanha, "https://www.tiktok.com/music/som-1");
    link(campanha, "https://exemplo.com/regras");
    int fulana = criador("fulana@exemplo.com");
    participacao(campanha, fulana);

    mvc.perform(get("/campanhas/{id}", campanha).cookie(acesso(Funcao.CRIADOR, fulana)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.regulamento").value("Poste um vídeo com o som."))
        .andExpect(
            jsonPath("$.links[*].url")
                .value(
                    contains("https://www.tiktok.com/music/som-1", "https://exemplo.com/regras")))
        .andExpect(jsonPath("$.dataFechamento").value(hoje.plusDays(15).toString()))
        .andExpect(jsonPath("$.status").value("aberta"))
        .andExpect(jsonPath("$.participa").value(true));
  }

  @Test
  void campanhaQueNaoExisteRespondeNaoEncontrado() throws Exception {
    mvc.perform(get("/campanhas/{id}", 999_999).cookie(acesso(Funcao.GRAVADORA, 1)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void criaComOsLinksEJaAbertaQuandoOInicioPassou() throws Exception {
    // Início no passado é aceito (UC-05 2d), e link de referência pode ser http (RN-23).
    criar(
            corpo(
                hoje.minusDays(1),
                hoje.plusDays(10),
                hoje.plusDays(15),
                "{\"url\": \"http://exemplo.com/regras\"}, " + UM_LINK))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("aberta"))
        .andExpect(jsonPath("$.links", hasSize(2)));

    entidades.flush();
    assertThat(jdbc.sql("select count(*) from link_referencia").query(Integer.class).single())
        .isEqualTo(2);
  }

  @Test
  void criarSemLinkDeReferenciaEhRecusado() throws Exception {
    criar(corpo(hoje, hoje.plusDays(10), hoje.plusDays(15), ""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("LINK_REFERENCIA_AUSENTE"))
        .andExpect(jsonPath("$.erro.campo").value("links"))
        .andExpect(jsonPath("$.erro.mensagem").value("Informe ao menos um link de referência."));
  }

  @Test
  void linkQueNaoEhHttpNemHttpsEhRecusado() throws Exception {
    // O link vira endereço clicável na tela: javascript: executaria código.
    criar(corpo(hoje, hoje.plusDays(10), hoje.plusDays(15), "{\"url\": \"javascript:alert(1)\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("LINK_INVALIDO"))
        .andExpect(jsonPath("$.erro.campo").value("links[0].url"));
  }

  @Test
  void criarComFechamentoHojeEhRecusado() throws Exception {
    criar(corpo(hoje.minusDays(5), hoje, hoje, UM_LINK))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro.codigo").value("FECHAMENTO_NAO_FUTURO"));
  }

  @Test
  void criarComMusicaEmBrancoEhRecusado() throws Exception {
    criar(corpo(hoje, hoje.plusDays(10), hoje.plusDays(15), UM_LINK).replace("Som do Verão", "  "))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.erro.codigo").value("CORPO_INVALIDO"))
        .andExpect(jsonPath("$.erro.campo").value("musica"));
  }

  private ResultActions criar(String corpo) throws Exception {
    return mvc.perform(
        post("/campanhas")
            .cookie(acesso(Funcao.OPERADOR, 1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
  }

  private static String corpo(LocalDate inicio, LocalDate fim, LocalDate fechamento, String links) {
    return """
        {"musica": "Som do Verão", "artista": "Banda Exemplo",
         "regulamento": "Poste um vídeo com o som.", "links": [%s],
         "dataInicio": "%s", "dataFim": "%s", "dataFechamento": "%s"}
        """
        .formatted(links, inicio, fim, fechamento);
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

  private void link(int campanha, String url) {
    jdbc.sql("insert into link_referencia (xid_campanha, url) values (?, ?)")
        .params(campanha, url)
        .update();
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

  private void participacao(int campanha, int criador) {
    jdbc.sql("insert into participacao (xid_campanha, xid_criador, aceite_em) values (?, ?, now())")
        .params(campanha, criador)
        .update();
  }

  private static Cookie acesso(Funcao funcao, int conta) {
    return new Cookie(
        "acesso", ChaveDeTeste.token(funcao, conta, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }
}
