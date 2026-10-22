package ecoa.controller;

import ecoa.config.CookiesDaSessao;
import ecoa.service.Sessao;
import ecoa.service.SessaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * As rotas de auth/, que funcionam sem token de acesso (endpoints.md). Só existe na api, como o
 * SessaoService.
 */
@RestController
@RequestMapping("/auth")
@ConditionalOnWebApplication
public class AutenticacaoController {

  private final SessaoService sessoes;

  public AutenticacaoController(SessaoService sessoes) {
    this.sessoes = sessoes;
  }

  /** Sem cookie, o valor chega vazio, e a renovação recusa como token desconhecido. */
  @PostMapping("/renovacao")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> renovar(
      @CookieValue(name = CookiesDaSessao.RENOVACAO, defaultValue = "") String renovacao,
      HttpServletRequest pedido) {
    Sessao sessao = sessoes.renovar(renovacao);
    return ResponseEntity.noContent()
        .header(
            HttpHeaders.SET_COOKIE,
            CookiesDaSessao.acesso(pedido, sessao.acesso()).toString(),
            CookiesDaSessao.renovacao(pedido, sessao.renovacao()).toString())
        .build();
  }
}
