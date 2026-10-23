package ecoa.dto;

import ecoa.regras.StatusCampanha;
import java.time.LocalDate;
import java.util.List;

/**
 * A página da campanha: os dados, o regulamento, os links, as datas e o status (RF-23). O participa
 * só vem para o criador, que sem ele não sabe se ainda pode entrar (UC-08 3b).
 */
public record CampanhaDto(
    int id,
    String musica,
    String artista,
    String regulamento,
    List<LinkReferenciaDto> links,
    LocalDate dataInicio,
    LocalDate dataFim,
    LocalDate dataFechamento,
    StatusCampanha status,
    Boolean participa) {}
