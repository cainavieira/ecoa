package ecoa.config;

import java.util.List;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Derruba a subida quando falta variável obrigatória, dizendo qual, em vez de um erro do banco três
 * camadas adiante (convencoes-backend.md, Configuração).
 */
public class VariaveisObrigatorias
    implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  static final List<String> NOMES = List.of("ECOA_DB_URL", "ECOA_DB_USUARIO", "ECOA_DB_SENHA");

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent evento) {
    conferir(evento.getEnvironment());
  }

  static void conferir(Environment ambiente) {
    List<String> faltando =
        NOMES.stream().filter(nome -> !StringUtils.hasText(ambiente.getProperty(nome))).toList();
    if (!faltando.isEmpty()) {
      throw new IllegalStateException(
          "Faltam as variáveis de ambiente "
              + String.join(", ", faltando)
              + ". Em desenvolvimento, elas vêm do backend/.env; veja o .env.example.");
    }
  }
}
