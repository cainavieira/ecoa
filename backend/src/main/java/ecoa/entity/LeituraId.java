package ecoa.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class LeituraId implements Serializable {

  private Integer xidVideo;
  private LocalDate data;

  protected LeituraId() {}

  LeituraId(Integer xidVideo, LocalDate data) {
    this.xidVideo = xidVideo;
    this.data = data;
  }

  Integer xidVideo() {
    return xidVideo;
  }

  LocalDate data() {
    return data;
  }

  @Override
  public boolean equals(Object outro) {
    return outro instanceof LeituraId id
        && Objects.equals(xidVideo, id.xidVideo)
        && Objects.equals(data, id.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xidVideo, data);
  }
}
