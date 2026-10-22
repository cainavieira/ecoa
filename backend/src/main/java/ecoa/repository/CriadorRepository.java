package ecoa.repository;

import ecoa.entity.Criador;
import ecoa.regras.StatusCriador;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CriadorRepository extends JpaRepository<Criador, Integer> {

  boolean existsByIdCriadorAndStatus(Integer idCriador, StatusCriador status);

  boolean existsByCodigoVerificacao(String codigoVerificacao);

  @Query("select c from Criador c join fetch c.conta order by c.idCriador")
  List<Criador> listarComConta();

  @Query("select c from Criador c join fetch c.conta where c.status = :status order by c.idCriador")
  List<Criador> listarComConta(StatusCriador status);

  /** Os status das outras contas com o mesmo @, que a regra usa para decidir (RN-11). */
  @Query(
      "select c.status from Criador c where c.usuarioTiktok = :usuarioTiktok"
          + " and c.idCriador <> :idCriador")
  List<StatusCriador> statusDasOutrasComOUsuarioTiktok(String usuarioTiktok, Integer idCriador);
}
