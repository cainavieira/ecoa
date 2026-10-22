package ecoa.repository;

import ecoa.entity.Conta;
import ecoa.regras.Funcao;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ContaRepository extends JpaRepository<Conta, Integer> {

  boolean existsByEmail(String email);

  Optional<Conta> findByEmail(String email);

  List<Conta> findByFuncaoInOrderByIdConta(Collection<Funcao> funcoes);

  /**
   * As contas da função, com as linhas bloqueadas até o fim da transação (select for update). A
   * ordem fixa faz duas transações bloquearem na mesma sequência, e a segunda espera, em vez de
   * travar as duas.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Conta c where c.funcao = :funcao order by c.idConta")
  List<Conta> travarPorFuncao(Funcao funcao);
}
