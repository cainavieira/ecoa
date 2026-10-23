package ecoa.controller;

import ecoa.dto.CampanhaDto;
import ecoa.dto.CampanhaResumoDto;
import ecoa.dto.NovaCampanhaDto;
import ecoa.service.CampanhaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** As campanhas: todos veem, e só o operador cria, edita e apaga (UC-05, UC-06, UC-07). */
@RestController
@RequestMapping("/campanhas")
public class CampanhaController {

  private final CampanhaService campanhas;

  public CampanhaController(CampanhaService campanhas) {
    this.campanhas = campanhas;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('CRIADOR', 'OPERADOR', 'GRAVADORA')")
  public List<CampanhaResumoDto> listar(@AuthenticationPrincipal Jwt token) {
    return campanhas.listar(ContaDoToken.id(token), ContaDoToken.funcao(token));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CRIADOR', 'OPERADOR', 'GRAVADORA')")
  public CampanhaDto mostrar(@AuthenticationPrincipal Jwt token, @PathVariable int id) {
    return campanhas.mostrar(id, ContaDoToken.id(token), ContaDoToken.funcao(token));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('OPERADOR')")
  public CampanhaDto criar(@Valid @RequestBody NovaCampanhaDto corpo) {
    return campanhas.criar(corpo);
  }
}
