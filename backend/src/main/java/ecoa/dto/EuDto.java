package ecoa.dto;

import ecoa.regras.Funcao;

/**
 * A própria conta (GET /eu). O bloco do criador é nulo para operador e gravadora, e é por ele que o
 * frontend escolhe a tela: pedir o @, mostrar a recusa ou a de bloqueio (UC-01 4b e 4c).
 */
public record EuDto(String nome, String email, Funcao funcao, EuCriadorDto criador) {}
