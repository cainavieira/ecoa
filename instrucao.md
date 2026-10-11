# Instrução de construção

Documento de partida para quem for **implementar** o MVP do Ecoa. A
especificação já existe inteira em `especificacao/` e não precisa ser
redescoberta: 65 regras de negócio, 42 requisitos funcionais, 20 casos de uso,
28 requisitos não funcionais e 18 ADR, todos com identificador estável.

Este documento não repete nada disso. Ele diz como ler a especificação, o que
já foi recusado, onde estão as armadilhas, e o desenho do esquema e da camada
de regras. **O que construir e em que ordem está no `tarefas.md`**, que muda
enquanto o trabalho acontece; este muda pouco.

---

## 1. Antes de escrever qualquer linha

Leia nesta ordem. É a ordem de dependência, não de importância.

1. `especificacao/regras-de-negocio.md`, inteiro, com a seção **Decisões
   recusadas** no fim: ela existe para ninguém propor de novo o que já foi
   descartado.
2. `especificacao/atores.md`. São quatro, e o TikTok não é um deles.
3. `especificacao/requisitos-funcionais.md`.
4. `especificacao/casos-de-uso.md`. Os **fluxos alternativos** são o valor:
   dizem o que acontece quando dá errado.
5. `especificacao/requisitos-nao-funcionais.md`. Repare na assimetria: não há
   meta de disponibilidade, e a leitura é o dado que não se recupera.
6. `especificacao/arquitetura.md`.
7. `especificacao/convencoes-backend.md` e `convencoes-frontend.md`, com a
   stack em versão fixa.
8. `especificacao/endpoints.md`.
9. `especificacao/fluxo-de-trabalho.md`.
10. Os ADR, por assunto, quando a tarefa chegar nele.

A visão do produto (`visao/`) descreve a ideia inteira, e quase tudo nela é
depois do MVP. Ela não decide comportamento: quem decide é a especificação.

**A regra que resume tudo:** quando você for escrever uma linha e não souber
qual comportamento é o certo, a resposta está num documento com identificador.
Se não estiver, é decisão nova, e decisão nova é do interessado.

---

## 2. Como trabalhar com o interessado

Está no `CLAUDE.md`, que carrega sozinho em toda sessão nesta pasta. Três pontos
específicos de implementar:

**Rode antes de opinar sobre código.** Bug raramente aparece só de ler.

**Ele escreve código também.** Quando o código é dele, aponte o defeito e
explique o mecanismo, e só escreva no arquivo se ele pedir. Por padrão, a
assistência escreve e ele revisa, um passo por vez.

**Teste só entra se pega defeito real.** A seção Testes do
`fluxo-de-trabalho.md` diz o critério.

---

## 3. O que já está decidido e não se rediscute

| Vontade | Já recusado em |
|:--|:--|
| Ler as views pela API do TikTok, ou por scraping | ADR-0001 |
| Login pelo TikTok, ou com senha própria | ADR-0007 |
| Django, ou páginas montadas no servidor | ADR-0002 |
| SQLite, para simplificar backup ou teste | ADR-0004, ADR-0013 |
| Devolver a entidade direto na rota | ADR-0005 |
| Migração na subida da api, o padrão do Spring | ADR-0006 |
| Guardar o JWT no `localStorage` | ADR-0008 |
| Segurança por linha do Postgres | ADR-0009 |
| Duplicar no frontend a validação de regra | ADR-0011 |
| Camadas de Clean Architecture | ADR-0012 |
| Rota de teste que emite sessão | ADR-0013 |
| Data do aparelho em cabeçalho, ou dia do servidor em UTC | ADR-0014 |
| Kubernetes, nginx, plataforma gerenciada | ADR-0015 |
| Backup na nuvem, por ora | ADR-0016 |
| Deploy automático pela integração contínua | ADR-0017 |
| Upload de arquivo, cálculo automático de prêmio, CPF ou Pix no sistema, e-mail, página pública de ranking, limite de vídeos | Decisões recusadas, nas regras |

---

## 4. Armadilhas conhecidas

Cada uma destas não gera erro na hora.

**O "hoje" em UTC.** O contêiner roda em UTC, e `LocalDate.now()` sem zona vira
o dia às 21h de Brasília: a campanha fecha três horas antes. Todo "hoje" vem do
relógio do sistema, no horário de Brasília (ADR-0014).

**`new Date("2026-11-30")` no frontend.** O JavaScript lê como meia-noite UTC, e
em Brasília a data volta um dia. Data pura se formata como texto.

**`endsWith("tiktok.com")` aceita `eviltiktok.com`.** O domínio do link é
`tiktok.com` ou termina em `.tiktok.com`, com o ponto (RN-34).

