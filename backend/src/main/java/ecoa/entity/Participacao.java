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
}
