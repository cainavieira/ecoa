package ecoa.regras;

import java.util.List;

public record VideoDaCampanha(int idVideo, StatusVideo status, List<LeituraDoVideo> leituras) {}
