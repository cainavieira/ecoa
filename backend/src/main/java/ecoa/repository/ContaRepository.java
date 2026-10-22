package ecoa.repository;

import ecoa.entity.Conta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Integer> {

  boolean existsByEmail(String email);

  Optional<Conta> findByEmail(String email);
}
