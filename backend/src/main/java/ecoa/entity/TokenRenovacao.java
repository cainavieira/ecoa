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
public class TokenRenovacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idTokenRenovacao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_conta")
  private Conta conta;

  private String hash;
  private Instant criadoEm;
  private Instant expiraEm;

  protected TokenRenovacao() {}

  public Conta conta() {
    return conta;
  }

  public Instant expiraEm() {
    return expiraEm;
  }

  /** Cada uso empurra a validade, sem trocar o token (ADR-0008). */
  public void prorrogarAte(Instant expiraEm) {
    this.expiraEm = expiraEm;
  }
}
