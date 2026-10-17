# Fluxo de trabalho

Como o trabalho entra no repositório. O projeto tem uma pessoa desenvolvendo,
mais assistência, e este documento é calibrado para isso: nada aqui existe para
coordenar equipe. É o fluxo do Tracking peso, ajustado à VPS.

## Ramos

`main` mais um ramo curto por assunto, integrado rápido.

Nome do ramo: o tipo do commit mais o assunto em poucas palavras.
`feat/leituras`, `fix/empate-no-ranking`, `docs/especificacao-mvp`. Um ramo
cobre o assunto inteiro, nunca um ramo por item.

**Git Flow foi recusado.** O modelo original tem `develop`, `release/*` e
`hotfix/*`, e existe para produto com várias versões mantidas em paralelo. Aqui
existe uma versão: a que está no ar.

Ramo curto significa dias, não semanas. Ramo que vive um mês acumula conflito
com `main` e vira um segundo projeto.

Cada ramo vira um pull request, só como registro; a descrição sai da skill
`pr-description`. O merge não usa o botão do GitHub, que cria commits novos:
com a integração contínua verde, `git merge --ff-only` local e push da `main`,
que fecha o pull request.

## Commit

Formato:

```
<tipo>(<escopo>): <o que muda, no imperativo, ate ~50 caracteres>

<corpo curto, quebrado em 72 colunas>
```

Tipos: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `style`, `build`,
`ci`, `chore`, `revert`. `style` é formatação, não design; `build` mexe no que
produz o artefato, e `ci` em quem dispara.

**O corpo existe para o porquê, não para o como.** O diff já mostra o como.
Mudança mecânica não tem corpo. Dois testes antes de escrever a frase:

1. **Sobrevive a um rename?** Se cita nome de função ou caminho de código, vira
   mentira no dia em que alguém renomeia.
2. **O diff já mostra isso?** Se sim, corta.

Mensagem de commit **sem acento**: ela vai para terminal e ferramenta onde
codificação errada vira lixo.

Commit por assunto: mudança com decisão própria (uma regra, um ADR, uma rota)
vai sozinha; correções mecânicas vão juntas.

**Tamanho:** até umas 400 a 500 linhas de lógica por commit. Documentação,
formatação e configuração não entram na conta. Trabalho maior se divide em
blocos que fazem sentido sozinhos, cada um com o seu porquê.

## Testes

Todo teste passa por uma pergunta antes de ser proposto: **que defeito real ele
pega?** Sem resposta concreta, o teste não entra.

- Nada de teste tautológico: o que só repete o código, ou confere o que a
  linguagem ou a biblioteca já garantem.
- Nada de teste duplicado: se outro já pega o mesmo defeito, um dos dois sai.
- O que o teste espera sai do próprio teste, nunca importado do código testado.
- Ao propor um teste, dizer em uma linha o defeito que ele pega.

A camada de cada teste está no ADR-0013.

### TDD

Nas partes críticas, o teste vem antes do código: o pacote de regras (RNF-28),
e as recusas, a função e o isolamento das rotas (ADR-0009). Nas outras partes, o
TDD pode ficar de lado por ponderação de quem escreve.

1. **Vermelho:** escrever o teste do comportamento novo e ver ele falhar. Se
   passar de primeira, ou o comportamento já existe, ou o teste não testa nada.
2. **Verde:** escrever o mínimo de código para o teste passar.
3. **Refatorar:** melhorar o código com o teste protegendo.

## Antes de integrar

Roda local e roda na integração contínua (ADR-0017), e os dois têm que passar:

- Spotless, que formata e confere a formatação;
- a compilação com Error Prone, e o NullAway no pacote de regras;
- JUnit, nas duas camadas do backend, com o ArchUnit conferindo a regra de
  dependência (`arquitetura.md`);
- a checagem de tipos do TypeScript e o ESLint, no frontend;
- a regeneração dos tipos do OpenAPI a partir do backend, que falha se o
  resultado diferir do arquivo versionado: é o que força contrato e frontend a
  concordarem (ADR-0010);
- Vitest, nos componentes que decidem;
- o teste de fumaça do Playwright, quando existir.

