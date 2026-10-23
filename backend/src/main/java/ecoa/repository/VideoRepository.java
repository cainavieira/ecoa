package ecoa.repository;

import ecoa.entity.Video;
import ecoa.regras.StatusVideo;
import ecoa.regras.VideoAprovado;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<Video, Integer> {

  /** A data de publicação dos vídeos da campanha num status, que a mudança de datas confere. */
  @Query(
      "select new ecoa.regras.VideoAprovado(v.idVideo, v.dataPublicacao) from Video v"
          + " where v.participacao.campanha.idCampanha = :idCampanha and v.status = :status")
  List<VideoAprovado> publicacoesNaCampanha(Integer idCampanha, StatusVideo status);

  /** Os status dos envios do link, em todas as campanhas (RN-35). */
  @Query("select v.status from Video v where v.link = :link")
  List<StatusVideo> statusDosEnvios(String link);

  /** Os vídeos do criador na campanha, do envio mais novo para o mais antigo (ADR-0009). */
  @Query(
      "select v from Video v where v.participacao.campanha.idCampanha = :idCampanha"
          + " and v.participacao.criador.idCriador = :idCriador order by v.idVideo desc")
  List<Video> doCriadorNaCampanha(Integer idCampanha, Integer idCriador);

  Optional<Video> findByLinkAndStatusNot(String link, StatusVideo status);

  /**
   * Grava o vídeo em análise, ou nada, se o link já está noutro envio não recusado, e devolve
   * quantas linhas entraram. O segundo envio do duplo clique espera o primeiro terminar e não
   * grava, em vez de falhar no índice único do link (RN-35).
   */
  @Modifying
  @Query(
      value =
          "insert into video (xid_participacao, link, status, enviado_em)"
              + " values (:idParticipacao, :link, 'em_analise', :enviadoEm)"
              + " on conflict (link) where status <> 'recusado' do nothing",
      nativeQuery = true)
  int enviar(Integer idParticipacao, String link, Instant enviadoEm);
}