**O mesmo vídeo com dois links.** O link curto do aplicativo e o link completo
não são o mesmo texto, e o índice único não pega. Isso é conferência humana
(RN-35), não defeito do índice.

**Views em `int`.** Estoura em dois bilhões. É `long` e `bigint`.

**Views que caem.** A leitura nova pode ser menor que a anterior, porque as
redes removem views falsas (RN-39). Validar crescimento seria recusar dado
verdadeiro.

**Índice único parcial não existe no JPA.** O @ único só entre contas aprovadas
e bloqueadas (RN-11) e o link único só entre envios não recusados (RN-35) vivem
só na migração. A conferência da subida não os vê (ADR-0005).

**A chave estrangeira com `ON DELETE SET NULL` anonimiza tudo.** É ela que deixa
o criador apagado como "participante removido" nas campanhas encerradas. Mas,
nas não encerradas, a regra manda apagar a participação (RN-59): o service
apaga essas antes de apagar a conta, ou elas viram anônimas também. Ela também
não alcança o link do vídeo, que leva ao @: o service apaga o link dos vídeos
das campanhas encerradas na mesma transação.

**Calcular o ranking de campanha encerrada a partir do estado atual.** Um
criador desbloqueado depois do encerramento voltaria a um ranking que é final
(RN-48). O ranking final é gravado no encerramento e lido de lá.

**O Spring Boot migra sozinho na subida.** Com o Flyway no projeto, a api aplica
as migrações ao iniciar, a menos que isso seja desligado (ADR-0006).

**`open-in-view` vem ligado.** Com ele desligado, o carregamento preguiçoso fora
do service vira erro na hora (ADR-0005).

**`SameSite=Strict` quebra o login.** O retorno do Google é navegação vinda de
outro site. É `Lax` (ADR-0008).

**Sem o retorno de `index.html` no Caddy, recarregar a página dá 404.** As rotas
existem só no navegador (ADR-0010, ADR-0015).

**`react-router-dom` não tem versão 8.** O pacote é `react-router`, e o npm não
avisa nada ao instalar o errado.

**TypeScript 7 quebra o lint e a geração de tipos.** O typescript-eslint e o
openapi-typescript ainda não aceitam a 7. Fica a 5.9
(`convencoes-frontend.md`).

**CSV no Excel em português.** Sem ponto e vírgula e sem a marca de ordem de
bytes, o Excel junta as colunas e estraga os acentos. E célula que começa com
`=`, `+`, `-` ou `@` vira fórmula (`endpoints.md`).

**E-mail com maiúscula.** O mesmo e-mail com caixa diferente criaria duas
contas. Normaliza ao entrar, num ponto só (RN-01).

**E-mail não verificado.** O Google devolve se o e-mail foi verificado. Aceitar
sem conferir deixa alguém entrar com o e-mail de outra pessoa (RN-01).

**Terminal Windows em `cp1252`.** Texto com acento pode sair embaralhado no
console. O ambiente de desenvolvimento aqui é Windows.

**Caminho relativo em script.** O script do backup, na máquina do operador,
roda pelo agendador do Windows, de outra pasta. Use caminho derivado do próprio
script, e teste do jeito que o agendador roda.

**Docker parado na máquina do operador.** A restauração diária do backup precisa
do Docker de pé. Se ele não subir junto com a máquina, o download acontece e a
restauração falha, e o aviso de homem morto não deve disparar como sucesso.

**Dump velho que restaura bem.** Se o agendamento da VPS parar, o "dump mais
recente" continua sendo o último que deu certo, e baixá-lo e restaurá-lo passa
todo dia. O agendamento confere a data do dump antes de avisar o homem morto, e
o ensaio do deploy confere antes de ensaiar (RNF-08).

**Remoção cruzada de operadores.** Dois operadores que removem um ao outro ao
mesmo tempo passam os dois pela conferência de "não é o último" se ela não
bloquear a leitura. A contagem e a remoção acontecem na mesma transação, com
bloqueio das linhas de operador (RN-06).

**Chave lida no código e ausente no `.env`.** Mantenha o `.env.example`
sincronizado, e faça variável obrigatória ausente derrubar a subida.

**`catch (Exception e)` largo.** Engole a falha e devolve resultado vazio. Está
proibido nas convenções.

---

## 5. Desenho do esquema e da camada de regras

### Esquema

Dez tabelas, nomes no singular, colunas em português sem acento
(`convencoes-backend.md`).

**`conta`**: `id`, `email` único e em minúsculas, `nome` nulo até o primeiro
acesso de conta cadastrada pelo operador, `papel` (`criador`, `operador`,
`gravadora`), `criado_em`.

**`criador`**: `conta_id` como chave primária, apontando para `conta` com
`ON DELETE CASCADE`; `usuario_tiktok` nulo até ser informado;
`codigo_verificacao` único; `situacao` (`pendente`, `recusada`, `aprovada`,
`bloqueada`); `motivo_recusa`, `motivo_bloqueio` e `motivo_desbloqueio`,
nulos; `aceite_termos_em`.