Não há revisão de segunda pessoa. A integração contínua é o revisor, e por isso
não é opcional.

Quando o código é escrito pela assistência, a revisão é do interessado. Quando é
escrito pelo interessado, a assistência aponta o defeito e explica o mecanismo,
sem reescrever o arquivo sem pedido.

Rastro de teste e relatório de build sobem como artefato **só quando o job
falha**, com retenção curta: execução que passou não gera pergunta, e o rastro
dela ocupa o espaço do rastro que serviria.

## Definição de pronto

Uma tarefa está pronta quando as seis linhas abaixo são verdade.

1. O comportamento corresponde ao requisito, citado pelo identificador (`RF-28`,
   `UC-10`), inclusive nos fluxos alternativos.
2. Regra nova ou alterada está escrita em `regras-de-negocio.md`, com número
   novo, nunca reaproveitando número aposentado.
3. Existe teste na camada certa (ADR-0013): regra sem banco, rota contra
   Postgres, ou nenhum se for tela que só desenha.
4. As verificações de "Antes de integrar" passam local e na integração
   contínua.
5. Se houve decisão de arquitetura ou de stack com alternativa recusada, existe
   ADR novo, no formato do `modelo-adr.md`.
6. Se houve migração de esquema, ela roda nos testes, sobre o banco que o teste
   cria do zero; o ensaio do deploy a repete sobre o dump (RNF-09).

## Quando cada documento muda

| Aconteceu | Muda |
|:--|:--|
| Regra de negócio nova ou alterada | `regras-de-negocio.md`, e o requisito que a cita |
| Comportamento novo visível | `requisitos-funcionais.md` e `casos-de-uso.md` |
| Decisão de tecnologia com alternativa recusada | ADR novo em `especificacao/adr/` |
| Detalhe de implementação com alternativa recusada | a convenção do lado correspondente |
| Versão de ferramenta | a tabela de stack da convenção do lado correspondente |
| Rota nova ou alterada | `endpoints.md` |
| Código de recusa novo | o catálogo em `convencoes-backend.md` |
| Contêiner novo, ou caminho da requisição mudou | `arquitetura.md` |
| Identificador criado ou aposentado | as contagens do `README.md` e do `CLAUDE.md` |

### Mexer numa regra exige ler quem a cita

Ao alterar ou aposentar uma regra, leia **todos** os documentos que a citam,
contra ela:

    grep -rn "RN-41" --include="*.md" .

Duas regras corretas separadas produzem um sistema errado juntas, e o documento
que está errado raramente é o que você acabou de mudar. Depois de mexer,
confira que nenhuma citação ficou pendurada, e que nenhuma regra aposentada
continua citada fora da tabela de aposentadoria.

Documento datado que uma descoberta posterior contradiz **não estava errado**:
registrava o que se sabia. Só se reescreve quando a informação nova muda o que
deve ser feito. ADR nunca é reescrito: é superado, e no superado muda só a
linha de situação.

A auditoria de coerência (`.claude/skills/auditar-coerencia/`) faz essa
conferência em escala, por agentes, desde um commit ou na especificação
inteira.

## `especificacao/` é versionada

Deliberado: a especificação existe antes do código e é o que impede a
discussão de recomeçar. Ela entra em commit como qualquer arquivo.

O que **não** entra: `.env`, `target/`, `node_modules/`, a ferramenta local em
`.claude/`, e o `notas-privadas.md` (ADR-0018).

## Deploy

Manual, por script que para no primeiro erro (ADR-0017), rodado na VPS:

1. baixar as imagens da api e do frontend com a etiqueta escolhida;
2. rodar o ensaio: restaurar o dump mais recente num banco descartável,
   recusando dump com mais de um dia, e aplicar nele as migrações novas
   (RNF-08, RNF-09, ADR-0006);
3. rodar a migração na produção, num contêiner de uma execução só;
4. subir a versão nova com o Compose;
5. conferir que `GET /saude` responde, para o deploy não terminar em silêncio
   com a api caída.

Push para o remoto é sempre ação do interessado, nunca automática.
