package ecoa.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.WebUtils;

/**
 * O login é pelo Google, e o Spring Security cuida do state, do nonce, da troca do código e da
 * assinatura do id_token (ADR-0007, ADR-0008). Depois dele, o token de acesso vem do cookie, e a
 * função dentro dele decide quais rotas a pessoa alcança (ADR-0009). Cada rota declara a função com
 * {@code @PreAuthorize}.
 */
@Configuration
@EnableMethodSecurity
@ConditionalOnWebApplication
public class SegurancaConfig {

  /** As rotas de auth/ funcionam sem token de acesso (endpoints.md). */
  private static final RequestMatcher ROTAS_DE_AUTH =
      PathPatternRequestMatcher.pathPattern("/auth/**");

  private static final String GOOGLE = "google";

  @Bean
  SecurityFilterChain seguranca(
      HttpSecurity http,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver tratador,
      ClientRegistrationRepository registros,
      RetornoDoLogin retorno) {
    // A recusa de autenticação sai pelo tratador global, no formato único de erro.
    AuthenticationEntryPoint naoAutenticado =
        (pedido, resposta, excecao) -> tratador.resolveException(pedido, resposta, null, excecao);
    return http
        // Sem token contra CSRF: o cookie é SameSite=Lax, e nenhum GET muda dado (ADR-0008)
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            rotas -> rotas.requestMatchers(ROTAS_DE_AUTH).permitAll().anyRequest().authenticated())
        .oauth2Login(
            login ->
                login
                    // Sem a página de login que o Spring gera: entrar é só pelo Google
                    .loginPage("/auth/login")
                    .authorizationEndpoint(
                        ida -> ida.authorizationRequestResolver(loginPeloGoogle(registros)))
                    .redirectionEndpoint(volta -> volta.baseUri("/auth/callback"))
                    .userInfoEndpoint(usuario -> usuario.oidcUserService(soEmailVerificado()))
                    // Os tokens do Google ficam na sessão do login, que acaba no retorno
                    .authorizedClientRepository(new HttpSessionOAuth2AuthorizedClientRepository())
                    .successHandler(retorno)
                    .failureHandler(retorno))
        .oauth2ResourceServer(
            recurso ->
                recurso
                    .bearerTokenResolver(
                        pedido -> {
                          // Nas rotas de auth/, o cookie de acesso é ignorado: um token vencido
                          // ou de chave trocada não pode impedir a renovação nem o login.
                          if (ROTAS_DE_AUTH.matches(pedido)) {
                            return null;
                          }
                          Cookie cookie = WebUtils.getCookie(pedido, CookiesDaSessao.ACESSO);
                          return cookie == null ? null : cookie.getValue();
                        })
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDaFuncao()))
                    .authenticationEntryPoint(naoAutenticado))
        .exceptionHandling(erro -> erro.authenticationEntryPoint(naoAutenticado))
        .build();
  }

  /**
   * O cliente do Google, pedindo só identidade (RNF-22). O id e o segredo só são exigidos pela api,
   * como o ECOA_JWT_CHAVE.
   */
  @Bean
  ClientRegistrationRepository registroDoGoogle(
      @Value("${ECOA_GOOGLE_ID}") String id, @Value("${ECOA_GOOGLE_SEGREDO}") String segredo) {
    return new InMemoryClientRegistrationRepository(
        CommonOAuth2Provider.GOOGLE
            .getBuilder(GOOGLE)
            .clientId(id)
            .clientSecret(segredo)
            .scope("openid", "email", "profile")
            .redirectUri("{baseUrl}/auth/callback")
            .build());
  }

  @Bean
  JwtDecoder decodificadorDoToken(@Value("${ECOA_JWT_CHAVE}") String chave) {
    return NimbusJwtDecoder.withPublicKey(ChaveDoToken.publica(chave)).build();
  }

  @Bean
  JwtEncoder emissorDoToken(@Value("${ECOA_JWT_CHAVE}") String chave) {
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(ChaveDoToken.par(chave))));
  }

  /** O GET /auth/login leva direto ao Google, sem o nome do provedor no caminho. */
  private static OAuth2AuthorizationRequestResolver loginPeloGoogle(
      ClientRegistrationRepository registros) {
    DefaultOAuth2AuthorizationRequestResolver padrao =
        new DefaultOAuth2AuthorizationRequestResolver(registros, "/auth/login");
    RequestMatcher login = PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/auth/login");
    return new OAuth2AuthorizationRequestResolver() {
      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest pedido) {
        return login.matches(pedido) ? padrao.resolve(pedido, GOOGLE) : null;
      }

      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest pedido, String registro) {
        return padrao.resolve(pedido, registro);
      }
    };
  }

  /**
   * Os dados vêm do id_token, que é assinado, sem a chamada extra ao userinfo do Google. Só entra o
   * e-mail que o Google devolve como verificado (RN-01).
   */
  private static OAuth2UserService<OidcUserRequest, OidcUser> soEmailVerificado() {
    OidcUserService google = new OidcUserService();
    google.setRetrieveUserInfo(pedido -> false);
    return pedido -> {
      OidcUser usuario = google.loadUser(pedido);
      if (!Boolean.TRUE.equals(usuario.getEmailVerified())) {
        throw new OAuth2AuthenticationException(
            new OAuth2Error(RetornoDoLogin.EMAIL_NAO_VERIFICADO));
      }
      return usuario;
    };
  }

  /**
   * A função vem na claim "funcao" e vira o papel ROLE_CRIADOR, ROLE_OPERADOR ou ROLE_GRAVADORA.
   */
  private static JwtAuthenticationConverter conversorDaFuncao() {
    JwtGrantedAuthoritiesConverter funcao = new JwtGrantedAuthoritiesConverter();
    funcao.setAuthoritiesClaimName("funcao");
    funcao.setAuthorityPrefix("ROLE_");
    JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
    conversor.setJwtGrantedAuthoritiesConverter(funcao);
    return conversor;
  }
}
