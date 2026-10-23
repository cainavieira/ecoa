package ecoa.repository;

import ecoa.entity.Participacao;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Integer> {

  /** As campanhas de que o criador participa, sempre filtradas pela conta dele (ADR-0009). */
  @Query("select p.campanha.idCampanha from Participacao p where p.criador.idCriador = :idCriador")
  List<Integer> campanhasDoCriador(Integer idCriador);

  Optional<Participacao> findByCampanhaIdCampanhaAndCriadorIdCriador(
      Integer idCampanha, Integer idCriador);

  boolean existsByCampanhaIdCampanhaAndCriadorIdCriador(Integer idCampanha, Integer idCriador);

  boolean existsByCampanhaIdCampanha(Integer idCampanha);

  /**
   * Os participantes que ainda têm conta, com o criador, pela ordem do @: é de onde sai a escolha
   * do ganhador, e o participante removido não ganha prêmio novo (RN-52, RN-59).
   */
  @Query(
      "select p from Participacao p join fetch p.criador c"
          + " where p.campanha.idCampanha = :idCampanha order by c.usuarioTiktok")
  List<Participacao> comContaNaCampanha(Integer idCampanha);

  /**
   * As participações da campanha, com o criador, na ordem do ranking final e, dentro dela, do @.
   * Antes do encerramento a posição é nula, e a ordem do @ desempata o ranking.
   */
  @Query(
      "select p from Participacao p left join fetch p.criador c"
          + " where p.campanha.idCampanha = :idCampanha"
          + " order by p.posicaoFinal, c.usuarioTiktok, p.idParticipacao")
  List<Participacao> daCampanha(Integer idCampanha);

  /**
   * Grava a participação, ou nada, se ela já existe, e devolve quantas linhas entraram. A segunda
   * entrada do duplo clique espera a primeira terminar e não grava, em vez de falhar na chave
   * única.
   */
  @Modifying
  @Query(
      value =
          "insert into participacao (xid_campanha, xid_criador, aceite_em)"
              + " values (:idCampanha, :idCriador, :aceiteEm)"
              + " on conflict (xid_campanha, xid_criador) do nothing",
      nativeQuery = true)
  int entrar(Integer idCampanha, Integer idCriador, Instant aceiteEm);
}
