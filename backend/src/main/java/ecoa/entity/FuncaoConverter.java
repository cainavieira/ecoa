package ecoa.entity;

import ecoa.regras.Funcao;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FuncaoConverter extends EnumEmMinusculasConverter<Funcao> {

  public FuncaoConverter() {
    super(Funcao.class);
  }
}
