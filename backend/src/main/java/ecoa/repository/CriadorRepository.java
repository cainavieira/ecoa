package ecoa.repository;

import ecoa.entity.Criador;
import ecoa.regras.StatusCriador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriadorRepository extends JpaRepository<Criador, Integer> {

  boolean existsByIdCriadorAndStatus(Integer idCriador, StatusCriador status);
}
