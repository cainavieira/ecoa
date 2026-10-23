package ecoa.service;

import ecoa.dto.CampanhaDto;
import ecoa.dto.CampanhaResumoDto;
import ecoa.dto.NovaCampanhaDto;
import ecoa.entity.Campanha;
import ecoa.mapper.CampanhaMapper;
import ecoa.regras.DatasDaCampanha;
import ecoa.regras.Funcao;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaCampanha;
import ecoa.regras.StatusCampanha;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.ParticipacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampanhaService {

  private final CampanhaRepository campanhas;
  private final ParticipacaoRepository participacoes;
  private final Clock relogio;

  public CampanhaService(
      CampanhaRepository campanhas, ParticipacaoRepository participacoes, Clock relogio) {
    this.campanhas = campanhas;
    this.participacoes = participacoes;
    this.relogio = relogio;
  }

  /**
   * Todas as campanhas, da mais nova para a mais antiga, com o status de hoje. Para o criador,
   * marca as de que ele participa (RF-22).
   */
  @Transactional(readOnly = true)
  public List<CampanhaResumoDto> listar(int idConta, Funcao funcao) {
    LocalDate hoje = LocalDate.now(relogio);
    Set<Integer> dele =
        funcao == Funcao.CRIADOR
            ? new HashSet<>(participacoes.campanhasDoCriador(idConta))
            : Set.of();
    return campanhas.findAllByOrderByIdCampanhaDesc().stream()
        .map(
            campanha ->
                CampanhaMapper.resumo(
                    campanha,
                    status(campanha, hoje),
                    funcao == Funcao.CRIADOR ? dele.contains(campanha.idCampanha()) : null))
        .toList();
  }

  /** A página da campanha, que o criador vê participando ou não (RF-23, UC-08 1a). */
  @Transactional(readOnly = true)
  public CampanhaDto mostrar(int id, int idConta, Funcao funcao) {
    Campanha campanha = campanha(id);
    Boolean participa =
        funcao == Funcao.CRIADOR
            ? participacoes.existsByCampanhaIdCampanhaAndCriadorIdCriador(id, idConta)
            : null;
    return CampanhaMapper.paraDto(campanha, status(campanha, LocalDate.now(relogio)), participa);
  }

  /**
   * Com as datas em ordem e o fechamento futuro (RN-24, RN-26). Uma campanha não encerrada a mais é
   * aceita: uma por vez é premissa de operação, não regra (RN-29).
   */
  @Transactional
  public CampanhaDto criar(NovaCampanhaDto nova) {
    LocalDate hoje = LocalDate.now(relogio);
    DatasDaCampanha datas =
        new DatasDaCampanha(nova.dataInicio(), nova.dataFim(), nova.dataFechamento());
    RegrasDaCampanha.validarDatasDaCampanha(datas, hoje);
    Campanha campanha =
        new Campanha(
            nova.musica(), nova.artista(), nova.regulamento(), datas, Instant.now(relogio));
    campanha.trocarLinks(CampanhaMapper.links(campanha, nova.links()));
    return CampanhaMapper.paraDto(campanhas.save(campanha), status(campanha, hoje), null);
  }

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }

  private static StatusCampanha status(Campanha campanha, LocalDate hoje) {
    DatasDaCampanha datas = campanha.datas();
    return RegrasDaCampanha.statusDaCampanha(
        datas.inicio(), datas.fim(), campanha.encerrada(), hoje);
  }
}
