package ecoa.regras;

import static ecoa.regras.RegrasDoVideo.validarAprovacaoDeVideo;
import static ecoa.regras.RegrasDoVideo.validarEnvioDeVideo;
import static ecoa.regras.RegrasDoVideo.validarRecusaDeVideo;
import static ecoa.regras.RegrasDoVideo.validarVideoAprovado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RegrasDoVideoTest {

  private static final String LINK = "https://www.tiktok.com/@fulano/video/1";
  private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
  private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://vm.tiktok.com/ZMabc123/",
        "https://www.tiktok.com/@fulano/video/1",
        "https://tiktok.com/@fulano/video/1"
      })
  void aceitaLinkHttpsDoTiktok(String link) {
    assertThatCode(() -> validarEnvioDeVideo(link, StatusCampanha.ABERTA, List.of()))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://www.tiktok.com/@fulano/video/1",
        "https://eviltiktok.com/@fulano/video/1",
        "https://tiktok.com.exemplo.com/@fulano/video/1",
        "https://www.tiktok.com@exemplo.com/@fulano/video/1",
        "não é um link"
      })
  void recusaLinkQueNaoSejaHttpsDoTiktok(String link) {
    recusado(
        () -> validarEnvioDeVideo(link, StatusCampanha.ABERTA, List.of()), Recusa.LINK_NAO_TIKTOK);
  }

  @ParameterizedTest
  @EnumSource(value = StatusCampanha.class, names = "ABERTA", mode = EnumSource.Mode.EXCLUDE)
  void recusaEnvioComCampanhaQueNaoEstaAberta(StatusCampanha campanha) {
    recusado(() -> validarEnvioDeVideo(LINK, campanha, List.of()), Recusa.CAMPANHA_NAO_ABERTA);
  }

  @ParameterizedTest
  @EnumSource(value = StatusVideo.class, names = "RECUSADO", mode = EnumSource.Mode.EXCLUDE)
  void recusaLinkDeEnvioQueNaoFoiRecusado(StatusVideo anterior) {
    recusado(
        () -> validarEnvioDeVideo(LINK, StatusCampanha.ABERTA, List.of(anterior)),
        Recusa.LINK_JA_ENVIADO);
  }

  @Test
  void aceitaLinkQueSoFoiRecusado() {
    assertThatCode(
            () -> validarEnvioDeVideo(LINK, StatusCampanha.ABERTA, List.of(StatusVideo.RECUSADO)))
        .doesNotThrowAnyException();
  }

  @Test
  void aceitaPublicacaoNoDiaDoInicioENoDiaDoFim() {
    assertThatCode(() -> aprovar(INICIO)).doesNotThrowAnyException();
    assertThatCode(() -> aprovar(FIM)).doesNotThrowAnyException();
  }

  @Test
  void recusaPublicacaoNaVesperaDoInicioENoDiaSeguinteAoFim() {
    recusado(() -> aprovar(INICIO.minusDays(1)), Recusa.PUBLICACAO_FORA_DO_PRAZO);
    recusado(() -> aprovar(FIM.plusDays(1)), Recusa.PUBLICACAO_FORA_DO_PRAZO);
  }

  @ParameterizedTest
  @EnumSource(value = StatusVideo.class, names = "EM_ANALISE", mode = EnumSource.Mode.EXCLUDE)
  void recusaAprovarVideoQueNaoEstaEmAnalise(StatusVideo video) {
    recusado(
        () -> validarAprovacaoDeVideo(video, StatusCriador.APROVADA, INICIO, INICIO, FIM),
        Recusa.VIDEO_NAO_EM_ANALISE);
  }

  @Test
  void recusaAprovarVideoDeCriadorBloqueado() {
    recusado(
        () ->
            validarAprovacaoDeVideo(
                StatusVideo.EM_ANALISE, StatusCriador.BLOQUEADA, INICIO, INICIO, FIM),
        Recusa.CRIADOR_BLOQUEADO);
  }

  @ParameterizedTest
  @EnumSource(value = StatusVideo.class, names = "APROVADO", mode = EnumSource.Mode.EXCLUDE)
  void recusaVideoQueNaoEstaAprovado(StatusVideo video) {
    recusado(() -> validarVideoAprovado(video), Recusa.VIDEO_NAO_APROVADO);
  }

  @ParameterizedTest
  @EnumSource(value = StatusVideo.class, names = "EM_ANALISE", mode = EnumSource.Mode.EXCLUDE)
  void recusaRecusarVideoQueNaoEstaEmAnalise(StatusVideo video) {
    // O aprovado não volta atrás: sai do ranking só pela desqualificação (RN-37, RN-38).
    recusado(() -> validarRecusaDeVideo(video), Recusa.VIDEO_NAO_EM_ANALISE);
  }

  private static void aprovar(LocalDate publicacao) {
    validarAprovacaoDeVideo(
        StatusVideo.EM_ANALISE, StatusCriador.APROVADA, publicacao, INICIO, FIM);
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
