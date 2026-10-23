package ecoa.dto;

import jakarta.validation.constraints.NotNull;

/** O link do vídeo que o criador envia (UC-09). Se ele é do TikTok, quem confere é a regra. */
public record NovoVideoDto(@NotNull String link) {}
