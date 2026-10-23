package ecoa.dto;

import java.time.LocalDate;

/** As views de um vídeo numa data (RN-39, RN-40). */
public record LeituraDto(LocalDate data, long views) {}
