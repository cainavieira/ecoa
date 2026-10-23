package ecoa.controller;

import ecoa.regras.Funcao;
import org.springframework.security.oauth2.jwt.Jwt;

/** O "sub" do token de acesso é o id da conta, e a claim "funcao" é a função (ADR-0008). */
final class ContaDoToken {

  private ContaDoToken() {}

  static int id(Jwt token) {
    return Integer.parseInt(token.getSubject());
  }

  static Funcao funcao(Jwt token) {
    return Funcao.valueOf(token.getClaimAsString("funcao"));
  }
}
