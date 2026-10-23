package ecoa.exception;

import ecoa.regras.Recusa;
import org.springframework.http.HttpStatus;

/** O status e a regra de cada código, como no catálogo (convencoes-backend.md). */
final class CatalogoDeRecusas {

  record Descricao(HttpStatus status, String regra) {}

  private CatalogoDeRecusas() {}

  static Descricao descricao(Recusa recusa) {
    return switch (recusa) {
      case DATAS_FORA_DE_ORDEM -> new Descricao(HttpStatus.BAD_REQUEST, "RN-24");
      case FECHAMENTO_NAO_FUTURO -> new Descricao(HttpStatus.BAD_REQUEST, "RN-26");
      case DATAS_EXCLUEM_VIDEO -> new Descricao(HttpStatus.CONFLICT, "RN-27");
      case CAMPANHA_ENCERRADA -> new Descricao(HttpStatus.CONFLICT, "RN-28");
      case CAMPANHA_COM_PARTICIPANTE -> new Descricao(HttpStatus.CONFLICT, "RN-30");
      case ACEITE_AUSENTE -> new Descricao(HttpStatus.BAD_REQUEST, "RN-31");
      case LINK_REFERENCIA_AUSENTE -> new Descricao(HttpStatus.BAD_REQUEST, "RN-22");
      case LINK_INVALIDO -> new Descricao(HttpStatus.BAD_REQUEST, "RN-23");
      case CONTA_NAO_APROVADA -> new Descricao(HttpStatus.CONFLICT, "RN-15, RN-16, RN-17");
      case ENTRADA_ENCERRADA -> new Descricao(HttpStatus.CONFLICT, "RN-32");
      case CAMPANHA_NAO_ABERTA -> new Descricao(HttpStatus.CONFLICT, "RN-34");
      case LINK_NAO_TIKTOK -> new Descricao(HttpStatus.BAD_REQUEST, "RN-34");
      case LINK_JA_ENVIADO -> new Descricao(HttpStatus.CONFLICT, "RN-35");
      case VIDEO_NAO_EM_ANALISE -> new Descricao(HttpStatus.CONFLICT, "RN-36, RN-37");
      case PUBLICACAO_FORA_DO_PRAZO -> new Descricao(HttpStatus.CONFLICT, "RN-36");
      case CRIADOR_BLOQUEADO -> new Descricao(HttpStatus.CONFLICT, "RN-19, RN-52");
      case VIDEO_NAO_APROVADO -> new Descricao(HttpStatus.CONFLICT, "RN-38, RN-39");
      case DATA_FUTURA -> new Descricao(HttpStatus.BAD_REQUEST, "RN-40, RN-52");
      case LEITURA_ANTES_DA_PUBLICACAO -> new Descricao(HttpStatus.BAD_REQUEST, "RN-40");
      case LEITURA_DEPOIS_DO_FECHAMENTO -> new Descricao(HttpStatus.CONFLICT, "RN-41");
      case ENCERRAMENTO_ANTES_DO_FECHAMENTO -> new Descricao(HttpStatus.CONFLICT, "RN-50");
      case ENCERRAMENTO_COM_PENDENCIA -> new Descricao(HttpStatus.CONFLICT, "RN-50");
      case USUARIO_TIKTOK_INVALIDO -> new Descricao(HttpStatus.BAD_REQUEST, "RN-11");
      case USUARIO_TIKTOK_FIXO -> new Descricao(HttpStatus.CONFLICT, "RN-15");
      case CONTA_NAO_PENDENTE -> new Descricao(HttpStatus.CONFLICT, "RN-13");
      case CONTA_SEM_USUARIO_TIKTOK -> new Descricao(HttpStatus.CONFLICT, "RN-13");
      case USUARIO_TIKTOK_EM_USO -> new Descricao(HttpStatus.CONFLICT, "RN-11, RN-13, RN-15");
      case CONTA_NAO_BLOQUEADA -> new Descricao(HttpStatus.CONFLICT, "RN-21");
      case GANHADOR_ANTES_DO_INICIO -> new Descricao(HttpStatus.CONFLICT, "RN-52");
      case GANHADOR_NAO_PARTICIPANTE -> new Descricao(HttpStatus.CONFLICT, "RN-52");
      case PROPRIA_CONTA -> new Descricao(HttpStatus.CONFLICT, "RN-06");
      case ULTIMO_OPERADOR -> new Descricao(HttpStatus.CONFLICT, "RN-06");
      case NAO_ENCONTRADO -> new Descricao(HttpStatus.NOT_FOUND, "RN-02");
      case CORPO_INVALIDO -> new Descricao(HttpStatus.UNPROCESSABLE_CONTENT, null);
      case ERRO_INTERNO -> new Descricao(HttpStatus.INTERNAL_SERVER_ERROR, null);
      case NAO_AUTENTICADO -> new Descricao(HttpStatus.UNAUTHORIZED, "ADR-0008");
      case SEM_PERMISSAO -> new Descricao(HttpStatus.FORBIDDEN, "RN-02");
      case CONTA_BLOQUEADA -> new Descricao(HttpStatus.FORBIDDEN, "RN-18");
      case EMAIL_JA_CADASTRADO -> new Descricao(HttpStatus.CONFLICT, "RN-04");
      case MOTIVO_AUSENTE ->
          new Descricao(HttpStatus.BAD_REQUEST, "RN-13, RN-17, RN-21, RN-37, RN-38");
    };
  }

  /**
   * A frase que a pessoa lê quando a recusa vem de um campo do corpo: a anotação do DTO traz só o
   * código, e a frase mora aqui, uma vez por código.
   */
  static String mensagemDoCampo(Recusa recusa) {
    return switch (recusa) {
      case MOTIVO_AUSENTE -> "Informe o motivo.";
      case LINK_REFERENCIA_AUSENTE -> "Informe ao menos um link de referência.";
      case LINK_INVALIDO -> "O link precisa ser um endereço http ou https.";
      case ACEITE_AUSENTE ->
          "Para entrar, aceite o regulamento e a exibição do seu @ e das suas views.";
      default -> "Confira o campo destacado.";
    };
  }
}
