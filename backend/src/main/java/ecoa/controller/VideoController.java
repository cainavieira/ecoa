package ecoa.controller;

import ecoa.dto.AprovacaoDto;
import ecoa.dto.MeuVideoDto;
import ecoa.dto.MotivoDto;
import ecoa.dto.NovoVideoDto;
import ecoa.dto.VideoDto;
import ecoa.regras.StatusVideo;
import ecoa.service.VideoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Os vídeos: o envio e os do próprio criador, sempre da conta do token (UC-09, UC-13, ADR-0009), e
 * a conferência pelo operador (UC-10, UC-12).
 */
@RestController
public class VideoController {

  private final VideoService videos;

  public VideoController(VideoService videos) {
    this.videos = videos;
  }

  @PostMapping("/campanhas/{id}/videos")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('CRIADOR')")
  public MeuVideoDto enviar(
      @AuthenticationPrincipal Jwt token,
      @PathVariable int id,
      @Valid @RequestBody NovoVideoDto corpo) {
    return videos.enviar(id, ContaDoToken.id(token), corpo.link());
  }

  @GetMapping("/campanhas/{id}/meus-videos")
  @PreAuthorize("hasRole('CRIADOR')")
  public List<MeuVideoDto> meusVideos(@AuthenticationPrincipal Jwt token, @PathVariable int id) {
    return videos.meusVideos(id, ContaDoToken.id(token));
  }

  /** Sem status, todos os vídeos; com ?status=em_analise, a fila de conferência. */
  @GetMapping("/videos")
  @PreAuthorize("hasRole('OPERADOR')")
  public List<VideoDto> listar(@RequestParam(required = false) StatusVideo status) {
    return status == null ? videos.listar() : videos.listar(status);
  }

  @PostMapping("/videos/{id}/aprovacao")
  @PreAuthorize("hasRole('OPERADOR')")
  public VideoDto aprovar(@PathVariable int id, @Valid @RequestBody AprovacaoDto corpo) {
    return videos.aprovar(id, corpo.dataPublicacao(), corpo.views());
  }

  @PostMapping("/videos/{id}/recusa")
  @PreAuthorize("hasRole('OPERADOR')")
  public VideoDto recusar(@PathVariable int id, @Valid @RequestBody MotivoDto corpo) {
    return videos.recusar(id, corpo.motivo());
  }

  @PostMapping("/videos/{id}/desqualificacao")
  @PreAuthorize("hasRole('OPERADOR')")
  public VideoDto desqualificar(@PathVariable int id, @Valid @RequestBody MotivoDto corpo) {
    return videos.desqualificar(id, corpo.motivo());
  }
}
