package ecoa.dto;

import java.util.List;

/**
 * O resultado da campanha numa resposta só, para total, ranking e gráfico saírem das mesmas
 * leituras (endpoints.md): o ranking, o total na data de hoje, as contagens, a série do gráfico, os
 * ganhadores e se o ranking é o final (RN-45 a RN-49, RN-54, RN-61). Nenhum campo tem e-mail ou
 * código de verificação (RN-57).
 */
public record ResultadoDto(
    boolean rankingFinal,
    long totalHoje,
    int criadoresNoRanking,
    int videosQueContam,
    List<PosicaoDto> ranking,
    List<PontoDoGraficoDto> grafico,
    List<GanhadorDto> ganhadores) {}
