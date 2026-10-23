package ecoa.dto;

import java.time.LocalDate;

/**
 * Um ganhador registrado: o participante, o prêmio em texto e a data de entrega, nula até a entrega
 * (RN-51, RN-54). O @ é nulo para o participante removido (RN-59).
 */
public record GanhadorDto(int id, String usuarioTiktok, String premio, LocalDate entregueEm) {}
