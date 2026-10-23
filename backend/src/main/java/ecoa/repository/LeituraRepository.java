package ecoa.repository;

import ecoa.entity.Leitura;
import ecoa.entity.LeituraId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeituraRepository extends JpaRepository<Leitura, LeituraId> {

  /** As leituras de vários vídeos numa consulta só, da data mais antiga para a mais nova. */
  @Query("select l from Leitura l where l.video.idVideo in :idsDosVideos order by l.id.data")
  List<Leitura> dosVideos(Collection<Integer> idsDosVideos);
}
