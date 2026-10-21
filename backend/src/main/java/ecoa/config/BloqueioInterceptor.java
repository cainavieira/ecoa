package ecoa.config;

import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.service.CriadorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * O criador bloqueado só alcança a própria conta: toda rota fora do GET /eu e das de auth/ recusa
 * com CONTA_BLOQUEADA (RN-18). Vale na requisição seguinte ao bloqueio.
 */
@Component
public class BloqueioInterceptor implements HandlerInterceptor {

  private final CriadorService criadores;

  public BloqueioInterceptor(CriadorService criadores) {
    this.criadores = criadores;
  }

  @Override
  public boolean preHandle(HttpServletRequest pedido, HttpServletResponse resposta, Object rota) {
    if (SecurityContextHolder.getContext().getAuthentication()
            instanceof JwtAuthenticationToken token
        && token.getAuthorities().stream()
            .anyMatch(papel -> "ROLE_CRIADOR".equals(papel.getAuthority()))) {
      // O "sub" do token é o id da conta (ADR-0008), que é também a chave da tabela criador.
      int idConta = Integer.parseInt(token.getToken().getSubject());
      if (criadores.estaBloqueado(idConta)) {
        throw new RegraVioladaException(Recusa.CONTA_BLOQUEADA, "Sua conta está bloqueada.");
      }
    }
    return true;
  }
}
