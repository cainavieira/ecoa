package ecoa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * O participante, pelo id da conta, e o prêmio em texto (UC-15). Se ele participa e não está
 * bloqueado, quem confere é a regra (RN-52).
 */
public record NovoGanhadorDto(@NotNull Integer idCriador, @NotBlank String premio) {}
