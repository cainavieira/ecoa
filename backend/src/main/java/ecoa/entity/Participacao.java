package ecoa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class Participacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idParticipacao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_campanha")
  private Campanha campanha;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "xid_criador")
  private Criador criador;

  private Instant aceiteEm;
  private Integer posicaoFinal;
  private Long totalFinal;

  protected Participacao() {}

  public Integer idParticipacao() {
    return idParticipacao;
  }

  public Instant aceiteEm() {
    return aceiteEm;
  }

  public Campanha campanha() {
    return campanha;
  }

  /** Nulo para o participante removido, que só existe em campanha encerrada (RN-59). */
  public Criador criador() {
    return criador;
  }

  /** A posição no ranking final; nula antes do encerramento e para quem ficou fora (RN-48). */
  public Integer posicaoFinal() {
    return posicaoFinal;
  }

  public Long totalFinal() {
    return totalFinal;
  }

  /** A posição e a soma no encerramento, que não mudam mais (RN-48). */
  public void gravarRankingFinal(int posicao, long total) {
    this.posicaoFinal = posicao;
    this.totalFinal = total;
  }
}
