package ecoa.repository;

import ecoa.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Integer> {

  boolean existsByEmail(String email);
}
