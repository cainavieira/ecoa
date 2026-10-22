package ecoa.controller;

import ecoa.dto.EuDto;
import ecoa.dto.UsuarioTiktokDto;
import ecoa.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A própria conta, achada pelo token: nenhuma rota aqui recebe o id de outra (ADR-0009). */
@RestController
@RequestMapping("/eu")
public class EuController {

  private final ContaService contas;

  public EuController(ContaService contas) {
    this.contas = contas;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public EuDto eu(@AuthenticationPrincipal Jwt token) {
    return contas.eu(ContaDoToken.id(token));
  }

  @PutMapping("/usuario-tiktok")
  @PreAuthorize("hasRole('CRIADOR')")
  public EuDto informarUsuarioTiktok(
      @AuthenticationPrincipal Jwt token, @Valid @RequestBody UsuarioTiktokDto corpo) {
    return contas.informarUsuarioTiktok(ContaDoToken.id(token), corpo.usuarioTiktok());
  }
}
