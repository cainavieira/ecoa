package ecoa.repository;

import ecoa.entity.Campanha;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampanhaRepository extends JpaRepository<Campanha, Integer> {

  List<Campanha> findAllByOrderByIdCampanhaDesc();
}
