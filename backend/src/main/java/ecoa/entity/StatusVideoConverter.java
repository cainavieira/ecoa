package ecoa.entity;

import ecoa.regras.StatusVideo;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusVideoConverter extends EnumEmMinusculasConverter<StatusVideo> {

  public StatusVideoConverter() {
    super(StatusVideo.class);
  }
}
