package ecoa.service;

import ecoa.dto.ParticipacaoDto;

/** A participação, e se ela nasceu agora ou já existia, que a rota responde com 201 ou 200. */
public record Entrada(ParticipacaoDto participacao, boolean nova) {}
