package ecoa.repository;

import ecoa.entity.TokenRenovacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRenovacaoRepository extends JpaRepository<TokenRenovacao, Integer> {

  Optional<TokenRenovacao> findByHash(String hash);
}
