package ecoa.dto;

import java.time.LocalDate;

/** A participação do criador na campanha, com a data do aceite no horário de Brasília (RN-31). */
public record ParticipacaoDto(int id, int idCampanha, LocalDate aceiteEm) {}
