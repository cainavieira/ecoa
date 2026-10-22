package ecoa.dto;

import ecoa.regras.StatusCriador;

/**
 * O que o criador vê da própria conta: o status, o @, o código de verificação e o motivo da recusa
 * ou do bloqueio (RF-10, RF-13, RF-17).
 */
public record EuCriadorDto(
    StatusCriador status,
    String usuarioTiktok,
    String codigoVerificacao,
    String motivoRecusa,
    String motivoBloqueio) {}
