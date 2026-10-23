package ecoa.entity;

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
public class Ganhador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idGanhador;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_participacao")
  private Participacao participacao;

  private String premio;
  private LocalDate entregueEm;
  private Instant registradoEm;

  protected Ganhador() {}

  /** O prêmio em texto; o sistema não calcula prêmio (RN-51). */
  public Ganhador(Participacao participacao, String premio, Instant registradoEm) {
    this.participacao = participacao;
    this.premio = premio;
    this.registradoEm = registradoEm;
  }

  /** A gravadora entregou o prêmio, por fora do sistema (RN-55). */
  public void registrarEntrega(LocalDate entregueEm) {
    this.entregueEm = entregueEm;
  }

  public Integer idGanhador() {
    return idGanhador;
  }

  public Participacao participacao() {
    return participacao;
  }

  public String premio() {
    return premio;
  }

  public LocalDate entregueEm() {
    return entregueEm;
  }
}
