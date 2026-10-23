package ecoa.dto;

import ecoa.regras.StatusCampanha;
import java.time.LocalDate;

/**
 * Uma campanha na lista, com o status calculado na hora (RN-25). O participa só vem para o criador,
 * e é nulo para operador e gravadora (RF-22).
 */
public record CampanhaResumoDto(
    int id,
    String musica,
    String artista,
    LocalDate dataInicio,
    LocalDate dataFim,
    LocalDate dataFechamento,
    StatusCampanha status,
    Boolean participa) {}
