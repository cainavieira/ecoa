package ecoa.dto;

import jakarta.validation.constraints.NotBlank;

/** O motivo que recusa, bloqueia, desbloqueia ou desqualifica (RN-13, RN-17, RN-21, RN-38). */
public record MotivoDto(@NotBlank(message = "MOTIVO_AUSENTE") String motivo) {}
