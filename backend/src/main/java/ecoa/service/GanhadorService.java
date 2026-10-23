package ecoa.service;

import ecoa.dto.GanhadorDto;
import ecoa.dto.ParticipanteDto;
import ecoa.entity.Campanha;
import ecoa.entity.Criador;
import ecoa.entity.Ganhador;
import ecoa.entity.Participacao;
import ecoa.mapper.ResultadoMapper;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDoGanhador;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.CriadorRepository;
import ecoa.repository.GanhadorRepository;
import ecoa.repository.ParticipacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Os ganhadores, que o operador registra a qualquer momento, inclusive depois do encerramento
 * (RN-28, RN-52, RN-53).
 */
@Service
public class GanhadorService {

  private final CampanhaRepository campanhas;
  private final CriadorRepository criadores;
  private final ParticipacaoRepository participacoes;
  private final GanhadorRepository ganhadores;
  private final Clock relogio;

  public GanhadorService(
      CampanhaRepository campanhas,
      CriadorRepository criadores,
      ParticipacaoRepository participacoes,
      GanhadorRepository ganhadores,
      Clock relogio) {
    this.campanhas = campanhas;
    this.criadores = criadores;
    this.participacoes = participacoes;
    this.ganhadores = ganhadores;
    this.relogio = relogio;
  }

  /** Os participantes com conta, inclusive quem não tem vídeo que conta (RF-35). */
  @Transactional(readOnly = true)
  public List<ParticipanteDto> participantes(int idCampanha) {
    campanha(idCampanha);
    return participacoes.comContaNaCampanha(idCampanha).stream()
        .map(
            participacao -> {
              Criador criador = participacao.criador();
              return new ParticipanteDto(
                  criador.idCriador(), criador.usuarioTiktok(), criador.status());
            })
        .toList();
  }

  /**
   * O participante não bloqueado ganha, a partir do início da campanha, quantos prêmios o
   * regulamento der (RN-52).
   */
  @Transactional
  public GanhadorDto registrar(int idCampanha, int idCriador, String premio) {
    Campanha campanha = campanha(idCampanha);
    Criador criador =
        criadores
            .findById(idCriador)
            .orElseThrow(
                () ->
                    new RegraVioladaException(
                        Recusa.NAO_ENCONTRADO, "Conta de criador não encontrada."));
    Optional<Participacao> participacao =
        participacoes.findByCampanhaIdCampanhaAndCriadorIdCriador(idCampanha, idCriador);
    RegrasDoGanhador.validarGanhador(
        criador.status(),
        participacao.isPresent(),
        campanha.datas().inicio(),
        LocalDate.now(relogio));
    return ResultadoMapper.ganhador(
        ganhadores.save(new Ganhador(participacao.orElseThrow(), premio, Instant.now(relogio))));
  }

  /** A data de entrega não é futura (RN-52). */
  @Transactional
  public GanhadorDto informarEntrega(int id, LocalDate entregueEm) {
    Ganhador ganhador = ganhador(id);
    RegrasDoGanhador.validarEntregaDoPremio(entregueEm, LocalDate.now(relogio));
    ganhador.registrarEntrega(entregueEm);
    return ResultadoMapper.ganhador(ganhador);
  }

  /** O operador apaga o registro a qualquer momento (RN-53). */
  @Transactional
  public void apagar(int id) {
    ganhadores.delete(ganhador(id));
  }

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }

  private Ganhador ganhador(int id) {
    return ganhadores
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Ganhador não encontrado."));
  }
}
