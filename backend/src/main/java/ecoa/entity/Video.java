package ecoa.entity;

import ecoa.regras.StatusVideo;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idVideo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_participacao")
  private Participacao participacao;

  private String link;
  private StatusVideo status;
  private LocalDate dataPublicacao;
  private String motivo;
  private Instant enviadoEm;

  protected Video() {}

  public Integer idVideo() {
    return idVideo;
  }

  public String link() {
    return link;
  }

  public StatusVideo status() {
    return status;
  }

  public LocalDate dataPublicacao() {
    return dataPublicacao;
  }

  public String motivo() {
    return motivo;
  }
}
