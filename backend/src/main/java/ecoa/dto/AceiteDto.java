package ecoa.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * O aceite do regulamento e da exibição do @ e das views para os outros participantes e para a
 * gravadora (RN-31). Ausente ou falso, a entrada é recusada.
 */
public record AceiteDto(
    @NotNull(message = "ACEITE_AUSENTE") @AssertTrue(message = "ACEITE_AUSENTE") Boolean aceite) {}
