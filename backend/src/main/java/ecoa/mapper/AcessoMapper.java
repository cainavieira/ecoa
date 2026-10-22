package ecoa.mapper;

import ecoa.dto.AcessoDto;
import ecoa.entity.Conta;

public final class AcessoMapper {

  private AcessoMapper() {}

  public static AcessoDto paraDto(Conta conta) {
    return new AcessoDto(conta.idConta(), conta.nome(), conta.email(), conta.funcao());
  }
}
