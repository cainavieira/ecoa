package ecoa.regras;

import java.time.LocalDate;

/** O que a RN-27 precisa saber de um vídeo aprovado. */
public record VideoAprovado(int idVideo, LocalDate dataPublicacao) {}
