package ecoa.repository;

import ecoa.entity.Participacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Integer> {

  /** As campanhas de que o criador participa, sempre filtradas pela conta dele (ADR-0009). */
  @Query("select p.campanha.idCampanha from Participacao p where p.criador.idCriador = :idCriador")
  List<Integer> campanhasDoCriador(Integer idCriador);

  boolean existsByCampanhaIdCampanhaAndCriadorIdCriador(Integer idCampanha, Integer idCriador);

  boolean existsByCampanhaIdCampanha(Integer idCampanha);
}
