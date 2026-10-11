# ADR-0010 Bibliotecas do frontend herdadas do Tracking peso

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O frontend em React (ADR-0002) precisa de seis peças: estado de servidor,
rotas, formulários, tipos do contrato de API, componentes com estilo e gráfico.

O Tracking peso decidiu cada uma num ADR próprio, com alternativa recusada. O
Ecoa herda o conjunto, e o motivo de cada escolha vale igual aqui. Elas ficam
num ADR só porque só fazem sentido juntas e nenhuma mudou de razão.

## Decisão

| Peça | Escolha | Por quê |
|:--|:--|:--|
| Estado de servidor | TanStack Query; `useState` para estado de tela; nenhuma biblioteca de estado global | o que vem da api é cópia que envelhece. Depois de gravar (aprovar vídeo, registrar leitura), invalida-se uma chave e tudo que depende dela se atualiza |
| Rotas | React Router 8, em modo declarativo, pelo pacote `react-router` | poucas telas, numa SPA estática; o estado de servidor já tem dono |
| Formulário | React Hook Form, com Zod declarando a forma dos campos | a contabilidade de campo tocado, erro e envio fica com a biblioteca, e o Zod produz o tipo e a verificação da mesma declaração |
| Tipos da API | gerados do OpenAPI que o springdoc publica, versionados | o tipo é tradução do contrato, não segunda declaração dele |
| Componentes e estilo | Tailwind, com os componentes do shadcn/ui copiados para o repositório | comportamento acessível do Radix (diálogo, foco, Escape) e o layout sob controle, que é o carro-chefe (ADR-0002) |
| Gráfico | Recharts | declarativo em React, com linha de referência pronta para marcar o fechamento sobre o gráfico (RN-49) |

## Consequências

Os componentes do shadcn/ui passam a ser código do projeto: correção feita na
origem não chega sozinha, e atualizar é recopiar quando houver motivo.

O `<BrowserRouter>` exige que o servidor entregue `index.html` para todo
caminho que não é arquivo, papel do Caddy (ADR-0015).

A geração dos tipos é um passo que roda quando o contrato muda.

## Alternativa recusada: fetch com useState, ou Redux e Zustand

Buscar com `useEffect` deixa a invalidação em cascata na mão de quem escreve,
onde o erro é silencioso: a tela mostra número plausível e velho. Redux e
Zustand servem para estado de tela compartilhado, que aqui quase não existe, e
usá-los para estado de servidor é escrever o cache à mão.

## Alternativa recusada: modo framework do React Router

É a recomendação oficial para projeto novo. Recusado porque os carregadores de
rota buscam dado, e o estado de servidor já pertence ao TanStack Query: seriam
duas mecânicas respondendo à mesma pergunta.

## Alternativa recusada: formulário controlado à mão

Deixa a contabilidade de erro e envio na mão de quem escreve, que é onde ela sai
pela metade.

## Alternativa recusada: tipos da API escritos à mão

Duas declarações da mesma verdade, sem nada que force as duas a concordarem. A
divergência aparece em produção, e não na compilação.

## Alternativa recusada: biblioteca de componentes pronta, como MUI

Traria um sistema de design inteiro, e brigaria com ele quando o layout que se
quer não coubesse no molde.

## Alternativa recusada: Chart.js

Desenha em canvas e se configura por objeto de opções, desconfortável quando se
quer anotação sobre o gráfico.
