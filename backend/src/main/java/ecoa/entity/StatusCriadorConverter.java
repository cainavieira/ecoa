package ecoa.entity;

import ecoa.regras.StatusCriador;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusCriadorConverter extends EnumEmMinusculasConverter<StatusCriador> {

  public StatusCriadorConverter() {
    super(StatusCriador.class);
  }
}
