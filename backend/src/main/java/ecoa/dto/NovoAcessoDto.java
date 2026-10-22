package ecoa.dto;

import ecoa.regras.Funcao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** O e-mail e a função que o operador cadastra (UC-18). */
public record NovoAcessoDto(@NotBlank @Email String email, @NotNull FuncaoDeAcesso funcao) {

  /**
   * Só operador e gravadora: conta de criador nasce no primeiro acesso (RN-03). Outro valor no
   * corpo nem vira o DTO, e sai como CORPO_INVALIDO.
   */
  public enum FuncaoDeAcesso {
    OPERADOR,
    GRAVADORA;

    public Funcao funcao() {
      return Funcao.valueOf(name());
    }
  }
}
