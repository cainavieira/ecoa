package ecoa.controller;

import ecoa.dto.ResultadoDto;
import ecoa.service.ResultadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** O resultado da campanha: participante, gravadora e operador veem o mesmo (UC-13, UC-16). */
@RestController
public class ResultadoController {

  private final ResultadoService resultados;

  public ResultadoController(ResultadoService resultados) {
    this.resultados = resultados;
  }

  @GetMapping("/campanhas/{id}/resultado")
  @PreAuthorize("hasAnyRole('CRIADOR', 'OPERADOR', 'GRAVADORA')")
  public ResultadoDto resultado(@AuthenticationPrincipal Jwt token, @PathVariable int id) {
    return resultados.resultado(id, ContaDoToken.id(token), ContaDoToken.funcao(token));
  }
}
