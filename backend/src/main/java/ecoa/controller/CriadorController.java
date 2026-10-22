package ecoa.controller;

import ecoa.dto.CriadorDto;
import ecoa.dto.MotivoDto;
import ecoa.dto.UsuarioTiktokDto;
import ecoa.regras.StatusCriador;
import ecoa.service.CriadorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A verificação, o bloqueio e o desbloqueio das contas de criador, pelo operador (UC-03, UC-04).
 */
@RestController
@RequestMapping("/criadores")
public class CriadorController {

  private final CriadorService criadores;

  public CriadorController(CriadorService criadores) {
    this.criadores = criadores;
  }

  /** Sem status, a lista de contas; com ?status=pendente, a fila de verificação. */
  @GetMapping
  @PreAuthorize("hasRole('OPERADOR')")
  public List<CriadorDto> listar(@RequestParam(required = false) StatusCriador status) {
    return status == null ? criadores.listar() : criadores.listar(status);
  }

  @PostMapping("/{id}/aprovacao")
  @PreAuthorize("hasRole('OPERADOR')")
  public CriadorDto aprovar(@PathVariable int id) {
    return criadores.aprovar(id);
  }

  @PostMapping("/{id}/recusa")
  @PreAuthorize("hasRole('OPERADOR')")
  public CriadorDto recusar(@PathVariable int id, @Valid @RequestBody MotivoDto corpo) {
    return criadores.recusar(id, corpo.motivo());
  }

  @PutMapping("/{id}/usuario-tiktok")
  @PreAuthorize("hasRole('OPERADOR')")
  public CriadorDto corrigirUsuarioTiktok(
      @PathVariable int id, @Valid @RequestBody UsuarioTiktokDto corpo) {
    return criadores.corrigirUsuarioTiktok(id, corpo.usuarioTiktok());
  }

  @PostMapping("/{id}/bloqueio")
  @PreAuthorize("hasRole('OPERADOR')")
  public CriadorDto bloquear(@PathVariable int id, @Valid @RequestBody MotivoDto corpo) {
    return criadores.bloquear(id, corpo.motivo());
  }

  @PostMapping("/{id}/desbloqueio")
  @PreAuthorize("hasRole('OPERADOR')")
  public CriadorDto desbloquear(@PathVariable int id, @Valid @RequestBody MotivoDto corpo) {
    return criadores.desbloquear(id, corpo.motivo());
  }
}
