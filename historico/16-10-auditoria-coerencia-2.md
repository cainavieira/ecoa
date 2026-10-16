# Auditoria de coerência

- **Data:** 16-10-2026
- **Modo:** desde `64d5399`, limitada ao que mudou até `7293229`
- **Auditado:** as correções da auditoria completa de 10-10, o conserto do
  ADR-0009 e o adiamento dos termos, que nenhuma auditoria tinha cruzado com o
  resto
- **Correções:** `a7881b3` e o commit deste relatório
- **Estado:** três achados confirmados, decididos e aplicados

Quatro agentes: um auditor e um cético para cada metade dos 20 itens alterados
no intervalo. O alcance foi medido numa cópia do repositório parada em
`7293229`; as três seções de aposentados, vazias, ficaram de fora. Os
auditores acharam 6 defeitos; o cético derrubou 3. Cada achado foi conferido
nos arquivos antes de entrar aqui.

## Confirmados

| Problema | Decisão |
|:--|:--|
| A RN-15 passou a exigir que o operador confira o código da conta na bio do @ novo, mas o código só chegava a ele na fila de pendentes, e não na lista de contas onde a correção acontece | O código vai também na lista de contas: RF-15, `GET /criadores`, convenção do backend e ADR-0009, este corrigido no lugar por exceção do interessado |
| Com a correção do @ exigindo o código, a conta aprovada ou bloqueada que ficou com o @ de outra pessoa o prendia para sempre: bloquear mantém o @, e apagar só a pedido do dono. O porquê da derrubada de 10-10 ("a RF-14 libera") caiu com a própria correção | O operador pode apagar, por iniciativa própria, a conta cujo @ outra conta provou ser dela (RN-58, UC-03 3a, UC-19). Tirar só o @ e manter a conta entrou nas Decisões recusadas |
| A RNF-08 manda o ensaio do deploy recusar dump com mais de um dia, e o passo 2 do deploy e a T-38 não diziam isso | Passo 2 do `fluxo-de-trabalho.md` com a recusa, e a T-38 citando a RNF-08 |

## Derrubados pelo cético

- A participação idempotente dependeria do tratador global de erro. O service
  pode tratar a duplicata sozinho.
- Subir o perfil de desenvolvimento na integração contínua para gerar os tipos
  não está escrito. Detalhe de implementação, da T-02 ou da T-27.
- A T-02 ligava a regeneração dos tipos ao fim da T-26, e os tipos nascem na
  T-27. Número trocado, sem contradição; corrigido junto, por decisão do
  interessado.
