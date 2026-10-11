# ADR-0003 Monorepo com backend, frontend e documentação

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O sistema tem duas camadas que conversam por um contrato de API (ADR-0002), uma
especificação que descreve as duas, e uma visão do produto que vai além do MVP.
Uma pessoa desenvolve.

## Decisão

Um repositório só, com `backend/`, `frontend/`, `especificacao/`, `visao/` e
`historico/` lado a lado.

## Consequências

Mudança que atravessa as camadas cabe num commit: acrescentar um campo mexe na
tabela, na rota, nos tipos gerados e na tela, e as alterações entram juntas ou
não entram.

A especificação fica versionada ao lado do código que ela descreve.

O histórico mistura backend, frontend e documentação. Aceito: o filtro por
caminho resolve quando fizer falta.

## Alternativa recusada: um repositório por camada

Separar paga quando os lados têm ciclo de vida independente, e aqui eles têm o
mesmo. Custaria dois pull requests por mudança de contrato, com ordem
obrigatória entre eles e uma janela no meio em que o sistema está quebrado.
