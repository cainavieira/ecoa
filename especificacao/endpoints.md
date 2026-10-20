# Endpoints

Contrato da API. Escrito depois dos casos de uso e das decisões de stack, de
propósito: ele depende deles, e não o contrário.

Este documento é a **intenção**. A verdade executável é a especificação OpenAPI
que o springdoc publica a partir do código (ADR-0002), e é dela que saem os
tipos do frontend (ADR-0010). Quando os dois divergirem, o OpenAPI está certo e
este documento está datado.

## Convenções gerais

- Prefixo `/api/v1`. Toda rota abaixo é relativa a ele.
- Autenticação pelo token de acesso em cookie (ADR-0008) em todas as rotas,
  exceto as de `auth/` e a `saude`.
- Toda resposta de erro usa o formato único de `convencoes-backend.md`. A
  exceção é o retorno do login, que volta como redirecionamento.
- Datas em `AAAA-MM-DD`; views como inteiro (RN-64).
- O dia corrente é calculado no servidor, no horário de Brasília (ADR-0014).
  Nenhuma rota recebe a data do aparelho.
- Campanha, vídeo, conta e ganhador são endereçados por identificador
  sequencial. A leitura é endereçada pelo vídeo e pela data, que é a chave
  natural dela (RN-40).

## Lista

| Método | Caminho | Função | Faz | Requisito |
|:--|:--|:--|:--|:--|
| `GET` | `/auth/login` | qualquer | redireciona para o Google | RF-01 |
| `GET` | `/auth/callback` | qualquer | recebe o código, acha ou cria a conta, emite os tokens e redireciona para a aplicação; em qualquer falha, redireciona para a tela de entrada com o motivo | RF-01, RF-02, RF-03 |
| `POST` | `/auth/renovacao` | qualquer | troca o token de renovação por um token de acesso novo e empurra a validade da renovação | RNF-23 |
| `POST` | `/auth/logout` | qualquer | apaga o token de renovação e expira os dois cookies | RF-04 |
| `GET` | `/eu` | qualquer | nome, e-mail e função; do criador, também status, @, código e motivo da recusa ou do bloqueio | RF-05, RF-10, RF-13, RF-17 |
| `PUT` | `/eu/usuario-tiktok` | criador | informa ou corrige o @ da própria conta | RF-09 |
| `GET` | `/campanhas` | criador, operador, gravadora | lista as campanhas com o status; para o criador, marca as que ele participa | RF-22 |
| `GET` | `/campanhas/{id}` | criador, operador, gravadora | dados, regulamento, links, datas e status | RF-23 |
| `POST` | `/campanhas` | operador | cria a campanha | RF-19 |
| `PATCH` | `/campanhas/{id}` | operador | edita textos, links e datas | RF-20 |
| `DELETE` | `/campanhas/{id}` | operador | apaga a campanha sem participante | RF-21 |
| `POST` | `/campanhas/{id}/encerramento` | operador | encerra e grava o ranking final | RF-24 |
| `POST` | `/campanhas/{id}/participacao` | criador | entra na campanha, com o aceite no corpo | RF-25 |
| `GET` | `/campanhas/{id}/resultado` | criador participante, operador, gravadora | ranking, total, contagens, série do gráfico e ganhadores, numa resposta | RF-34, RF-38, RF-39 |
| `GET` | `/campanhas/{id}/exportacao` | operador, gravadora | o resultado em CSV | RF-40 |
| `GET` | `/campanhas/{id}/meus-videos` | criador participante | os vídeos dele na campanha, em todos os status, com as leituras | RF-31 |
| `POST` | `/campanhas/{id}/videos` | criador participante | envia o link de um vídeo | RF-26 |
| `GET` | `/campanhas/{id}/leituras` | operador | a planilha de leituras: os vídeos que contam, com o número atual e se já têm leitura de fechamento | RF-32 |
| `GET` | `/campanhas/{id}/participantes` | operador | lista os participantes da campanha, com @ e status da conta, inclusive quem não tem vídeo que conta; é de onde sai a escolha do ganhador | RF-35 |
| `POST` | `/campanhas/{id}/ganhadores` | operador | registra um ganhador, com o prêmio em texto | RF-35 |
| `GET` | `/videos` | operador | lista vídeos por status; `?status=em_analise` é a fila de conferência | RF-27 |
| `POST` | `/videos/{id}/aprovacao` | operador | aprova, com a data de publicação e as views da primeira leitura, que leva a data de hoje | RF-28 |
| `POST` | `/videos/{id}/recusa` | operador | recusa, com motivo | RF-29 |
| `POST` | `/videos/{id}/desqualificacao` | operador | desqualifica, com motivo | RF-30 |
| `GET` | `/videos/{id}/leituras` | operador | o histórico de leituras do vídeo | RF-33 |
| `PUT` | `/videos/{id}/leituras/{data}` | operador | grava a leitura da data, ou substitui a que existia | RF-32, RF-33 |
| `DELETE` | `/videos/{id}/leituras/{data}` | operador | apaga a leitura da data | RF-33 |
| `PATCH` | `/ganhadores/{id}` | operador | informa a data de entrega | RF-36 |
| `DELETE` | `/ganhadores/{id}` | operador | apaga o registro | RF-37 |
| `GET` | `/criadores` | operador | lista as contas de criador, com o e-mail, o código e, quando o @ foi de conta bloqueada apagada, a data da exclusão; `?status=pendente` é a fila de verificação, só com quem já informou o @ | RF-11, RF-15 |
| `POST` | `/criadores/{id}/aprovacao` | operador | aprova a conta pendente | RF-12 |
| `POST` | `/criadores/{id}/recusa` | operador | recusa a conta pendente, com motivo | RF-12 |
| `PUT` | `/criadores/{id}/usuario-tiktok` | operador | corrige o @ de conta aprovada ou bloqueada | RF-14 |
| `POST` | `/criadores/{id}/bloqueio` | operador | bloqueia, com motivo | RF-16 |
| `POST` | `/criadores/{id}/desbloqueio` | operador | desbloqueia, com motivo | RF-18 |
| `POST` | `/criadores/{id}/exclusao` | operador | apaga a conta, com o e-mail digitado no corpo; se ela estava bloqueada, guarda o @ | RF-41 |
| `GET` | `/acessos` | operador | lista as contas de operador e de gravadora | RF-06, RF-07 |
| `POST` | `/acessos` | operador | cadastra e-mail e função | RF-06 |
| `DELETE` | `/acessos/{id}` | operador | remove o acesso e apaga a conta | RF-07 |
| `GET` | `/saude` | sem login | responde se a api está no ar; o monitor externo e o deploy conferem por ela | RNF-04, ADR-0017 |

