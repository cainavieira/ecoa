# Auditoria de coerência

- **Data:** 11-10-2026
- **Modo:** desde `7293229`
- **Auditado:** `7293229..cd8d376`, o caminho (c) do achado 2 de 10-10
- **Correções:** `1f56b37` e o commit deste relatório
- **Estado:** três problemas confirmados, todos decididos e aplicados

Quatro agentes: um auditor e um cético para cada metade dos 15 itens
alterados. Os auditores acharam 7 defeitos; o cético derrubou 3. Os 4
confirmados se reduzem a 3 problemas, porque um apareceu nas duas metades.
Cada um foi conferido nos arquivos antes de entrar aqui.

## Confirmados

| Problema | Decisão |
|:--|:--|
| A RN-65 avisava só na conta pendente. Quem voltava com outro @, era aprovado e pedia a correção para o @ bloqueado (RN-15, UC-03 4a) chegava a ele sem aviso | O aviso vale para toda conta com @ do registro, em qualquer situação, na fila e na lista de contas, e o operador decide se bloqueia. O registro deixa de ter prazo, porque guarda só o @. RN-57, RN-60, RN-65, RF-15, RNF-11, UC-03 1a e 4c, UC-19 3e e pós-condição, contrato, esquema, T-36 |
| Com o link apagado, a RN-35 não alcança quem apaga a conta e volta com o mesmo @: ele reenvia o link numa segunda campanha aberta ou, na campanha aberta, o link desqualificado, porque ali a RN-59 já apagava o vídeo | Custo aceito, nas Decisões recusadas: guardar o link desfaria a exclusão |
| O desenho do esquema lista dez tabelas, e o texto dizia nove. A contagem nasceu errada (oito com nove tabelas), e o cd8d376 somou um | "Dez tabelas" no `instrucao.md` e na T-03 |

## Derrubados pelo cético

- A RN-28 diz que vídeo de campanha encerrada não muda, e a RN-59 apaga o
  link. A exceção está escrita na própria RN-59.
- O UC-13 não tem o participante removido. É completude, não contradição: o
  contrato e o UC-16 2c já dizem.
- Participante removido registrado como ganhador depois do encerramento. O
  estado já é aceito pelo UC-15 4b.

## Observação

O cético sugeriu, como melhoria opcional, um fluxo alternativo no UC-13 para o
participante removido, antes do design das telas (T-25).
