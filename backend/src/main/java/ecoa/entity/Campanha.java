package ecoa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;

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

  protected Campanha() {}
}
