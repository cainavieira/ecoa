package ecoa.mapper;

import ecoa.dto.CriadorDto;
import ecoa.entity.Conta;
import ecoa.entity.Criador;
import java.time.LocalDate;

/** A conta de criador para o operador, o único que recebe e-mail e código de outra pessoa. */
public final class CriadorMapper {

  private CriadorMapper() {}

  public static CriadorDto paraOperador(Criador criador, LocalDate contaBloqueadaApagadaEm) {
    Conta conta = criador.conta();
    return new CriadorDto(
        criador.idCriador(),
        conta.nome(),
        conta.email(),
        criador.status(),
        criador.usuarioTiktok(),
        criador.codigoVerificacao(),
        contaBloqueadaApagadaEm);
  }
}
