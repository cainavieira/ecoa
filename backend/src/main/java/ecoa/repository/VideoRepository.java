package ecoa.repository;

import ecoa.entity.Video;
import ecoa.regras.StatusVideo;
import ecoa.regras.VideoAprovado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<Video, Integer> {

  /** A data de publicação dos vídeos da campanha num status, que a mudança de datas confere. */
  @Query(
      "select new ecoa.regras.VideoAprovado(v.idVideo, v.dataPublicacao) from Video v"
          + " where v.participacao.campanha.idCampanha = :idCampanha and v.status = :status")
  List<VideoAprovado> publicacoesNaCampanha(Integer idCampanha, StatusVideo status);
}
