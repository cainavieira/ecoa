package ecoa.exception;

import ecoa.regras.RegraVioladaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte toda exceção no formato único de erro (convencoes-backend.md). */
@RestControllerAdvice
public class TratadorGlobal {

  @ExceptionHandler
  ResponseEntity<RespostaDeErro> recusaDeRegra(RegraVioladaException excecao) {
    CatalogoDeRecusas.Descricao descricao = CatalogoDeRecusas.descricao(excecao.recusa());
    RespostaDeErro.Erro erro =
        new RespostaDeErro.Erro(
            excecao.recusa().name(),
            null,
            excecao.getMessage(),
            descricao.regra(),
            excecao.itens());
    return ResponseEntity.status(descricao.status()).body(new RespostaDeErro(erro));
  }
}
