# Tarefas

Conjunto de macro tarefas: o que se constrói, em que ordem, e o estado de cada
uma. Cada macro tarefa se quebra em tarefas pequenas no dia a dia, decididas na
hora, num ramo por assunto (`fluxo-de-trabalho.md`). O detalhe não mora aqui.

O desenho do esquema e da camada de regras, as armadilhas e o que já foi
recusado estão no `instrucao.md`. O comportamento está na `especificacao/`.

## O que este documento não carrega

| Para achar | Abra |
|:--|:--|
| a que requisito uma rota atende | `especificacao/endpoints.md`, coluna Requisito |
| que regra cada restrição de banco impõe | `instrucao.md`, seção 5 |
| que regra cada método de `regras/` implementa, e as fronteiras dos testes | `instrucao.md`, seção 5 |
| os fluxos alternativos de um caso de uso | `especificacao/casos-de-uso.md` |
| o código de cada recusa | `especificacao/convencoes-backend.md`, Catálogo de códigos |

A coluna de entrega nomeia **o artefato**: a rota com método e caminho, a
função, a tela. O identificador entre parênteses existe para a conferência de
cobertura do fim.

Identificador de tarefa é estável, e número aposentado não volta.

## Vale para toda tarefa

- **Testes** seguem a seção Testes do `fluxo-de-trabalho.md`: só entra teste
  que pega defeito real, e nas partes críticas ele vem antes do código.
- **Toda rota** tem teste de papel errado e, quando lida com dado de criador,
  teste de que um criador não alcança o dado de outro (ADR-0009). Recusa sai no
  formato único, com o código do catálogo.
- **Todo "hoje"** vem do relógio do sistema, no horário de Brasília (ADR-0014).
- **Nenhum log** registra e-mail ou @, nem pelo caminho indireto (RNF-16).
- Tarefa de rota ou de tela está pronta quando os **fluxos alternativos** do
  caso de uso citado funcionam, e não só o caminho sem erro.

## O corte é horizontal

A tarefa entrega uma camada, não uma fatia de ponta a ponta. Rota sem tela é
entrega válida. Entre a tarefa da rota e a da tela, a primeira linha da
definição de pronto fica parcial: o contrato fecha, e o caso de uso, escrito em
termos de pessoa numa tela, ainda não.

O corte vertical foi considerado e recusado: a base do frontend é pré-requisito
de qualquer fatia de ponta a ponta, e a camada de regras é a parte mais testável
justamente por não ter tela.

## Estado

`a fazer`, `fazendo`, `feito`.

---

## Etapa 0: ambiente e integração contínua

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-01 | ambiente do backend: projeto Maven com o Maven Wrapper, Spring Boot e as versões da tabela de stack (ADR-0002), Spotless, Error Prone e NullAway em `regras/` (ADR-0013), `compose.dev.yml` só com o Postgres (RNF-27), `.env.example`, a estrutura de pasta do `arquitetura.md` (ADR-0003), e o log da aplicação só em erro (RNF-16) | a fazer |
| T-02 | integração contínua com o Spotless e a compilação com Error Prone, artefato só na falha; cada camada de teste entra no job quando a primeira tarefa que a usa termina, e o frontend entra com a checagem de tipos e o ESLint quando a T-26 terminar, e com a regeneração dos tipos do OpenAPI comparada com a versionada quando a T-27 terminar (ADR-0010, ADR-0017, ADR-0018) | a fazer |

A T-02 depende do repositório remoto, que ainda não existe (`instrucao.md`,
seção 6).

## Etapa 1: esquema

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-03 | as dez tabelas na primeira migração do Flyway, escrita em SQL, com as restrições e os índices únicos parciais do desenho; as entidades JPA, conferidas pelo `validate`; e o modo `migrar` do `.jar` (ADR-0004, ADR-0005, ADR-0006, RNF-11) | a fazer |

## Etapa 2: camada de regras

Sem Postgres. Os métodos e as fronteiras estão no `instrucao.md`, seção 5. O
NullAway limpo e o teste de arquitetura valem para as quatro (ADR-0012,
ADR-0013, RNF-28).

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-04 | `situacaoDaCampanha`, `validarDatasDaCampanha`, `podeEntrar`, `validarEnvioDeVideo`, `validarAprovacaoDeVideo`, e o teste de arquitetura com o ArchUnit | a fazer |
| T-05 | `validarLeitura`, `leituraDeFechamento`, `numeroAtual` | a fazer |
| T-06 | `calcularRanking`, `totalPorData`, `pendenciasDoEncerramento` | a fazer |
| T-07 | `normalizarEmail`, `normalizarUsuarioTiktok`, `transicaoDaConta`, `validarGanhador`, `validarRemocaoDeAcesso` | a fazer |

