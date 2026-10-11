# Convenções do frontend

Regras de escrita, não de negócio. As decisões de ferramenta estão nos ADR. O
formato segue o contrato de arquitetura do paineis-kanban: stack com versão,
pastas, dependência, regras, nomes, testes e proibido.

## 1. Stack (obrigatória)

Conferida em 10/10/2026, nas fontes oficiais: registro do npm e nodejs.org.

| Camada | Tecnologia |
|:--|:--|
| Linguagem | TypeScript 5.9, com `strict` |
| Ambiente de build | Node.js 24 (LTS) |
| Build | Vite 8.3, com @vitejs/plugin-react 6.1 |
| UI | React 19.3 |
| Rotas | React Router 8.4, pelo pacote `react-router` |
| Dado vindo da API | TanStack Query 5 |
| Formulário | React Hook Form 7.89, com @hookform/resolvers 5.9 e Zod 4.6 |
| Tipos da API | gerados do OpenAPI com openapi-typescript 7.13 |
| Estilo | Tailwind CSS 4.3 |
| Componentes | shadcn/ui, copiados pela CLI `shadcn` 4.21 |
| Gráfico | Recharts 3.10 |
| Testes | Vitest 5.0, com Testing Library 16.3; Playwright 1.64 no teste de fumaça |
| Lint | ESLint 10.12, com typescript-eslint 8.71 |

**Por que o TypeScript não é o 7.** A versão mais recente é a 7.0, mas o
typescript-eslint 8.71 aceita TypeScript abaixo de 6.1, e o openapi-typescript
7.13 pede a linha 5. A 5.9 é a mais nova que os dois aceitam. Subir para a 7
espera os dois acompanharem.

Atualizar uma versão é mudança desta tabela, no mesmo commit que a muda no
`package.json`, e confere as dependências entre pares, como acima.

## 2. Pastas

    frontend/
    ├── package.json        dependências e scripts
    ├── Dockerfile          build do Vite e Caddy com o resultado
    └── src/
        ├── api/            cliente HTTP único e os tipos gerados do OpenAPI
        ├── components/ui/  componentes do shadcn/ui, copiados pela CLI
        ├── features/
        │   └── <feature>/  uma por assunto: entrada, conta, campanhas,
        │       │           videos, leituras, resultado, ganhadores,
        │       │           criadores, acessos
        │       ├── api.ts                  chamadas das rotas da feature
        │       ├── useResultado.ts         gancho com TanStack Query
        │       ├── TabelaRanking.tsx       componente de tela
        │       └── TabelaRanking.test.tsx  teste, ao lado do arquivo
        └── shared/         o que mais de uma feature usa: formatação de data
                            e de views, estados de tela, guarda de papel

Sem subpastas dentro da feature: cresce primeiro numa pasta só, e separa quando
pesar.

## 3. Dependência

    componente -> gancho -> api.ts da feature -> cliente em src/api

Ninguém importa no sentido contrário, e uma feature não importa de outra: o
que for comum sobe para `shared/`.

## 4. Regras

### Idioma dos identificadores

Mesma regra do backend: domínio em português, vocabulário técnico em inglês.

| O que | Como | Exemplos |
|:--|:--|:--|
| Componente de domínio | PascalCase em português | `TabelaRanking`, `TelaBloqueio`, `FormularioCampanha`, `LinhaLeitura` |
| Gancho | português, prefixo `use` | `useResultado`, `useMeusVideos`, `useConta` |
| Vocabulário de React | inglês | `props`, `useState`, `onSubmit` |
| Arquivo de componente | igual ao componente | `TabelaRanking.tsx` |

### Dado da API

- **Componente não chama a API.** Ele recebe o dado de um gancho e desenha.
- **Todo dado da API passa pelo TanStack Query**, dentro de um gancho da
  feature (ADR-0010).
- **Um cliente HTTP só**, em `src/api/`. É ele que, ao receber 401, chama a
  renovação uma vez e repete a chamada; se a renovação também voltar 401, a
  pessoa vai para a tela de entrada (`endpoints.md`).
- **A API é chamada por caminho relativo**, `/api/v1/...`. No desenvolvimento,
  quem repassa para a api local é o Vite.
- **Chave de consulta** no formato `[feature, recurso, parâmetros]`, por
  exemplo `['resultado', 'campanha', { id }]`.
- Depois de gravar, invalida-se a chave da consulta, e os consumidores se
  atualizam. Não se atualiza tela na mão.

### Tipos

- `strict: true`, e `any` é proibido. Dado de formato desconhecido entra como
  `unknown` e é verificado antes de usar.
