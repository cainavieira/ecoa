# Ecoa

Plataforma que conecta uma gravadora a criadores de conteúdo que postam vídeos
com a música dela. O **MVP é só TikTok**, com as views lidas à mão pela
operação, uma gravadora, uma campanha por vez e até 50 criadores. O sistema
mede e mostra; o prêmio é texto, e a gravadora paga direto ao ganhador.

**Estado: a especificação do MVP está completa, e a construção começou; o
andamento está no `tarefas.md`.**

## Onde está a verdade

`especificacao/` é a fonte. Nenhum comportamento se decide fora dela. A
`visao/` descreve a ideia inteira, quase toda depois do MVP, e não decide
comportamento.

Os identificadores são estáveis e se citam entre documentos: `RN-41`, `RF-28`,
`RNF-07`, `UC-10`, `ADR-0001`. **Número aposentado nunca volta**, e cada
documento tem seção listando os seus aposentados com o motivo.

## Antes de propor qualquer coisa

`especificacao/adr/` tem 18 decisões, cada uma com a alternativa que perdeu e o
motivo. A seção **Decisões recusadas** do `regras-de-negocio.md` faz o mesmo
para regra de produto. O `grill-mvp.md` registra como o MVP foi recortado.

Se você sentir vontade de propor a API do TikTok no MVP, login pelo TikTok,
upload de arquivo, cálculo automático de prêmio, CPF ou Pix no sistema, e-mail,
Kubernetes ou backup na nuvem, a recusa já está escrita com o motivo. Decisão
nova é do interessado, nunca sua.

## O padrão que acha defeito

Defeito de coerência não aparece lendo um documento sozinho. Aparece quando dois
ficam lado a lado: duas regras corretas separadas produzindo um sistema errado
juntas, uma condição que parecia suficiente e não era.

Ao mexer numa regra, leia todos os documentos que a citam, contra ela
(`fluxo-de-trabalho.md`). A skill local `auditar-coerencia` faz isso em escala.

## Como trabalhar aqui

- Trabalhe direto, uma tarefa atrás da outra, cada uma no seu ramo, e commite
  sem pedir aprovação. Pare só para decisão dele ou para o que só ele faz. Push,
  PR e merge se resolvem com ele no fim da leva. **Push é sempre dele.**
- Não busque na web sem ele pedir.
- Teste só entra se pega defeito real, e quem o propõe diz qual em uma
  linha. O critério inteiro está na seção Testes do `fluxo-de-trabalho.md`.
- Sem travessão, em documento, commit ou conversa.
- Mensagem de commit sem acento; documento com acento.
- `especificacao/` é versionada de propósito. `.env`, `target/`,
  `node_modules/`, `.claude/` e `notas-privadas.md` não entram.
- O repositório é público (ADR-0018): nada sobre a negociação entre os sócios
  entra em arquivo versionado.
