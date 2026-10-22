package ecoa.exception;

import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;

/** Converte toda exceção no formato único de erro (convencoes-backend.md). */
@RestControllerAdvice
public class TratadorGlobal {

  private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobal.class);

  private static final String NAO_ENCONTRADO = "Não encontrado.";
  private static final String CORPO_INVALIDO =
      "A requisição veio num formato que o sistema não entende.";
  private static final String ERRO_INTERNO =
      "Erro inesperado. Tente de novo; se continuar, avise a operação.";
  private static final String NAO_AUTENTICADO = "Entre de novo para continuar.";
  private static final String SEM_PERMISSAO = "Sua conta não tem acesso a esta ação.";

  @ExceptionHandler
  ResponseEntity<RespostaDeErro> recusaDeRegra(RegraVioladaException excecao) {
    return resposta(excecao.recusa(), null, excecao.getMessage(), excecao.itens());
  }

  /** Sem token de acesso válido: ausente, vencido ou com assinatura que não confere (ADR-0008). */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> naoAutenticado(AuthenticationException excecao) {
    return resposta(Recusa.NAO_AUTENTICADO, null, NAO_AUTENTICADO, List.of());
  }

  /** A função do token não alcança a rota (ADR-0009). */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> semPermissao(AccessDeniedException excecao) {
    return resposta(Recusa.SEM_PERMISSAO, null, SEM_PERMISSAO, List.of());
  }

  /** O corpo não vira o DTO: JSON quebrado, ou valor que não tem o tipo do campo. */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> corpoInvalido(HttpMessageNotReadableException excecao) {
    String campo = null;
    if (excecao.getCause() instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
      campo = jackson.getPath().getLast().getPropertyName();
    }
    return resposta(Recusa.CORPO_INVALIDO, campo, CORPO_INVALIDO, List.of());
  }

  /**
   * Campo do corpo que não passa no Bean Validation: a anotação traz o código do catálogo, e a
   * frase que a pessoa lê vem do catálogo. Anotação sem código sai como CORPO_INVALIDO, com o
   * campo.
   */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> campoInvalido(MethodArgumentNotValidException excecao) {
    FieldError erro = excecao.getBindingResult().getFieldError();
    String campo = erro == null ? null : erro.getField();
    String codigo = erro == null ? null : erro.getDefaultMessage();
    return Arrays.stream(Recusa.values())
        .filter(recusa -> recusa.name().equals(codigo))
        .findFirst()
        .map(
            recusa -> resposta(recusa, campo, CatalogoDeRecusas.mensagemDoCampo(recusa), List.of()))
        .orElseGet(() -> resposta(Recusa.CORPO_INVALIDO, campo, CORPO_INVALIDO, List.of()));
  }

  /** Id que não é número: o recurso não pode existir. */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> idInvalido(MethodArgumentTypeMismatchException excecao) {
    return resposta(Recusa.NAO_ENCONTRADO, null, NAO_ENCONTRADO, List.of());
  }

  /** O que não tem tratador próprio: erro que o Spring já classifica, ou o inesperado. */
  @ExceptionHandler
  ResponseEntity<RespostaDeErro> outroErro(Exception excecao) {
    if (excecao instanceof ErrorResponse doSpring) {
      int status = doSpring.getStatusCode().value();
      if (status == 404 || status == 405) {
        return resposta(Recusa.NAO_ENCONTRADO, null, NAO_ENCONTRADO, List.of());
      }
      if (doSpring.getStatusCode().is4xxClientError()) {
        return resposta(Recusa.CORPO_INVALIDO, null, CORPO_INVALIDO, List.of());
      }
    }
    // Só a classe e o lugar de cada causa: a mensagem pode trazer e-mail ou @ (RNF-16).
    LOG.error("Erro inesperado: {}", rastro(excecao));
    return resposta(Recusa.ERRO_INTERNO, null, ERRO_INTERNO, List.of());
  }

  static String rastro(Throwable erro) {
    StringJoiner causas = new StringJoiner(" <- ");
    for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
      StackTraceElement[] pilha = causa.getStackTrace();
      causas.add(causa.getClass().getName() + (pilha.length > 0 ? " em " + pilha[0] : ""));
    }
    return causas.toString();
  }

  private static ResponseEntity<RespostaDeErro> resposta(
      Recusa recusa, String campo, String mensagem, List<Integer> itens) {
    CatalogoDeRecusas.Descricao descricao = CatalogoDeRecusas.descricao(recusa);
    RespostaDeErro.Erro erro =
        new RespostaDeErro.Erro(recusa.name(), campo, mensagem, descricao.regra(), itens);
    return ResponseEntity.status(descricao.status()).body(new RespostaDeErro(erro));
  }
}
