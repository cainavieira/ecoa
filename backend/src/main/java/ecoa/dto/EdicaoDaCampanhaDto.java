package ecoa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * O que o operador muda na campanha (UC-06). Campo que não vem fica como está; a lista de links,
 * quando vem, substitui a antiga inteira, e vazia é recusada (RN-22).
 */
public record EdicaoDaCampanhaDto(
    @Pattern(regexp = TEXTO) String musica,
    @Pattern(regexp = TEXTO) String artista,
    @Pattern(regexp = TEXTO) String regulamento,
    @Size(min = 1, message = "LINK_REFERENCIA_AUSENTE") @Valid
        List<@NotNull LinkReferenciaDto> links,
    LocalDate dataInicio,
    LocalDate dataFim,
    LocalDate dataFechamento) {

  /** Texto que vem não pode ser vazio nem só espaço, como na criação. */
  private static final String TEXTO = "(?s).*\\S.*";
}
