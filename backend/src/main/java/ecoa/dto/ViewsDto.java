package ecoa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * As views de uma leitura (RN-39). Menos views que a leitura anterior é aceito, porque as redes
 * removem views falsas; com parte decimal, nem viram o DTO, e saem como CORPO_INVALIDO.
 */
public record ViewsDto(@NotNull @PositiveOrZero(message = "VIEWS_INVALIDAS") Long views) {}
