package ecoa.dto;

import ecoa.regras.StatusCriador;
import java.time.LocalDate;

/**
 * A conta de criador para o operador, na fila de verificação e na lista de contas: o e-mail e o
 * código vão porque é com eles que o operador confere e confirma (RF-11, RF-15, RN-12). Quando o @
 * foi de conta bloqueada e apagada, vem a data da exclusão, que é o aviso (RN-65).
 */
public record CriadorDto(
    int id,
    String nome,
    String email,
    StatusCriador status,
    String usuarioTiktok,
    String codigoVerificacao,
    LocalDate contaBloqueadaApagadaEm) {}
