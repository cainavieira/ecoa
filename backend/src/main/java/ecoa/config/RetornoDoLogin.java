package ecoa.config;

import ecoa.service.ContaService;
import ecoa.service.Sessao;
import ecoa.service.SessaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * O fim do retorno do Google, que sempre termina em redirecionamento (endpoints.md): no sucesso,
 * acha ou cria a conta, emite os dois cookies e vai para a raiz da aplicação; na falha, vai para a
 * tela de entrada com o motivo.
 */
@Component
@ConditionalOnWebApplication
public class RetornoDoLogin implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

  static final String EMAIL_NAO_VERIFICADO = "EMAIL_NAO_VERIFICADO";
  private static final String LOGIN_NAO_CONCLUIDO = "LOGIN_NAO_CONCLUIDO";

  private final ContaService contas;
  private final SessaoService sessoes;

  public RetornoDoLogin(ContaService contas, SessaoService sessoes) {
    this.contas = contas;
    this.sessoes = sessoes;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest pedido, HttpServletResponse resposta, Authentication autenticacao)
      throws IOException {
    OidcUser usuario = (OidcUser) autenticacao.getPrincipal();
    Sessao sessao = sessoes.abrir(contas.entrar(usuario.getEmail(), usuario.getFullName()));
    resposta.addHeader(
        HttpHeaders.SET_COOKIE, CookiesDaSessao.acesso(pedido, sessao.acesso()).toString());
    resposta.addHeader(
        HttpHeaders.SET_COOKIE, CookiesDaSessao.renovacao(pedido, sessao.renovacao()).toString());
    encerrarSessaoDoLogin(pedido);
    resposta.sendRedirect("/");
  }

  /** O cancelamento no Google e toda verificação que falha viram LOGIN_NAO_CONCLUIDO (UC-01 2a). */
  @Override
  public void onAuthenticationFailure(
      HttpServletRequest pedido, HttpServletResponse resposta, AuthenticationException excecao)
      throws IOException {
    String motivo =
        excecao instanceof OAuth2AuthenticationException oauth
                && EMAIL_NAO_VERIFICADO.equals(oauth.getError().getErrorCode())
            ? EMAIL_NAO_VERIFICADO
            : LOGIN_NAO_CONCLUIDO;
    encerrarSessaoDoLogin(pedido);
    resposta.sendRedirect("/entrar?motivo=" + motivo);
  }

  /** A sessão em memória só guarda o state durante o retorno do login (ADR-0008). */
  private static void encerrarSessaoDoLogin(HttpServletRequest pedido) {
    HttpSession sessao = pedido.getSession(false);
    if (sessao != null) {
      sessao.invalidate();
    }
  }
}
