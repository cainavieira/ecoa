package ecoa.controller;

import ecoa.dto.LeituraDoHistoricoDto;
import ecoa.dto.LinhaDaPlanilhaDto;
import ecoa.dto.ViewsDto;
import ecoa.service.LeituraService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * As leituras, que o operador digita da página pública de cada vídeo (UC-11, ADR-0001). A leitura é
 * endereçada pelo vídeo e pela data, a chave natural dela (endpoints.md).
 */
@RestController
public class LeituraController {

  private final LeituraService leituras;

  public LeituraController(LeituraService leituras) {
    this.leituras = leituras;
  }

  @GetMapping("/campanhas/{id}/leituras")
  @PreAuthorize("hasRole('OPERADOR')")
  public List<LinhaDaPlanilhaDto> planilha(@PathVariable int id) {
    return leituras.planilha(id);
  }

  @GetMapping("/videos/{id}/leituras")
  @PreAuthorize("hasRole('OPERADOR')")
  public List<LeituraDoHistoricoDto> historico(@PathVariable int id) {
    return leituras.historico(id);
  }

  /** PUT, porque a data identifica a leitura, e repetir dá o mesmo resultado (endpoints.md). */
  @PutMapping("/videos/{id}/leituras/{data}")
  @PreAuthorize("hasRole('OPERADOR')")
  public LeituraDoHistoricoDto gravar(
      @PathVariable int id,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
      @Valid @RequestBody ViewsDto corpo) {
    return leituras.gravar(id, data, corpo.views());
  }

  @DeleteMapping("/videos/{id}/leituras/{data}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('OPERADOR')")
  public void apagar(
      @PathVariable int id,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
    leituras.apagar(id, data);
  }
}
