package ecoa.mapper;

import ecoa.dto.EuCriadorDto;
import ecoa.dto.EuDto;
import ecoa.entity.Conta;
import ecoa.entity.Criador;

/** A conta para a própria pessoa: é o único DTO de criador com e-mail e código (RN-57). */
public final class EuMapper {

  private EuMapper() {}

  public static EuDto deConta(Conta conta) {
    return new EuDto(conta.nome(), conta.email(), conta.funcao(), null);
  }

  public static EuDto deCriador(Conta conta, Criador criador) {
    return new EuDto(
        conta.nome(),
        conta.email(),
        conta.funcao(),
        new EuCriadorDto(
            criador.status(),
            criador.usuarioTiktok(),
            criador.codigoVerificacao(),
            criador.motivoRecusa(),
            criador.motivoBloqueio()));
  }
}
