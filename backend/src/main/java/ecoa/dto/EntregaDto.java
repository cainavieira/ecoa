package ecoa.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** A data em que a gravadora entregou o prêmio, que não é futura (RN-52, RN-55). */
public record EntregaDto(@NotNull LocalDate entregueEm) {}
