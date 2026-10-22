package ecoa.controller;

import ecoa.dto.AcessoDto;
import ecoa.dto.NovoAcessoDto;
import ecoa.service.AcessoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** O cadastro e a remoção dos acessos de operador e de gravadora (UC-18). */
@RestController
@RequestMapping("/acessos")
public class AcessoController {

  private final AcessoService acessos;

  public AcessoController(AcessoService acessos) {
    this.acessos = acessos;
  }

  @GetMapping
  @PreAuthorize("hasRole('OPERADOR')")
  public List<AcessoDto> listar() {
    return acessos.listar();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('OPERADOR')")
  public AcessoDto cadastrar(@Valid @RequestBody NovoAcessoDto corpo) {
    return acessos.cadastrar(corpo.email(), corpo.funcao().funcao());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('OPERADOR')")
  public void remover(@AuthenticationPrincipal Jwt token, @PathVariable int id) {
    acessos.remover(ContaDoToken.id(token), id);
  }
}
