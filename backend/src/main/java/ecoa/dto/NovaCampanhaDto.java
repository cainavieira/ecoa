package ecoa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * A campanha que o operador cria (UC-05). A ordem das datas e o fechamento futuro, quem confere é a
 * regra (RN-24, RN-26).
 */
public record NovaCampanhaDto(
    @NotBlank String musica,
    @NotBlank String artista,
    @NotBlank String regulamento,
    @NotEmpty(message = "LINK_REFERENCIA_AUSENTE") @Valid List<@NotNull LinkReferenciaDto> links,
    @NotNull LocalDate dataInicio,
    @NotNull LocalDate dataFim,
    @NotNull LocalDate dataFechamento) {}
