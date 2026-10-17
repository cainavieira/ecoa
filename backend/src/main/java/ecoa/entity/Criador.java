package ecoa.entity;

import ecoa.regras.StatusCriador;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.time.Instant;

@Entity
public class Criador {

  @Id private Integer idCriador;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_criador")
  private Conta conta;

  private String usuarioTiktok;
  private String codigoVerificacao;
  private StatusCriador status;
  private String motivoRecusa;
  private String motivoBloqueio;
  private String motivoDesbloqueio;
  private Instant aceiteTermosEm;

  protected Criador() {}
}
