package ecoa.controller;

import ecoa.dto.EntregaDto;
import ecoa.dto.GanhadorDto;
import ecoa.dto.NovoGanhadorDto;
import ecoa.dto.ParticipanteDto;
import ecoa.service.GanhadorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Os ganhadores e a entrega dos prêmios, pelo operador (UC-15). */
@RestController
public class GanhadorController {

  private final GanhadorService ganhadores;

  public GanhadorController(GanhadorService ganhadores) {
    this.ganhadores = ganhadores;
  }

  @GetMapping("/campanhas/{id}/participantes")
  @PreAuthorize("hasRole('OPERADOR')")
  public List<ParticipanteDto> participantes(@PathVariable int id) {
    return ganhadores.participantes(id);
  }

  @PostMapping("/campanhas/{id}/ganhadores")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('OPERADOR')")
  public GanhadorDto registrar(@PathVariable int id, @Valid @RequestBody NovoGanhadorDto corpo) {
    return ganhadores.registrar(id, corpo.idCriador(), corpo.premio());
  }

  @PatchMapping("/ganhadores/{id}")
  @PreAuthorize("hasRole('OPERADOR')")
  public GanhadorDto informarEntrega(@PathVariable int id, @Valid @RequestBody EntregaDto corpo) {
    return ganhadores.informarEntrega(id, corpo.entregueEm());
  }

  @DeleteMapping("/ganhadores/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('OPERADOR')")
  public void apagar(@PathVariable int id) {
    ganhadores.apagar(id);
  }
}
