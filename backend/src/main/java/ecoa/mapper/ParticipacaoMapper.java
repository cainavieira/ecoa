package ecoa.mapper;

import ecoa.dto.ParticipacaoDto;
import ecoa.entity.Participacao;
import java.time.LocalDate;

public final class ParticipacaoMapper {

  private ParticipacaoMapper() {}

  public static ParticipacaoDto paraDto(
      Participacao participacao, int idCampanha, LocalDate aceiteEm) {
    return new ParticipacaoDto(participacao.idParticipacao(), idCampanha, aceiteEm);
  }
}
