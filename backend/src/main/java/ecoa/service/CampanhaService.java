package ecoa.service;

import static java.util.Objects.requireNonNullElse;

import ecoa.dto.CampanhaDto;
import ecoa.dto.CampanhaResumoDto;
import ecoa.dto.EdicaoDaCampanhaDto;
import ecoa.dto.NovaCampanhaDto;
import ecoa.entity.Campanha;
import ecoa.mapper.CampanhaMapper;
import ecoa.regras.DatasDaCampanha;
import ecoa.regras.Funcao;
import ecoa.regras.Recusa;
import ecoa.regras.RegraVioladaException;
import ecoa.regras.RegrasDaCampanha;
import ecoa.regras.StatusVideo;
import ecoa.repository.CampanhaRepository;
import ecoa.repository.ParticipacaoRepository;
import ecoa.repository.VideoRepository;
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
  private final VideoRepository videos;
  private final Clock relogio;

  public CampanhaService(
      CampanhaRepository campanhas,
      ParticipacaoRepository participacoes,
      VideoRepository videos,
      Clock relogio) {
    this.campanhas = campanhas;
    this.participacoes = participacoes;
    this.videos = videos;
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
                    campanha.status(hoje),
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
    return CampanhaMapper.paraDto(campanha, campanha.status(LocalDate.now(relogio)), participa);
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
    return CampanhaMapper.paraDto(campanhas.save(campanha), campanha.status(hoje), null);
  }

  /**
   * Muda o que veio, na campanha não encerrada (RN-28). As datas ficam em ordem, o fechamento que
   * muda é futuro, e nenhum vídeo aprovado sai do início e do fim (RN-24, RN-26, RN-27). O status
   * sai das datas novas: adiar o fim reabre o envio, e antecipá-lo o fecha (UC-06 2f, 2g).
   */
  @Transactional
  public CampanhaDto editar(int id, EdicaoDaCampanhaDto edicao) {
    Campanha campanha = campanha(id);
    LocalDate hoje = LocalDate.now(relogio);
    RegrasDaCampanha.validarMudancaNaCampanha(campanha.status(hoje));
    DatasDaCampanha atuais = campanha.datas();
    DatasDaCampanha novas =
        new DatasDaCampanha(
            requireNonNullElse(edicao.dataInicio(), atuais.inicio()),
            requireNonNullElse(edicao.dataFim(), atuais.fim()),
            requireNonNullElse(edicao.dataFechamento(), atuais.fechamento()));
    RegrasDaCampanha.validarDatasDaCampanha(
        atuais, novas, videos.publicacoesNaCampanha(id, StatusVideo.APROVADO), hoje);
    campanha.editar(
        requireNonNullElse(edicao.musica(), campanha.musica()),
        requireNonNullElse(edicao.artista(), campanha.artista()),
        requireNonNullElse(edicao.regulamento(), campanha.regulamento()),
        novas);
    if (edicao.links() != null) {
      campanha.trocarLinks(CampanhaMapper.links(campanha, edicao.links()));
    }
    return CampanhaMapper.paraDto(campanha, campanha.status(hoje), null);
  }

  /** Só a campanha sem participante, e os links saem com ela (RN-30). */
  @Transactional
  public void apagar(int id) {
    Campanha campanha = campanha(id);
    if (participacoes.existsByCampanhaIdCampanha(id)) {
      throw new RegraVioladaException(
          Recusa.CAMPANHA_COM_PARTICIPANTE, "A campanha tem participante e não pode ser apagada.");
    }
    campanhas.delete(campanha);
  }

  private Campanha campanha(int id) {
    return campanhas
        .findById(id)
        .orElseThrow(
            () -> new RegraVioladaException(Recusa.NAO_ENCONTRADO, "Campanha não encontrada."));
  }
}
