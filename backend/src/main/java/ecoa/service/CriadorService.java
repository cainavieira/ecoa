package ecoa.service;

import ecoa.dto.CriadorDto;
import ecoa.entity.ContaBloqueadaApagada;
import ecoa.entity.Criador;
import ecoa.mapper.CriadorMapper;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaConta;
import ecoa.regras.StatusCriador;
import ecoa.repository.ContaBloqueadaApagadaRepository;
import ecoa.repository.CriadorRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriadorService {

  private final CriadorRepository criadores;
  private final ContaBloqueadaApagadaRepository apagadas;
  private final Clock relogio;

  public CriadorService(
      CriadorRepository criadores, ContaBloqueadaApagadaRepository apagadas, Clock relogio) {
    this.criadores = criadores;
    this.apagadas = apagadas;
    this.relogio = relogio;
  }

  /** Lido do banco a cada requisição: o status não vai no token, porque muda (ADR-0008). */
  @Transactional(readOnly = true)
  public boolean estaBloqueado(int idConta) {
    return criadores.existsByIdCriadorAndStatus(idConta, StatusCriador.BLOQUEADA);
  }

  /** A lista de contas, com todas as de criador (RF-15). */
  @Transactional(readOnly = true)
  public List<CriadorDto> listar() {
    return paraOperador(criadores.listarComConta());
  }

  /**
   * As contas de um status. A fila de verificação, a das pendentes, só tem quem já informou o @
   * (RF-11).
   */
  @Transactional(readOnly = true)
  public List<CriadorDto> listar(StatusCriador status) {
    List<Criador> doStatus = criadores.listarComConta(status);
    if (status == StatusCriador.PENDENTE) {
      doStatus = doStatus.stream().filter(criador -> criador.usuarioTiktok() != null).toList();
    }
    return paraOperador(doStatus);
  }

  /** Só a pendente que já informou o @, e com o @ livre de outra aprovada ou bloqueada (RN-13). */
  @Transactional
  public CriadorDto aprovar(int id) {
    Criador criador = criador(id);
    RegrasDaConta.validarAprovacaoDaConta(
        criador.status(),
        criador.usuarioTiktok() != null,
        criadores.statusDasOutrasComOUsuarioTiktok(criador.usuarioTiktok(), id));
    criador.aprovar();
    return paraOperador(criador);
  }

  /** Só a pendente que já informou o @; o motivo aparece na tela do criador (RN-13). */
  @Transactional
  public CriadorDto recusar(int id, String motivo) {
    Criador criador = criador(id);
    RegrasDaConta.validarRecusaDaConta(criador.status(), criador.usuarioTiktok() != null);
    criador.recusar(motivo);
    return paraOperador(criador);
  }

  /**
   * Só a aprovada é bloqueada (RN-17). O bloqueio vale na requisição seguinte do criador, porque o
   * status é lido do banco a cada uma (ADR-0008).
   */
  @Transactional
  public CriadorDto bloquear(int id, String motivo) {
    Criador criador = criador(id);
    RegrasDaConta.validarBloqueioDaConta(criador.status());
    criador.bloquear(motivo);
    return paraOperador(criador);
  }

  /** Só a bloqueada é desbloqueada, e volta a aprovada (RN-21). */
  @Transactional
  public CriadorDto desbloquear(int id, String motivo) {
    Criador criador = criador(id);
    RegrasDaConta.validarDesbloqueioDaConta(criador.status());
    criador.desbloquear(motivo);
    return paraOperador(criador);
  }

  /**
   * O operador corrige o @ da conta aprovada ou bloqueada, depois de conferir o código na bio do @
   * novo, que não pode ser de outra aprovada ou bloqueada (RN-15, UC-03 4a).
   */
  @Transactional
  public CriadorDto corrigirUsuarioTiktok(int id, String usuarioTiktok) {
    Criador criador = criador(id);
    String novo = RegrasDaConta.normalizarUsuarioTiktok(usuarioTiktok);
    RegrasDaConta.validarCorrecaoDoUsuarioTiktokPeloOperador(
        criador.status(), criadores.statusDasOutrasComOUsuarioTiktok(novo, id));
    criador.corrigirUsuarioTiktok(novo, criador.status());
    return paraOperador(criador);
  }

  private Criador criador(int id) {
    return criadores
        .findById(id)
        .orElseThrow(
            () ->
                new RegraVioladaException(
                    Recusa.NAO_ENCONTRADO, "Conta de criador não encontrada."));
  }

  /** O aviso de @ de conta bloqueada apagada vale para toda conta com esse @ (RN-65). */
  private List<CriadorDto> paraOperador(List<Criador> lista) {
    Map<String, LocalDate> apagadasPorUsuario =
        apagadas
            .findAllById(
                lista.stream().map(Criador::usuarioTiktok).filter(Objects::nonNull).toList())
            .stream()
            .collect(Collectors.toMap(ContaBloqueadaApagada::usuarioTiktok, this::dataDaExclusao));
    return lista.stream()
        .map(
            criador ->
                CriadorMapper.paraOperador(
                    criador, apagadasPorUsuario.get(criador.usuarioTiktok())))
        .toList();
  }

  private CriadorDto paraOperador(Criador criador) {
    return paraOperador(List.of(criador)).getFirst();
  }

  /** A data da exclusão no horário de Brasília (ADR-0014). */
  private LocalDate dataDaExclusao(ContaBloqueadaApagada apagada) {
    return LocalDate.ofInstant(apagada.apagadaEm(), relogio.getZone());
  }
}
