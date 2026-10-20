package ecoa.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(OutputCaptureExtension.class)
class TratadorGlobalTest {

  record ComViews(Long views) {}

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

    @GetMapping("/videos/{id}")
    void video(@PathVariable int id) {}

    @PostMapping("/views")
    void views(@RequestBody ComViews corpo) {}

    @GetMapping("/inesperado")
    void inesperado() {
      throw new IllegalStateException("falhou ao gravar fulano@exemplo.com");
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

  @Test
  void rotaInexistenteRespondeNaoEncontrado() throws Exception {
    mvc.perform(get("/nao-existe"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void metodoQueARotaNaoAceitaRespondeNaoEncontrado() throws Exception {
    mvc.perform(post("/publicacao"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void idQueNaoEhNumeroRespondeNaoEncontrado() throws Exception {
    mvc.perform(get("/videos/abc"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro.codigo").value("NAO_ENCONTRADO"));
  }

  @Test
  void corpoComTipoErradoRespondeCorpoInvalidoComOCampo() throws Exception {
    mvc.perform(
            post("/views").contentType(MediaType.APPLICATION_JSON).content("{\"views\": \"abc\"}"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.erro.codigo").value("CORPO_INVALIDO"))
        .andExpect(jsonPath("$.erro.campo").value("views"));
  }

  @Test
  void erroInesperadoNaoLevaAMensagemParaARespostaNemParaOLog(CapturedOutput saida)
      throws Exception {
    mvc.perform(get("/inesperado"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.erro.codigo").value("ERRO_INTERNO"))
        .andExpect(
            jsonPath("$.erro.mensagem")
                .value("Erro inesperado. Tente de novo; se continuar, avise a operação."));
    assertThat(saida.getAll())
        .contains("IllegalStateException")
        .doesNotContain("fulano@exemplo.com");
  }
}
