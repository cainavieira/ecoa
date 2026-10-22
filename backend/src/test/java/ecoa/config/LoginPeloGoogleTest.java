package ecoa.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ecoa.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * O retorno do Google contra um id_token fabricado (ADR-0013): um Google falso, dentro do teste,
 * troca o código pelo token e publica a chave que o assina. O state, o nonce e a assinatura passam
 * pelo caminho real do Spring Security.
 */
@Import({
  TestcontainersConfiguration.class,
  LoginPeloGoogleTest.GoogleFalso.class,
  LoginPeloGoogleTest.RotasDeTeste.class
})
@SpringBootTest(properties = "spring.flyway.enabled=true")
@AutoConfigureMockMvc
@Transactional
class LoginPeloGoogleTest {

  private static final String NOME = "Fulana de Tal";
  private static final RSAKey CHAVE_DO_GOOGLE = gerarChave();
  private static volatile String idTokenDaTroca = "";
  private static final HttpServer GOOGLE = subirGoogle();

  /** O registro de verdade, com a troca do código e a chave apontando para o Google falso. */
  @TestConfiguration(proxyBeanMethods = false)
  static class GoogleFalso {

    @Bean
    @Primary
    ClientRegistrationRepository registroDoGoogleFalso(
        ClientRegistrationRepository registroDoGoogle) {
      String endereco = "http://127.0.0.1:" + GOOGLE.getAddress().getPort();
      return new InMemoryClientRegistrationRepository(
          ClientRegistration.withClientRegistration(registroDoGoogle.findByRegistrationId("google"))
              .tokenUri(endereco + "/token")
              .jwkSetUri(endereco + "/jwks")
              .build());
    }
  }

  @RestController
  static class RotasDeTeste {

    @GetMapping("/teste/criador")
    @PreAuthorize("hasRole('CRIADOR')")
    String criador() {
      return "ok";
    }

    @GetMapping("/teste/operador")
    @PreAuthorize("hasRole('OPERADOR')")
    String operador() {
      return "ok";
    }
  }

  private final MockMvc mvc;
  private final JdbcClient jdbc;
  private final EntityManager entidades;
  private MockHttpSession sessaoDoLogin = new MockHttpSession();
  private String state = "";
  private String nonce = "";

  LoginPeloGoogleTest(
      @Autowired MockMvc mvc, @Autowired JdbcClient jdbc, @Autowired EntityManager entidades) {
    this.mvc = mvc;
    this.jdbc = jdbc;
    this.entidades = entidades;
  }

  @AfterAll
  static void derrubarGoogle() {
    GOOGLE.stop(0);
  }

  @Test
  void primeiroAcessoCriaCriadorPendenteEAbreASessao() throws Exception {
    irAoGoogle();

    String acesso =
        voltarDoGoogle(idToken(nonce, "Fulana@Exemplo.com", true))
            .andExpect(redirectedUrl("/"))
            .andExpect(cookie().exists("renovacao"))
            .andReturn()
            .getResponse()
            .getCookie("acesso")
            .getValue();

    mvc.perform(get("/teste/criador").cookie(new Cookie("acesso", acesso)))
        .andExpect(status().isOk());
    // O service participa da transação do teste, e o Hibernate só grava no fim dela.
    entidades.flush();
    Map<String, Object> criador =
        jdbc.sql(
                """
                select c.email, c.funcao, c.nome, cr.status, cr.codigo_verificacao,
                    cr.aceite_termos_em is not null as com_aceite
                from conta c join criador cr on cr.id_criador = c.id_conta
                """)
            .query()
            .singleRow();
    assertThat(criador)
        .containsEntry("email", "fulana@exemplo.com")
        .containsEntry("funcao", "criador")
        .containsEntry("nome", NOME)
        .containsEntry("status", "pendente")
        .containsEntry("com_aceite", true);
    assertThat((String) criador.get("codigo_verificacao")).matches("ECOA-[A-HJ-NP-Z2-9]{6}");
  }

  @Test
  void contaCadastradaPeloOperadorRecebeONomeEAFuncaoCadastrada() throws Exception {
    jdbc.sql(
            "insert into conta (email, funcao, criado_em)"
                + " values ('operadora@exemplo.com', 'operador', now())")
        .update();
    irAoGoogle();

    String acesso =
        voltarDoGoogle(idToken(nonce, "Operadora@Exemplo.com", true))
            .andExpect(redirectedUrl("/"))
            .andReturn()
            .getResponse()
            .getCookie("acesso")
            .getValue();

    mvc.perform(get("/teste/operador").cookie(new Cookie("acesso", acesso)))
        .andExpect(status().isOk());
    entidades.flush();
    assertThat(jdbc.sql("select nome from conta").query(String.class).list()).containsExactly(NOME);
    assertThat(jdbc.sql("select count(*) from criador").query(Integer.class).single()).isZero();
  }

