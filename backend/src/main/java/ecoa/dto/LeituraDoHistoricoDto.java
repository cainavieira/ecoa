package ecoa.dto;

import java.time.LocalDate;

/**
 * Uma leitura no histórico do vídeo, com a marca da de fechamento, que a regra deriva da data
 * (RN-41): para trocar a data dela, o operador a apaga antes.
 */
public record LeituraDoHistoricoDto(LocalDate data, long views, boolean deFechamento) {}
