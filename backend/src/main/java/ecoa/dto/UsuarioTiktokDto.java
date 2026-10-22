package ecoa.dto;

import jakarta.validation.constraints.NotNull;

/**
 * O @ que o criador informa (UC-02). Vazio ou com espaço no meio, quem recusa é a regra (RN-11).
 */
public record UsuarioTiktokDto(@NotNull String usuarioTiktok) {}
