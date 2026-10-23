package ecoa.repository;

import ecoa.entity.Leitura;
import ecoa.entity.LeituraId;
import ecoa.regras.LeituraDoVideo;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeituraRepository extends JpaRepository<Leitura, LeituraId> {

  /** As leituras de vários vídeos numa consulta só, da data mais antiga para a mais nova. */
  @Query("select l from Leitura l where l.video.idVideo in :idsDosVideos order by l.id.data")
  List<Leitura> dosVideos(Collection<Integer> idsDosVideos);

  Optional<Leitura> findByVideoIdVideoAndIdData(Integer idVideo, LocalDate data);

  /** As leituras de cada vídeo, como a regra as recebe, da mais antiga para a mais nova. */
  default Map<Integer, List<LeituraDoVideo>> porVideo(Collection<Integer> idsDosVideos) {
    if (idsDosVideos.isEmpty()) {
      return Map.of();
    }
    return dosVideos(idsDosVideos).stream()
        .collect(
            Collectors.groupingBy(
                Leitura::idVideo,
                Collectors.mapping(
                    leitura -> new LeituraDoVideo(leitura.data(), leitura.views()),
                    Collectors.toList())));
  }
}