## As decisões que não se leem na lista

### Função errada responde 403; dado de outro criador, 404

Cada rota declara a função que a aceita (ADR-0009). Função errada responde
403, com `SEM_PERMISSAO`: a rota existe, e a função não a alcança, sem dado nenhum em
jogo.

Recurso de outro criador e recurso inexistente dão o mesmo resultado, 404, com
`NAO_ENCONTRADO`. Responder 403 exigiria ler a linha alheia para saber que ela
existe, que é a consulta sem filtro que o ADR-0009 proíbe.

O criador que não participa de uma campanha recebe 403 com `NAO_PARTICIPANTE`
no resultado e no envio de vídeo: a campanha ele vê na lista, e o que falta é a
participação.

### O criador bloqueado só alcança a própria conta

Bloqueado, o criador continua entrando (RN-18). As rotas de `auth/` e o
`GET /eu` respondem normalmente, e é pelo `GET /eu` que o frontend sabe que
deve abrir a tela de bloqueio. Toda outra rota responde 403 com
`CONTA_BLOQUEADA`, para a regra não depender de a tela estar certa.

O status da conta não vai no token (ADR-0008), então o bloqueio vale na
requisição seguinte, e não em até 15 minutos.

### O resultado é uma resposta só

`GET /campanhas/{id}/resultado` devolve, de uma vez:

- o ranking: posição, @ ou "participante removido", soma e, de cada vídeo que
  conta, o link (ausente no vídeo do participante removido), o número atual e
  a data dele (RN-45, RN-46, RN-59);
- o total na data de hoje, o número de criadores no ranking e o de vídeos que
  contam (RN-61);
- a série do gráfico: uma entrada por data com leitura, com o total naquela
  data (RN-49);
- os ganhadores: participante, prêmio e data de entrega (RN-54);
- se o ranking é o final (RN-48).

O motivo é consistência, não desempenho. Total, ranking e gráfico saem das
mesmas leituras, e em três chamadas a tela poderia mostrar um total calculado
antes de uma leitura que o ranking ao lado já inclui. Uma resposta é uma chave
de cache e uma invalidação depois de gravar (ADR-0010).

Nenhum campo da resposta tem e-mail ou código de verificação (RN-57, ADR-0005).
A mesma resposta serve ao participante, à gravadora e ao operador.