## Etapa 3: base das rotas e autenticação

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-08 | o que toda rota usa: o formato único de erro num tratador global, o relógio do sistema no horário de Brasília, a guarda de papel e a recusa do criador bloqueado (ADR-0009, ADR-0014) | a fazer |
| T-09 | modo `cadastrar-operador <e-mail>` do `.jar` (UC-20) | a fazer |
| T-10 | `GET /auth/login`, `GET /auth/callback`, `POST /auth/renovacao` e `POST /auth/logout`, pelo Spring Security: só e-mail verificado, conta de criador criada no primeiro acesso, os dois tokens em cookie, a renovação, e a falha redirecionando com o motivo (UC-01, ADR-0007, ADR-0008, RNF-22, RNF-23) | a fazer |
| T-11 | `GET /eu` e `PUT /eu/usuario-tiktok` (UC-02) | a fazer |

A T-08 vem antes de qualquer rota porque, sem ela, as recusas saem no formato
do framework e as verificações passam assim mesmo. A T-10 depende do console do
Google (`instrucao.md`, seção 6).

## Etapa 4: contas e acessos

Teste de rota pelo MockMvc, contra o Postgres do Testcontainers, em transação
revertida (ADR-0013).

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-12 | `GET /criadores`, `POST /criadores/{id}/aprovacao`, `POST /criadores/{id}/recusa` e `PUT /criadores/{id}/usuario-tiktok` (UC-03) | a fazer |
| T-13 | `POST /criadores/{id}/bloqueio` e `POST /criadores/{id}/desbloqueio` (UC-04) | a fazer |
| T-14 | `GET /acessos`, `POST /acessos` e `DELETE /acessos/{id}` (UC-18) | a fazer |

## Etapa 5: campanhas

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-15 | `GET /campanhas`, `GET /campanhas/{id}`, `POST /campanhas`, `PATCH /campanhas/{id}` e `DELETE /campanhas/{id}` (UC-05, UC-06, UC-07) | a fazer |
| T-16 | `POST /campanhas/{id}/participacao` (UC-08) | a fazer |

## Etapa 6: vídeos

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-17 | `POST /campanhas/{id}/videos` e `GET /campanhas/{id}/meus-videos` (UC-09, UC-13) | a fazer |
| T-18 | `GET /videos`, `POST /videos/{id}/aprovacao`, `POST /videos/{id}/recusa` e `POST /videos/{id}/desqualificacao` (UC-10, UC-12) | a fazer |

## Etapa 7: leituras e resultado

Se aparecer vontade de escrever soma ou ordenação no controller ou no service,
falta método na etapa 2.

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-19 | `GET /campanhas/{id}/leituras`, `GET /videos/{id}/leituras`, `PUT /videos/{id}/leituras/{data}` e `DELETE /videos/{id}/leituras/{data}` (UC-11, ADR-0001) | a fazer |
| T-20 | `GET /campanhas/{id}/resultado`, montado no service chamando `regras/`, sem e-mail em campo nenhum (UC-13, UC-16, RNF-12) | a fazer |
| T-21 | `POST /campanhas/{id}/encerramento`, gravando o ranking final (UC-14) | a fazer |
| T-22 | `GET /campanhas/{id}/participantes`, `POST /campanhas/{id}/ganhadores`, `PATCH /ganhadores/{id}` e `DELETE /ganhadores/{id}` (UC-15) | a fazer |
| T-23 | `GET /campanhas/{id}/exportacao`, no formato da planilha brasileira (UC-17) | a fazer |

## Exclusão de conta

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-24 | `POST /criadores/{id}/exclusao`, apagando as participações das campanhas não encerradas e o link dos vídeos das encerradas antes da conta, e guardando o @ da conta bloqueada (UC-19) | a fazer |

Vem depois de todas as tabelas terem rota, de propósito. É a operação que
apaga tudo, e escrita antes ela nasce contra um esquema pela metade.

## Etapa 8: design das telas

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-25 | design das telas com shadcn/ui, a partir dos casos de uso, no Claude Design, seguindo o `identidade-visual.md`; fluxo que faltar nos casos de uso volta para a especificação antes de virar tela | a fazer |

## Etapa 9: base do frontend

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-26 | projeto Vite com TypeScript, Tailwind e shadcn/ui, React Router declarativo, TanStack Query, o repasse do Vite para a api local, o cliente HTTP que renova uma vez ao receber 401, e a tela de entrada com o aviso dos termos e os dois motivos de falha (UC-01, ADR-0010) | a fazer |
| T-27 | tipos gerados do OpenAPI que o springdoc publica no perfil de desenvolvimento, e a formatação de data e de views em `shared/` (ADR-0010, RF-42) | a fazer |

A T-27 vem antes de qualquer tela: formatação que nasce espalhada diverge
antes do fim do mês.

## Etapa 10: telas

