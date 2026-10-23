package ecoa.mapper;

import ecoa.dto.CampanhaDto;
import ecoa.dto.CampanhaResumoDto;
import ecoa.dto.LinkReferenciaDto;
import ecoa.entity.Campanha;
import ecoa.entity.LinkReferencia;
import ecoa.regras.DatasDaCampanha;
import ecoa.regras.StatusCampanha;
import java.util.List;

public final class CampanhaMapper {

  private CampanhaMapper() {}

  public static CampanhaResumoDto resumo(
      Campanha campanha, StatusCampanha status, Boolean participa) {
    DatasDaCampanha datas = campanha.datas();
    return new CampanhaResumoDto(
        campanha.idCampanha(),
        campanha.musica(),
        campanha.artista(),
        datas.inicio(),
        datas.fim(),
        datas.fechamento(),
        status,
        participa);
  }

  public static CampanhaDto paraDto(Campanha campanha, StatusCampanha status, Boolean participa) {
    DatasDaCampanha datas = campanha.datas();
    return new CampanhaDto(
        campanha.idCampanha(),
        campanha.musica(),
        campanha.artista(),
        campanha.regulamento(),
        campanha.links().stream()
            .map(link -> new LinkReferenciaDto(link.url(), link.descricao()))
            .toList(),
        datas.inicio(),
        datas.fim(),
        datas.fechamento(),
        status,
        participa);
  }

  public static List<LinkReferencia> links(Campanha campanha, List<LinkReferenciaDto> links) {
    return links.stream()
        .map(link -> new LinkReferencia(campanha, link.url(), link.descricao()))
        .toList();
  }
}
