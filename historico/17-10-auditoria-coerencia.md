# Auditoria de coerência

- **Data:** 17-10-2026
- **Modo:** desde `0956ec8`, até `2f98160`
- **Auditado:** os commits da T-03 até aqui: o critério de teste no CLAUDE.md,
  o ganhador sem campanha, a RN-30 no banco, as chaves `id_` e `xid_`, a
  primeira migração e a troca de papel por função e de situação por status
- **Correções:** `959fd79`, `16de3f7` e o commit deste relatório
- **Estado:** dois achados confirmados, os dois anteriores ao intervalo,
  decididos e aplicados

Oito agentes: um auditor e um cético para cada um dos quatro grupos dos 47
itens alterados (46 do `alcance.sh` e a seção Esquema do `instrucao.md`,
incluída à mão porque o script só olha a `especificacao/`). Os auditores
acharam 10 defeitos; o cético derrubou 8. Cada achado foi conferido nos
arquivos antes de entrar aqui.

## Confirmados

| Problema | Decisão |
|:--|:--|
| O UC-19 exige que o operador digite o e-mail da conta para confirmar a exclusão, mas nenhum contrato entregava o e-mail do criador ao operador (RF-15, `GET /criadores`, convenção do backend, ADR-0009). Na iniciativa própria da RN-58 (UC-03 3a), não havia pedido de onde tirá-lo, e o @ voltava a ficar preso. Nasceu em `a7881b3` | O e-mail vai também na lista de contas do operador: RF-15, `GET /criadores`, convenção do backend e ADR-0009, este corrigido no lugar por exceção do interessado |
| A convenção manda para o banco toda restrição que ele consegue impor, e o `instrucao.md` descreve o `email` em minúsculas, mas o esquema só impunha a unicidade: o banco aceitava `maria@x.com` e `Maria@X.com`, e `fulano`, `Fulano` e `@fulano` como três @ aprovados. Anterior ao intervalo; a migração `4b10667` levou a falha ao SQL | `CHECK` na própria migração, que nenhum banco persistente recebeu: e-mail em minúsculas; @ em minúsculas, sem arroba no começo, sem espaço e não vazio, no `criador` e na `conta_bloqueada_apagada`. A lista do `instrucao.md` registra os dois |

## Derrubados pelo cético

- O `2f98160` reescreveu o vocabulário de quatro ADR sem registrar a exceção
  no commit. Não é contradição entre documentos, e a exceção foi do
  interessado, sem registro por decisão dele.
- Apagar pela RN-58 a conta bloqueada errada grava, pela RN-65, o @ do dono
  legítimo. O aviso diz só que o @ foi de conta bloqueada, e quem decide é o
  operador.
- A convenção de chaves não cobriria `criador`, `leitura` e
  `conta_bloqueada_apagada` (dois achados). `id_criador` segue a regra da
  primária, e as chaves naturais estão escritas no esquema, na RN-40 e no
  `endpoints.md`.
- "Função e isolamento ficam na aplicação" passou a soar como a coluna
  `funcao`. A tensão já existia com "papel", antes do intervalo.
- O criador bloqueado que muda o @ receberia `CONTA_BLOQUEADA` e
  `USUARIO_TIKTOK_FIXO`. Já derrubado em 10-10: a precedência está no contrato.
- Prêmio novo para "participante removido" em campanha encerrada. Já
  derrubado em 11-10: o UC-15 4b aceita.
- "status" e "função" ganharam homônimos (código HTTP, função de código).
  São termos técnicos que já estavam lá, e a troca foi decisão do interessado.
