package ecoa.controller;

import org.springframework.security.oauth2.jwt.Jwt;

/** O "sub" do token de acesso é o id da conta (ADR-0008). */
final class ContaDoToken {

  private ContaDoToken() {}

  static int id(Jwt token) {
    return Integer.parseInt(token.getSubject());
  }
}
