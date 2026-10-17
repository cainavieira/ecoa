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
}