**`token_renovacao`**: `id`, `conta_id` com `ON DELETE CASCADE`, `hash` único (o
token nunca é gravado, só o hash), `criado_em`, `expira_em`.

**`campanha`**: `id`, `musica`, `artista`, `regulamento`, `data_inicio`,
`data_fim`, `data_fechamento`, `encerrada_em` nulo até o encerramento,
`criado_em`.

**`link_referencia`**: `id`, `campanha_id` com `ON DELETE CASCADE`, `url`,
`descricao` nula.

**`participacao`**: `id`, `campanha_id`, `criador_id` apontando para
`criador` com `ON DELETE SET NULL` (é a anonimização da RN-59), `aceite_em`,
`posicao_final` e `total_final`, nulos até o encerramento e nulos para quem
ficou fora do ranking final.

**`video`**: `id`, `participacao_id` com `ON DELETE CASCADE`, `link`, nulo só
no vídeo de participante removido (RN-59), `situacao` (`em_analise`, `aprovado`, `recusado`, `desqualificado`),
`data_publicacao` nula até a aprovação, `motivo` nulo, `enviado_em`.

**`leitura`**: `video_id` com `ON DELETE CASCADE`, `data`, `views`, com chave
primária (`video_id`, `data`), `registrada_em`.

**`ganhador`**: `id`, `campanha_id`, `participacao_id` com `ON DELETE CASCADE`,
`premio`, `entregue_em` nula, `registrado_em`.

**`conta_bloqueada_apagada`**: `usuario_tiktok` como chave primária,
`apagada_em`. Sem chave estrangeira, porque a conta já não existe. Apagar de
novo uma conta bloqueada com o mesmo @ atualiza a data.

Mais a `flyway_schema_history`, que é do Flyway e não se mexe à mão.

Restrições que **vão** para o banco, junto com a verificação na aplicação:

| Restrição | Regra |
|:--|:--|
| `conta.email` único | RN-01 |
| `conta.papel` em criador, operador, gravadora | RN-02 |
| `criador.situacao` nos quatro valores | RN-10 |
| `criador.usuario_tiktok` único entre as situações aprovada e bloqueada, por índice único parcial | RN-11 |
| `criador`: situação diferente de pendente exige `usuario_tiktok` | RN-13 |
| `criador`: recusada exige `motivo_recusa`; bloqueada exige `motivo_bloqueio` | RN-13, RN-17 |
| `criador.codigo_verificacao` único | RN-12 |
| `campanha`: `data_inicio` ≤ `data_fim` ≤ `data_fechamento` | RN-24 |
| `participacao` única por (`campanha_id`, `criador_id`) | RN-31 |
| `video.situacao` nos quatro valores | RN-33 |
| `video.link` único entre as situações diferentes de recusado, por índice único parcial | RN-35 |
| `video`: aprovado ou desqualificado exige `data_publicacao`; recusado ou desqualificado exige `motivo` | RN-36, RN-37, RN-38 |
| `leitura` única por (`video_id`, `data`) | RN-40 |
| `leitura.views` zero ou maior | RN-39 |

A unicidade da participação deixa várias linhas com `criador_id` nulo, que é o
que acontece com vários participantes removidos na mesma campanha: o Postgres
não considera dois nulos iguais.

Restrições que **não** vão para o banco, e por quê:

- Fechamento futuro (RN-26), leitura e entrega não futuras (RN-40, RN-52):
  `CHECK` precisa ser imutável, e `current_date` não é.
- Publicação entre início e fim (RN-27, RN-36), ganhador da mesma campanha
  (RN-52), link nulo só no vídeo de participante removido (RN-59): atravessam
  tabelas.
- Leitura de fechamento (RN-41) e pendências do encerramento (RN-50):
  atravessam linhas.
- Ao menos um link de referência (RN-22): o banco não exige "ao menos um" sem
  gatilho.
- Papel e isolamento (ADR-0009).

### Camada de regras

Pacote `regras/`, que **não importa Spring nem JPA** (ADR-0012). Os métodos
recebem dados e devolvem valores.

| Método | Regras |
|:--|:--|
| `normalizarEmail`, `normalizarUsuarioTiktok` | RN-01, RN-11 |
| `transicaoDaConta` (aprovar, recusar, corrigir o @, bloquear, desbloquear) | RN-11, RN-13, RN-14, RN-15, RN-17, RN-21 |
| `situacaoDaCampanha` | RN-25 |
| `validarDatasDaCampanha` | RN-24, RN-26, RN-27 |
| `podeEntrar` | RN-16, RN-32 |
| `validarEnvioDeVideo` | RN-34, RN-35 |
| `validarAprovacaoDeVideo` | RN-19, RN-36 |
| `validarLeitura` | RN-39, RN-40, RN-41 |
| `leituraDeFechamento`, `numeroAtual` | RN-41, RN-43 |
| `calcularRanking` | RN-45, RN-46 |
| `totalPorData` | RN-49 |
| `pendenciasDoEncerramento` | RN-50 |
| `validarGanhador` | RN-52 |
| `validarRemocaoDeAcesso` | RN-06 |

