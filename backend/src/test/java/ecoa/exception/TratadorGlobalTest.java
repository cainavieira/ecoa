package ecoa.exception;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class TratadorGlobalTest {

  @RestController
  static class RotasDeTeste {

    @GetMapping("/publicacao")
    void publicacao() {
      throw new RegraVioladaException(
          Recusa.PUBLICACAO_FORA_DO_PRAZO,
          "A data de publicação precisa estar entre 01/11/2026 e 30/11/2026.");
    }

    @GetMapping("/datas")
    void datas() {
      throw new RegraVioladaException(
          Recusa.DATAS_EXCLUEM_VIDEO,
          "Há vídeo aprovado com a publicação fora das datas novas.",
          List.of(2));
    }
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new RotasDeTeste())
          .setControllerAdvice(new TratadorGlobal())
          .build();

  @Test
  void recusaDeRegraSaiNoFormatoUnicoComOStatusDoCatalogo() throws Exception {
    mvc.perform(get("/publicacao"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.codigo").value("PUBLICACAO_FORA_DO_PRAZO"))
        .andExpect(jsonPath("$.erro.campo").value(nullValue()))
        .andExpect(
            jsonPath("$.erro.mensagem")
                .value("A data de publicação precisa estar entre 01/11/2026 e 30/11/2026."))
        .andExpect(jsonPath("$.erro.regra").value("RN-36"))
        .andExpect(jsonPath("$.erro.itens").isEmpty());
  }

  @Test
  void recusaSobreVariosObjetosListaOsItens() throws Exception {
    mvc.perform(get("/datas"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro.itens[0]").value(2));
  }
}
