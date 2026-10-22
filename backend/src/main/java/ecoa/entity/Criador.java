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

  /** A conta de criador nasce pendente (RN-10). */
  public Criador(Conta conta, String codigoVerificacao, Instant aceiteTermosEm) {
    this.conta = conta;
    this.codigoVerificacao = codigoVerificacao;
    this.status = StatusCriador.PENDENTE;
    this.aceiteTermosEm = aceiteTermosEm;
  }

  public Integer idCriador() {
    return idCriador;
  }

  public Conta conta() {
    return conta;
  }

  public StatusCriador status() {
    return status;
  }

  public String usuarioTiktok() {
    return usuarioTiktok;
  }

  public String codigoVerificacao() {
    return codigoVerificacao;
  }

  public String motivoRecusa() {
    return motivoRecusa;
  }

  public String motivoBloqueio() {
    return motivoBloqueio;
  }

  /** O @ novo e o status que a regra decidiu. O motivo da recusa só vale enquanto ela durar. */
  public void corrigirUsuarioTiktok(String usuarioTiktok, StatusCriador status) {
    this.usuarioTiktok = usuarioTiktok;
    this.status = status;
    if (status != StatusCriador.RECUSADA) {
      this.motivoRecusa = null;
    }
  }

  /** A conferência achou o código na bio (RN-13). */
  public void aprovar() {
    this.status = StatusCriador.APROVADA;
  }

  /** O motivo aparece na tela do criador (RN-13). */
  public void recusar(String motivo) {
    this.status = StatusCriador.RECUSADA;
    this.motivoRecusa = motivo;
  }
}