As fronteiras onde a regra decide, e que por isso precisam de teste:

- situação: o dia do início e o dia do fim são de campanha aberta; o dia
  seguinte ao fim é de apuração; o encerramento vence as datas (RN-25)
- fechamento igual a hoje é recusado, e amanhã é aceito (RN-26)
- mudar o fim para a véspera da publicação de um vídeo aprovado é recusado, e
  para o próprio dia da publicação é aceito (RN-27)
- link: `https://vm.tiktok.com/...` aceito; `http://www.tiktok.com/...`,
  `https://eviltiktok.com/...` e `https://tiktok.com.exemplo.com/...` recusados
  (RN-34)
- @: arroba na frente e maiúsculas são normalizados; espaço no meio é recusado
  (RN-11)
- aprovar conta cujo @ é de outra aprovada é recusado; cujo @ é de outra
  pendente é aceito (RN-11, RN-13)
- publicação no dia do início e no dia do fim é aceita; na véspera do início, e
  no dia seguinte ao fim, recusada (RN-36)
- leitura na data da publicação é aceita, e na véspera, recusada (RN-40)
- leitura de fechamento: com fechamento em 30/11, gravar 02/12 a cria; depois,
  01/12 e 03/12 são recusadas, e outra em 02/12 a substitui; apagada a de 02/12,
  01/12 passa a ser aceita (RN-40, RN-41)
- views menores que as da leitura anterior são aceitas (RN-39)
- vídeo aprovado sem leitura conta zero, com a marca de que não tem leitura
  (RN-43)
- ranking: empate fica 1, 1, 3; criador bloqueado, vídeo desqualificado e
  participante sem vídeo que conta ficam fora (RN-45, RN-46)
- total por data: soma, de cada vídeo, a leitura mais recente até a data; vídeo
  sem leitura até a data não soma (RN-49)
- encerramento: vídeo em análise pende, inclusive o de criador bloqueado;
  vídeo que conta sem leitura de fechamento pende; vídeo aprovado de criador
  bloqueado não pende, porque não conta (RN-45, RN-50)
- remoção de acesso: remover o outro operador, quando há dois, é aceito;
  remover o último operador é recusado (RN-06)
- ganhador: antes do início é recusado, no dia do início é aceito, bloqueado é
  recusado (RN-52)

Mais o **teste de arquitetura**, com o ArchUnit (`arquitetura.md`).

Tudo isso roda sem Postgres.

---

## 6. O que depende do mundo real

Peça ao interessado antes da tarefa em que faz falta:

| O quê | Antes de | Para quê |
|:--|:--|:--|
| Repositório público no GitHub, com o remoto conectado | T-02 | integração contínua (ADR-0018) |
| Projeto no console do Google, com os endereços de retorno local e de produção, publicado em produção | T-10 | sem isso, ninguém entra (ADR-0007) |
| Contato da operação para a tela de bloqueio | T-28 | RN-18 |
| Termos de uso e política de privacidade, com o aceite do ranking | T-35 | RN-09, RNF-15. Em desenvolvimento, a tela de entrada aponta para uma página provisória. O texto não é escrito pela assistência |
| Nome público e domínio, decididos com o Mateus | T-35 | HTTPS e endereço de retorno do Google |
| VPS contratada | T-35 | produção (ADR-0015) |
| Serviço de monitor externo, para disponibilidade e homem morto | T-36 | RNF-04, RNF-08 |
| Máquina do operador com Docker, ligada no horário do agendamento | T-36 | ADR-0016 |
| Regulamento e prêmio da campanha piloto, escritos pela gravadora | o piloto | Fase 0 da visão |

---

## 7. Como saber que acabou

A definição de pronto de cada tarefa está no `fluxo-de-trabalho.md`. Para o
MVP inteiro, o critério é mais curto:

Um criador entra com a conta Google, informa o @, é aprovado pelo operador,
entra numa campanha e envia o link de um vídeo; o operador aprova o vídeo com a
primeira leitura, registra as leituras até o fechamento, encerra a campanha e
registra um ganhador; e a gravadora vê o ranking final e exporta o CSV. Tudo
rodando na VPS com HTTPS, com o dump de ontem restaurado na máquina do operador.

Se qualquer parte dessa frase não for verdade, não acabou.
