package ecoa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class ContaBloqueadaApagada {

  @Id private String usuarioTiktok;

  private Instant apagadaEm;

  protected ContaBloqueadaApagada() {}
}
