package ecoa.repository;

import ecoa.entity.Ganhador;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GanhadorRepository extends JpaRepository<Ganhador, Integer> {

  /** Os ganhadores da campanha, com o criador numa consulta só, na ordem do registro. */
  @Query(
      "select g from Ganhador g join fetch g.participacao p left join fetch p.criador"
          + " where p.campanha.idCampanha = :idCampanha order by g.idGanhador")
  List<Ganhador> daCampanha(Integer idCampanha);
}
