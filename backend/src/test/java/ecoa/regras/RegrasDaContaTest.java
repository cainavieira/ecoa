package ecoa.regras;

import static ecoa.regras.RegrasDaConta.normalizarEmail;
import static ecoa.regras.RegrasDaConta.normalizarUsuarioTiktok;
import static ecoa.regras.RegrasDaConta.validarAprovacaoDaConta;
import static ecoa.regras.RegrasDaConta.validarBloqueioDaConta;
import static ecoa.regras.RegrasDaConta.validarCorrecaoDoUsuarioTiktokPeloCriador;
import static ecoa.regras.RegrasDaConta.validarCorrecaoDoUsuarioTiktokPeloOperador;
import static ecoa.regras.RegrasDaConta.validarDesbloqueioDaConta;
import static ecoa.regras.RegrasDaConta.validarRecusaDaConta;
import static ecoa.regras.RegrasDaConta.validarRemocaoDeAcesso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RegrasDaContaTest {

  @Test
  void emailFicaEmMinusculas() {
    assertThat(normalizarEmail("Fulano.Silva@Gmail.COM")).isEqualTo("fulano.silva@gmail.com");
  }

  @ParameterizedTest
  @CsvSource({"@Fulano_Silva, fulano_silva", "@@fulano, fulano", "'  @fulano ', fulano"})
  void usuarioTiktokPerdeOArrobaEAsMaiusculas(String digitado, String guardado) {
    assertThat(normalizarUsuarioTiktok(digitado)).isEqualTo(guardado);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "@", "   ", "fulano silva", "@ fulano", "fulano\tsilva"})
  void recusaUsuarioTiktokVazioOuComEspacoNoMeio(String digitado) {
    recusado(() -> normalizarUsuarioTiktok(digitado), Recusa.USUARIO_TIKTOK_INVALIDO);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"PENDENTE", "RECUSADA"})
  void criadorCorrigeOUsuarioTiktokDaContaPendenteOuRecusada(StatusCriador conta) {
    assertThatCode(() -> validarCorrecaoDoUsuarioTiktokPeloCriador(conta))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"APROVADA", "BLOQUEADA"})
  void recusaCriadorCorrigindoOUsuarioTiktokDaContaAprovadaOuBloqueada(StatusCriador conta) {
    recusado(() -> validarCorrecaoDoUsuarioTiktokPeloCriador(conta), Recusa.USUARIO_TIKTOK_FIXO);
  }

  @ParameterizedTest
  @EnumSource(value = StatusCriador.class, names = "PENDENTE", mode = EnumSource.Mode.EXCLUDE)
  void recusaAprovarContaQueNaoEstaPendente(StatusCriador conta) {
    recusado(() -> validarAprovacaoDaConta(conta, true, List.of()), Recusa.CONTA_NAO_PENDENTE);
  }

  @ParameterizedTest
  @EnumSource(value = StatusCriador.class, names = "PENDENTE", mode = EnumSource.Mode.EXCLUDE)
  void recusaRecusarContaQueNaoEstaPendente(StatusCriador conta) {
    recusado(() -> validarRecusaDaConta(conta, true), Recusa.CONTA_NAO_PENDENTE);
  }

  @Test
  void recusaAprovarOuRecusarContaSemUsuarioTiktok() {
    recusado(
        () -> validarAprovacaoDaConta(StatusCriador.PENDENTE, false, List.of()),
        Recusa.CONTA_SEM_USUARIO_TIKTOK);
    recusado(
        () -> validarRecusaDaConta(StatusCriador.PENDENTE, false), Recusa.CONTA_SEM_USUARIO_TIKTOK);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"APROVADA", "BLOQUEADA"})
  void recusaAprovarComUsuarioTiktokDeOutraContaAprovadaOuBloqueada(StatusCriador outra) {
    recusado(
        () -> validarAprovacaoDaConta(StatusCriador.PENDENTE, true, List.of(outra)),
        Recusa.USUARIO_TIKTOK_EM_USO);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"PENDENTE", "RECUSADA"})
  void aceitaAprovarComUsuarioTiktokDeOutraContaPendenteOuRecusada(StatusCriador outra) {
    // Duas contas com o mesmo @: aprova-se a que tem o código na bio (UC-03 3b).
    assertThatCode(() -> validarAprovacaoDaConta(StatusCriador.PENDENTE, true, List.of(outra)))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"APROVADA", "BLOQUEADA"})
  void operadorCorrigeOUsuarioTiktokDaContaAprovadaOuBloqueada(StatusCriador conta) {
    assertThatCode(() -> validarCorrecaoDoUsuarioTiktokPeloOperador(conta, List.of()))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusCriador.class,
      names = {"PENDENTE", "RECUSADA"})
  void recusaOperadorCorrigindoOUsuarioTiktokDaContaPendenteOuRecusada(StatusCriador conta) {
    recusado(
        () -> validarCorrecaoDoUsuarioTiktokPeloOperador(conta, List.of()),
        Recusa.CONTA_NAO_APROVADA);
  }

  @Test
  void recusaOperadorCorrigindoParaUsuarioTiktokDeOutraContaBloqueada() {
    recusado(
        () ->
            validarCorrecaoDoUsuarioTiktokPeloOperador(
                StatusCriador.APROVADA, List.of(StatusCriador.BLOQUEADA)),
        Recusa.USUARIO_TIKTOK_EM_USO);
  }

  @ParameterizedTest
  @EnumSource(value = StatusCriador.class, names = "APROVADA", mode = EnumSource.Mode.EXCLUDE)
  void recusaBloquearContaQueNaoEstaAprovada(StatusCriador conta) {
    recusado(() -> validarBloqueioDaConta(conta), Recusa.CONTA_NAO_APROVADA);
  }

  @ParameterizedTest
  @EnumSource(value = StatusCriador.class, names = "BLOQUEADA", mode = EnumSource.Mode.EXCLUDE)
  void recusaDesbloquearContaQueNaoEstaBloqueada(StatusCriador conta) {
    recusado(() -> validarDesbloqueioDaConta(conta), Recusa.CONTA_NAO_BLOQUEADA);
  }

  @Test
  void recusaRemoverAPropriaConta() {
    recusado(() -> validarRemocaoDeAcesso(1, 1, Funcao.OPERADOR, 2), Recusa.PROPRIA_CONTA);
  }

  @Test
  void aceitaRemoverOOutroOperadorQuandoHaDois() {
    assertThatCode(() -> validarRemocaoDeAcesso(1, 2, Funcao.OPERADOR, 2))
        .doesNotThrowAnyException();
  }

  @Test
  void recusaRemoverOUltimoOperador() {
    // Dois operadores removendo um ao outro ao mesmo tempo: o segundo já conta um só (UC-18 4d).
    recusado(() -> validarRemocaoDeAcesso(2, 1, Funcao.OPERADOR, 1), Recusa.ULTIMO_OPERADOR);
  }

  @Test
  void aceitaRemoverGravadoraComUmOperadorSo() {
    assertThatCode(() -> validarRemocaoDeAcesso(1, 5, Funcao.GRAVADORA, 1))
        .doesNotThrowAnyException();
  }

  private static void recusado(ThrowingCallable chamada, Recusa recusa) {
    assertThatThrownBy(chamada)
        .isInstanceOfSatisfying(
            RegraVioladaException.class, e -> assertThat(e.recusa()).isEqualTo(recusa));
  }
}
