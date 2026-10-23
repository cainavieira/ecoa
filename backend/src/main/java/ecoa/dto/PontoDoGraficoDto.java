package ecoa.dto;

import java.time.LocalDate;

/** Um ponto do gráfico da campanha: o total numa data com leitura (RN-49). */
public record PontoDoGraficoDto(LocalDate data, long total) {}
