package ecoa.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Leitura {

  @EmbeddedId private LeituraId id;

  @MapsId("xidVideo")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_video")
  private Video video;

  private Long views;
  private Instant registradaEm;

  protected Leitura() {}

  public Integer idVideo() {
    return id.xidVideo();
  }

  public LocalDate data() {
    return id.data();
  }

  public long views() {
    return views;
  }
}
