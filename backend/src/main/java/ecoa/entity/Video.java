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

  public Participacao participacao() {
    return participacao;
  }

  /** Com a data de publicação que o operador conferiu na página do vídeo (RN-36). */
  public void aprovar(LocalDate dataPublicacao) {
    this.status = StatusVideo.APROVADO;
    this.dataPublicacao = dataPublicacao;
  }

  /** O motivo aparece na tela do criador (RN-37). */
  public void recusar(String motivo) {
    this.status = StatusVideo.RECUSADO;
    this.motivo = motivo;
  }

  /** Definitiva: o vídeo sai do ranking, e o motivo aparece na tela do criador (RN-38). */
  public void desqualificar(String motivo) {
    this.status = StatusVideo.DESQUALIFICADO;
    this.motivo = motivo;
  }
}
