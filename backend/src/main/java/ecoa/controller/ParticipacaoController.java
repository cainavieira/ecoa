package ecoa.controller;

import ecoa.dto.AceiteDto;
import ecoa.dto.ParticipacaoDto;
import ecoa.service.Entrada;
import ecoa.service.ParticipacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A entrada na campanha, sempre da conta do token (UC-08, ADR-0009). */
@RestController
@RequestMapping("/campanhas/{id}/participacao")
public class ParticipacaoController {

  private final ParticipacaoService participacoes;

  public ParticipacaoController(ParticipacaoService participacoes) {
    this.participacoes = participacoes;
  }

  /** 201 para a participação nova; 200 para a que já existia (endpoints.md). */
  @PostMapping
  @PreAuthorize("hasRole('CRIADOR')")
  public ResponseEntity<ParticipacaoDto> entrar(
      @AuthenticationPrincipal Jwt token,
      @PathVariable int id,
      @Valid @RequestBody AceiteDto corpo) {
    Entrada entrada = participacoes.entrar(id, ContaDoToken.id(token));
    return ResponseEntity.status(entrada.nova() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(entrada.participacao());
  }
}
