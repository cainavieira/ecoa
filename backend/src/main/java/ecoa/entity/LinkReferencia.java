package ecoa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class LinkReferencia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idLinkReferencia;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "xid_campanha")
  private Campanha campanha;

  private String url;
  private String descricao;

  protected LinkReferencia() {}

  public LinkReferencia(Campanha campanha, String url, String descricao) {
    this.campanha = campanha;
    this.url = url;
    this.descricao = descricao;
  }

  public String url() {
    return url;
  }

  public String descricao() {
    return descricao;
  }
}
