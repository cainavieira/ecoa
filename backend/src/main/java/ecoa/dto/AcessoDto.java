package ecoa.dto;

import ecoa.regras.Funcao;

/** Uma conta de operador ou de gravadora, na lista de acessos (RF-06, RF-07). */
public record AcessoDto(int id, String nome, String email, Funcao funcao) {}
