package ecoa.entity;

import ecoa.regras.Funcao;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Conta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idConta;

  private String email;
  private String nome;
  private Funcao funcao;
  private Instant criadoEm;

  protected Conta() {}
}
