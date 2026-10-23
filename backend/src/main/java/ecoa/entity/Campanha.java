package ecoa.entity;

import ecoa.regras.DatasDaCampanha;
import ecoa.regras.RegrasDaCampanha;
import ecoa.regras.StatusCampanha;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Campanha {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idCampanha;

  private String musica;
  private String artista;
  private String regulamento;
  private LocalDate dataInicio;
  private LocalDate dataFim;
  private LocalDate dataFechamento;
  private Instant encerradaEm;
  private Instant criadoEm;

  /** Os links não existem fora da campanha: entram, saem e somem com ela. */
  @OneToMany(mappedBy = "campanha", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("idLinkReferencia")
  private List<LinkReferencia> links = new ArrayList<>();

  protected Campanha() {}

  public Campanha(
      String musica, String artista, String regulamento, DatasDaCampanha datas, Instant criadoEm) {
    this.musica = musica;
    this.artista = artista;
    this.regulamento = regulamento;
    mudarDatas(datas);
    this.criadoEm = criadoEm;
  }

  public Integer idCampanha() {
    return idCampanha;
  }

  public String musica() {
    return musica;
  }

  public String artista() {
    return artista;
  }

  public String regulamento() {
    return regulamento;
  }

  public DatasDaCampanha datas() {
    return new DatasDaCampanha(dataInicio, dataFim, dataFechamento);
  }

  /** O status não é gravado: sai das datas e do encerramento, a cada leitura (RN-25). */
  public StatusCampanha status(LocalDate hoje) {
    return RegrasDaCampanha.statusDaCampanha(dataInicio, dataFim, encerradaEm != null, hoje);
  }

  public List<LinkReferencia> links() {
    return Collections.unmodifiableList(links);
  }

  /** Os textos e as datas depois da edição, com as datas já conferidas pela regra (UC-06). */
  public void editar(String musica, String artista, String regulamento, DatasDaCampanha datas) {
    this.musica = musica;
    this.artista = artista;
    this.regulamento = regulamento;
    mudarDatas(datas);
  }

  /** Os links novos substituem os antigos: a lista vem inteira, e o que ficou de fora sai. */
  public void trocarLinks(List<LinkReferencia> novos) {
    links.clear();
    links.addAll(novos);
  }

  private void mudarDatas(DatasDaCampanha datas) {
    this.dataInicio = datas.inicio();
    this.dataFim = datas.fim();
    this.dataFechamento = datas.fechamento();
  }
}