### A leitura é endereçada pela data, e gravar substitui

`PUT /videos/{id}/leituras/{data}` grava a leitura daquela data, ou substitui a
que existia (RN-40). `PUT`, e não `POST`, porque a data identifica a leitura, e
repetir o envio dá o mesmo resultado. Não há conflito para confirmar: na
planilha de leituras, corrigir o número do dia é o caso comum.

A planilha (`GET /campanhas/{id}/leituras`) grava linha a linha, uma chamada
por vídeo. Uma chamada em lote exigiria decidir o que fazer quando uma das 150
linhas falha, e linha a linha a recusa aparece na linha certa.

### Entrar de novo na campanha devolve a participação que existe

`POST /campanhas/{id}/participacao` de quem já participa responde 200 com a
participação existente, em vez de recusar. O caso real é o duplo clique, ou duas
abas: as duas requisições passam pela conferência antes de qualquer gravação, e
a segunda esbarra na chave única do banco. O tratador traduz essa violação na
mesma resposta, e nenhum código de erro novo é preciso.

### O status da campanha é calculado, não gravado

A resposta traz o status (RN-25), calculado a cada leitura a partir das datas
e do encerramento. Gravar o status faria a campanha precisar de alguém que o
mudasse à meia-noite.

### A exclusão de conta é um POST, não um DELETE

A confirmação exige o e-mail digitado (UC-19), e ele vai no corpo. `DELETE` com
corpo tem semântica indefinida em HTTP, e na URL o e-mail iria parar no log de
acesso (RNF-16, RNF-17).

### O CSV é para planilha brasileira

Separador ponto e vírgula, codificação UTF-8 com marca de ordem de bytes, datas
em `DD/MM/AAAA` e views como inteiro sem separador, que é o que o Excel em
português abre direto. Célula que começaria com `=`, `+`, `-` ou `@` recebe um
apóstrofo na frente, para a planilha não a executar como fórmula.

### O retorno do login volta como redirecionamento

O `GET /auth/callback` não é chamado pela aplicação: o navegador chega nele
vindo do Google, numa navegação de página inteira, e um erro em JSON ali
apareceria cru na tela. Por isso o retorno sempre termina em redirecionamento:
no caminho feliz, para a aplicação; em qualquer falha, para a tela de entrada
com o motivo na URL.

| Motivo | Quando | Caso de uso |
|:--|:--|:--|
| `LOGIN_NAO_CONCLUIDO` | a pessoa cancelou no Google, ou a verificação do retorno falhou | UC-01 2a |
| `EMAIL_NAO_VERIFICADO` | o Google não devolveu o e-mail como verificado | UC-01 2b |

### A renovação tem rota própria

O token de renovação só viaja para as rotas de `auth/`. A renovação o troca por
um token de acesso novo, e o logout o usa para achar a linha no banco e
apagá-la. Quando uma chamada volta 401, o cliente HTTP do frontend chama a
renovação uma vez e repete a chamada.

## O que não tem endpoint, e por quê

| O quê | Por quê |
|:--|:--|
| Cadastro do primeiro operador | antes dele não existe quem chame a rota; é comando de linha (RN-07) |
| Migração de esquema | passo do deploy (ADR-0006) |
| Backup | agendamento na VPS e na máquina do operador (ADR-0016) |
| Total, ranking ou gráfico separados | vêm no resultado, e separados permitiriam tela inconsistente |
| Qualquer rota que emita sessão sem autenticar | recusado no ADR-0013 |
| Upload de arquivo | o sistema não recebe arquivo (RN-23) |

## Códigos de status

| Código | Quando |
|:--|:--|
| 200 | leitura ou alteração bem-sucedida |
| 201 | campanha, participação, vídeo, ganhador ou acesso criado |
| 204 | exclusão bem-sucedida |
| 400 | valor recusado por regra (link, datas, views, motivo ausente, aceite ausente, data futura) |
| 401 | sem token de acesso válido; na renovação, sem token de renovação válido |
| 403 | função sem permissão, criador bloqueado, ou criador fora da campanha |
| 404 | recurso que não existe, ou de outro criador, sem distinguir os dois |
| 409 | o status atual não permite a ação (conta, vídeo, campanha encerrada, prazo, leitura de fechamento, pendência de encerramento, @ ou link em uso) |
| 422 | corpo malformado, antes de chegar à regra de negócio |
| 500 | erro inesperado, com `ERRO_INTERNO` |
