package ecoa.dto;

import ecoa.regras.StatusCriador;

/**
 * Um participante da campanha, para o operador escolher o ganhador: o id da conta, o @ e o status,
 * que diz se ele está bloqueado (RF-35, RN-52).
 */
public record ParticipanteDto(int idCriador, String usuarioTiktok, StatusCriador status) {}