- **Tipo da API não se escreve à mão** (ADR-0010). Ele sai do OpenAPI por um
  comando, e o resultado é versionado. Se o tipo está errado, o conserto é na
  api.

### Papel e tela

- A tela que a pessoa vê depende do papel e, no criador, da situação da conta,
  lidos de `GET /eu`. O criador bloqueado vê só a tela de bloqueio (RN-18).
- A guarda de papel no frontend é conveniência de navegação. Quem decide é a
  api (ADR-0009): tela escondida não é permissão.

### Formatação de número e data

Uma função para cada, em `shared/`, e nenhuma formatação solta em componente.

- Views: inteiro com ponto separando o milhar, `1.234.567` (RN-64).
- Data: exibida em `DD/MM/AAAA`, trafegada em `AAAA-MM-DD` (RN-64).
- **Nunca `new Date("2026-11-30")`** sobre data pura: o JavaScript lê esse
  formato como meia-noite UTC, e em Brasília a data volta um dia. A data se
  formata como texto.
- Todo número de leitura aparece com a data dela, "lido em" (RN-43).

### Validação de formulário

O front valida **forma**, o backend valida **regra** (ADR-0011).

- No front: obrigatório, inteiro, data, cara de endereço.
- No backend: domínio do link, ordem das datas, prazo, views zero ou maior.
- A mensagem de recusa de regra vem do backend e é exibida como veio. O `codigo`
  decide comportamento, e o `campo` destaca o input.

### Estados da tela

Todo componente que mostra dado da API trata quatro estados: **carregando**,
**erro**, **vazio** e **com dado**. Campanha sem resultado mostra números em
zero e ranking vazio, não erro (UC-16 2a).

### Configuração

Variáveis do build, públicas por natureza: tudo que vai para o build é lido por
qualquer um que abra o site.

| Variável | Para quê |
|:--|:--|
| `VITE_TERMOS_URL` | link dos termos de uso, na tela de entrada (RN-09) |
| `VITE_PRIVACIDADE_URL` | link da política de privacidade, na tela de entrada (RN-09) |
| `VITE_CONTATO_OPERACAO` | contato para contestar, na tela de bloqueio (RN-18) |

Segredo nunca vai para o build.

### Acessibilidade mínima

Quatro itens verificáveis.

- Todo campo tem `label` associado, não apenas `placeholder`.
- Erro de campo é referenciado por `aria-describedby`, para o leitor de tela
  anunciar a recusa junto do campo.
- Componente interativo vem do shadcn/ui, que traz o comportamento do Radix.
- **O gráfico não é a única forma de ler o dado.** O ranking em tabela, os
  números e as leituras listadas existem também para quem não lê o gráfico.

## 5. Nomes

| O quê | Regra | Exemplo |
|:--|:--|:--|
| Componente | PascalCase | `TabelaRanking.tsx` |
| Gancho | `use` + camelCase | `useResultado.ts` |
| Chamadas da feature | `api.ts` | `features/resultado/api.ts` |
| Teste | nome do arquivo + `.test`, ao lado dele | `TabelaRanking.test.tsx` |

## 6. Testes

Seguem o ADR-0013 e a seção Testes do `fluxo-de-trabalho.md`.

- **Vitest com Testing Library**, testando o que a pessoa vê, não detalhe
  interno do componente.
- Testa-se o componente que **decide**: a tela que escolhe entre bloqueio,
  pendência e campanhas; a mensagem de cada situação de vídeo; a linha de
  leitura que marca a leitura de fechamento; o ranking com empate; a formatação
  de views e de data.
- A API é simulada trocando o `fetch` por uma API falsa, com dado inventado no
  formato do OpenAPI. Os testes passam pelo `api.ts` da feature, e o backend não
  precisa estar de pé.
- Todo componente que mostra dado da API tem teste dos quatro estados.
- Dado de teste é inventado, nunca copiado de criador real.

## 7. Proibido

1. Componente chamando a API.
2. `useEffect` para buscar dado do servidor (ADR-0010).
3. `fetch` fora de `src/api/`.
4. Tipo da API escrito à mão (ADR-0010).
5. `any`.
6. Regra de negócio no front, inclusive repetir validação de regra (ADR-0011).
7. `new Date()` sobre data pura.
8. Formatar número ou data fora de `shared/`.
9. Guardar em `useState` valor que se deriva de outro.
10. Índice do array como `key` em lista que muda de ordem, como o ranking.
11. Diálogo feito à mão. Usa o do shadcn/ui, que trata foco preso, Escape e
    retorno de foco (ADR-0010).
12. Segredo no build.
