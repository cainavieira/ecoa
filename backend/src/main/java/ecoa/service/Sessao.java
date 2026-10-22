package ecoa.service;

/** Os dois tokens que viram cookies: o de acesso e o de renovação (ADR-0008). */
public record Sessao(String acesso, String renovacao) {}
