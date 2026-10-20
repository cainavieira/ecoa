package ecoa.config;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.WebUtils;

/**
 * O token de acesso vem do cookie, e a função dentro dele decide quais rotas a pessoa alcança
 * (ADR-0008, ADR-0009). Cada rota declara a função com @PreAuthorize.
 */
@Configuration
@EnableMethodSecurity
@ConditionalOnWebApplication
public class SegurancaConfig {

  static final String COOKIE_DE_ACESSO = "acesso";

  @Bean
  SecurityFilterChain seguranca(
      HttpSecurity http, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver tratador) {
    // A recusa de autenticação sai pelo tratador global, no formato único de erro.
    AuthenticationEntryPoint naoAutenticado =
        (pedido, resposta, excecao) -> tratador.resolveException(pedido, resposta, null, excecao);
    return http
        // Sem token contra CSRF: o cookie é SameSite=Lax, e nenhum GET muda dado (ADR-0008)
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(rotas -> rotas.anyRequest().authenticated())
        .oauth2ResourceServer(
            recurso ->
                recurso
                    .bearerTokenResolver(
                        pedido -> {
                          Cookie cookie = WebUtils.getCookie(pedido, COOKIE_DE_ACESSO);
                          return cookie == null ? null : cookie.getValue();
                        })
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDaFuncao()))
                    .authenticationEntryPoint(naoAutenticado))
        .exceptionHandling(erro -> erro.authenticationEntryPoint(naoAutenticado))
        .build();
  }

  @Bean
  JwtDecoder decodificadorDoToken(@Value("${ECOA_JWT_CHAVE}") String chave) {
    return NimbusJwtDecoder.withPublicKey(ChaveDoToken.publica(chave)).build();
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