  @Test
  void emailNaoVerificadoVoltaParaAEntradaSemCriarConta() throws Exception {
    irAoGoogle();

    voltarDoGoogle(idToken(nonce, "fulana@exemplo.com", false))
        .andExpect(redirectedUrl("/entrar?motivo=EMAIL_NAO_VERIFICADO"))
        .andExpect(cookie().doesNotExist("acesso"));

    assertThat(jdbc.sql("select count(*) from conta").query(Integer.class).single()).isZero();
  }

  @Test
  void cancelarNoGoogleVoltaComLoginNaoConcluido() throws Exception {
    irAoGoogle();

    mvc.perform(
            get("/auth/callback")
                .param("error", "access_denied")
                .param("state", state)
                .session(sessaoDoLogin))
        .andExpect(redirectedUrl("/entrar?motivo=LOGIN_NAO_CONCLUIDO"));
  }

  @Test
  void idTokenDeOutroLoginVoltaComLoginNaoConcluido() throws Exception {
    irAoGoogle();

    voltarDoGoogle(idToken("nonce-de-outro-login", "fulana@exemplo.com", true))
        .andExpect(redirectedUrl("/entrar?motivo=LOGIN_NAO_CONCLUIDO"));

    assertThat(jdbc.sql("select count(*) from conta").query(Integer.class).single()).isZero();
  }

  /** O GET /auth/login manda ao Google com o state e o nonce, guardados na sessão do login. */
  private void irAoGoogle() throws Exception {
    MvcResult ida =
        mvc.perform(get("/auth/login")).andExpect(status().is3xxRedirection()).andReturn();
    MultiValueMap<String, String> parametros =
        UriComponentsBuilder.fromUriString(ida.getResponse().getRedirectedUrl())
            .build()
            .getQueryParams();
    state = UriUtils.decode(parametros.getFirst("state"), StandardCharsets.UTF_8);
    nonce = UriUtils.decode(parametros.getFirst("nonce"), StandardCharsets.UTF_8);
    sessaoDoLogin = (MockHttpSession) ida.getRequest().getSession(false);
  }

  /** O navegador volta do Google com o código, e o Google falso o troca por este id_token. */
  private ResultActions voltarDoGoogle(String idToken) throws Exception {
    idTokenDaTroca = idToken;
    return mvc.perform(
        get("/auth/callback")
            .param("code", "codigo-de-teste")
            .param("state", state)
            .session(sessaoDoLogin));
  }

  private static String idToken(String nonce, String email, boolean verificado) {
    Instant agora = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("https://accounts.google.com")
            .subject("id-no-google")
            .audience(List.of(TestcontainersConfiguration.ID_DO_GOOGLE))
            .issuedAt(agora)
            .expiresAt(agora.plusSeconds(600))
            .claim("nonce", nonce)
            .claim("email", email)
            .claim("email_verified", verificado)
            .claim("name", NOME)
            .build();
    JwsHeader cabecalho =
        JwsHeader.with(SignatureAlgorithm.RS256).keyId(CHAVE_DO_GOOGLE.getKeyID()).build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(CHAVE_DO_GOOGLE)))
        .encode(JwtEncoderParameters.from(cabecalho, claims))
        .getTokenValue();
  }

  private static RSAKey gerarChave() {
    try {
      return new RSAKeyGenerator(2048).keyID("google-falso").generate();
    } catch (JOSEException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Responde à troca do código com o id_token da vez, e publica a metade pública da chave. */
  private static HttpServer subirGoogle() {
    try {
      HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      servidor.createContext(
          "/token",
          troca ->
              responder(
                  troca,
                  """
                  {"access_token": "acesso-do-google", "token_type": "Bearer",
                   "expires_in": 3600, "scope": "openid email profile", "id_token": "%s"}
                  """
                      .formatted(idTokenDaTroca)));
      servidor.createContext(
          "/jwks", troca -> responder(troca, new JWKSet(CHAVE_DO_GOOGLE.toPublicJWK()).toString()));
      servidor.start();
      return servidor;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void responder(HttpExchange troca, String json) throws IOException {
    troca.getRequestBody().readAllBytes();
    byte[] corpo = json.getBytes(StandardCharsets.UTF_8);
    troca.getResponseHeaders().add("Content-Type", "application/json");
    troca.sendResponseHeaders(200, corpo.length);
    try (OutputStream saida = troca.getResponseBody()) {
      saida.write(corpo);
    }
  }
}
