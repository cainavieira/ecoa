package ecoa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * A data de publicação e as views da primeira leitura, que leva a data de hoje (RN-36, RN-39).
 * Views com parte decimal nem viram o DTO, e saem como CORPO_INVALIDO (UC-10 3b).
 */
public record AprovacaoDto(
    @NotNull LocalDate dataPublicacao,
    @NotNull @PositiveOrZero(message = "VIEWS_INVALIDAS") Long views) {}
