package ecoa.regras;

import java.util.List;

public record ParticipanteDaCampanha(
    int idParticipacao, StatusCriador criador, List<VideoDaCampanha> videos) {}