Vitest só nos componentes que decidem. Os quatro itens de acessibilidade valem
para todas, e o gráfico nunca é a única forma de ler o dado.

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-28 | criador: @ e verificação, situação da conta e tela de bloqueio (UC-02, UC-04, ADR-0011) | a fazer |
| T-29 | criador: campanhas, participar, enviar vídeo, meus vídeos com gráfico, ranking e ganhadores (UC-08, UC-09, UC-13) | a fazer |
| T-30 | operador: contas, verificação, bloqueio, exclusão e acessos (UC-03, UC-04, UC-18, UC-19) | a fazer |
| T-31 | operador: criar, editar, apagar e encerrar campanha (UC-05, UC-06, UC-07, UC-14) | a fazer |
| T-32 | operador: fila de vídeos e planilha de leituras, uma linha por vídeo (UC-10, UC-11, UC-12, RNF-26) | a fazer |
| T-33 | operador: ganhadores e data de entrega (UC-15) | a fazer |
| T-34 | painel da gravadora e exportação (UC-16, UC-17) | a fazer |

## Etapa 11: produção

| Tarefa | Entrega | Estado |
|:--|:--|:--|
| T-35 | a VPS: Compose com caddy, api e db, o banco só na rede interna, o Caddyfile com HTTPS, retorno de `index.html` e repasse de `/api`, as duas imagens em dois estágios, SSH só com chave, firewall com 22, 80 e 443, atualizações automáticas, e os logs com 7 dias (ADR-0015, RNF-17, RNF-18, RNF-19, RNF-20, RNF-21) | a fazer |
| T-36 | backup: dump diário na VPS com 7 dias e limpeza do token de renovação vencido; o agendamento na máquina do operador, com 30 dias, restauração local e aviso de homem morto só para dump do próprio dia; e o monitor externo de disponibilidade (ADR-0016, RNF-04, RNF-06, RNF-07, RNF-08, RNF-13, RNF-14, RNF-23) | a fazer |
| T-37 | a integração contínua constrói e publica as imagens na `main` (ADR-0017) | a fazer |
| T-38 | script de deploy com os cinco passos do `fluxo-de-trabalho.md`, que para no primeiro erro, e o `GET /saude` que o último passo e o monitor externo conferem (ADR-0006, ADR-0017, RNF-04, RNF-08, RNF-09) | a fazer |
| T-39 | teste de fumaça do Playwright, com o token de renovação inserido no banco: entra como criador, vê a campanha e o ranking (ADR-0013) | a fazer |

O primeiro deploy é montagem, feita à mão. O script começa pelo ensaio, e o
ensaio precisa de um dump, que só existe depois de o backup rodar uma vez. Por
isso o backup vem antes do script.

---

## Sem tarefa

Requisitos não funcionais que são constatação, premissa ou prática de
operação, e não pedem entrega: RNF-01, RNF-02, RNF-03, RNF-05, RNF-10, RNF-15,
RNF-24, RNF-25.

## Conferir cobertura

Antes de fechar uma etapa, conferir que nada da especificação ficou sem
tarefa. Saída vazia nos cinco é o esperado.

Rota, por método e caminho:

    comm -23 \
      <(awk -F'|' '/^\| `(GET|POST|PATCH|PUT|DELETE)`/ {gsub(/[ `]/,"",$2); gsub(/[ `]/,"",$3); print $2" "$3}' especificacao/endpoints.md | sort -u) \
      <(grep -oE '`(GET|POST|PATCH|PUT|DELETE) /[a-z/{}._?=&-]+' tarefas.md | sed -E 's/^`//; s/[?].*//' | sort -u)

Caso de uso:

    grep -oE 'UC-[0-9]+' especificacao/casos-de-uso.md | sort -u |
      while read -r uc; do grep -q "$uc\b" tarefas.md || echo "falta: $uc"; done

Requisito funcional. Conta como coberto o RF citado num caso de uso, na coluna
Requisito de uma rota, ou aqui; por isso só vale depois de os dois de cima
passarem:

    comm -23 \
      <(grep -oE '^- \*\*RF-[0-9]+' especificacao/requisitos-funcionais.md | grep -oE 'RF-[0-9]+' | sort -u) \
      <(cat especificacao/casos-de-uso.md tarefas.md \
          <(awk -F'|' '/^\| `(GET|POST|PATCH|PUT|DELETE)`/ {print $6}' especificacao/endpoints.md) |
        grep -oE 'RF-[0-9]+' | sort -u)

Requisito não funcional, citado numa tarefa ou em "Sem tarefa":

    comm -23 \
      <(grep -oE '^- \*\*RNF-[0-9]+' especificacao/requisitos-nao-funcionais.md | grep -oE 'RNF-[0-9]+' | sort -u) \
      <(grep -oE 'RNF-[0-9]+' tarefas.md | sort -u)

ADR aceito, citado numa tarefa:

    for f in especificacao/adr/ADR-*.md; do
      grep -q 'Situação:\*\* aceito' "$f" || continue
      id=$(basename "$f" | cut -c1-8)
      grep -q "$id" tarefas.md || echo "falta: $id"
    done

A conferência só funciona porque a entrega nomeia a rota literalmente e cita o
identificador. Escrever "as rotas de vídeo" quebra a conferência em vez de
economizar espaço.

## Tarefas aposentadas

Nenhuma até agora.
