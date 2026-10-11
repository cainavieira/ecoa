# Auditoria de coerência

- **Data:** 10-10-2026
- **Modo:** completa, nas quatro frentes (regras, contrato, decisões,
  identificadores)
- **Auditado:** `64d5399`, a especificação inteira recém-escrita
- **Correções:** `fea1dc2`
- **Estado:** dez achados corrigidos; dos dois que esperavam decisão, um é
  risco aceito e o outro tem caminho escolhido em 11-10, ainda não aplicado

Oito agentes: um auditor e um cético por frente. Os auditores acharam 31
defeitos; o cético derrubou 16. Os 15 confirmados se reduzem a 12 problemas,
porque três apareceram em duas frentes. Cada um foi conferido nos arquivos antes
de entrar aqui.

## Corrigidos em `fea1dc2`

Os que tinham correção única, alinhando o texto à intenção já escrita.

| Problema | Correção |
|:--|:--|
| A RN-06 dizia que sempre existe um operador, mas só impedia a autorremoção: dois operadores que se removem um ao outro deixam o sistema sem nenhum | A remoção que deixaria o sistema sem operador é recusada (`ULTIMO_OPERADOR`), com a contagem e a remoção na mesma transação, com bloqueio. RN-06, RF-07, UC-18 4d, `validarRemocaoDeAcesso` |
| Uma leitura com data entre o fechamento e a leitura de fechamento já gravada era aceita, e o ranking final passava a divergir do número atual | Cada vídeo tem no máximo uma leitura a partir do fechamento; trocar a data exige apagar a de fechamento antes. RN-41, UC-11 2f, catálogo, fronteira na instrucao |
| O operador corrigia o @ de conta aprovada sem verificar o @ novo, e os vídeos do @ novo passavam como sendo do criador | Antes de corrigir, o operador confere o código na bio do @ novo. RN-15, UC-03 4a e 4b |
| A RN-34, ao pé da letra ("domínio termina em tiktok.com"), aceitava `eviltiktok.com` | O domínio é `tiktok.com` ou termina em `.tiktok.com`. RN-34 e catálogo |
| A fronteira do encerramento na instrucao dizia que vídeo de criador bloqueado não pende, contra a RN-50, para o vídeo em análise | Só o vídeo **aprovado** de criador bloqueado não pende |
| A convenção proibia o código de verificação para todos, mas o operador precisa dele para conferir a bio (RF-11) | O DTO servido a outro criador ou à gravadora não tem e-mail nem código; o operador recebe o código na fila |
| Nenhuma rota listava os participantes, e o ganhador pode ser participante sem vídeo que conta | `GET /campanhas/{id}/participantes`, para o operador; UC-15 e T-22 |
| O segundo `POST` de participação, no duplo clique, não tinha resposta definida | A rota é idempotente: devolve a participação que existe |
| O aviso de homem morto ficava quieto se o dump da VPS parasse, porque o dump velho continuava restaurando bem | O agendamento só avisa com dump do próprio dia, e o ensaio recusa dump com mais de um dia. RNF-08, armadilha na instrucao, T-36 |
| A integração contínua não checava tipos do TypeScript, ESLint nem a regeneração dos tipos do OpenAPI, e um campo renomeado na api quebraria só em produção | As três verificações entram em "Antes de integrar", nas convenções do frontend e na T-02 |
| Casos de uso, requisitos não funcionais e atores não tinham seção de aposentados, que o `CLAUDE.md` diz que todo documento tem | A seção entra nos três, vazia |

## Esperam decisão

**1. O operador removido ainda age por até 15 minutos.** O papel vai no token, e
o token de acesso vale até 15 minutos depois da remoção (ADR-0008). Nesse
intervalo, o operador removido pode se cadastrar de novo pela própria tela,
porque a conta dele já foi apagada e a RN-04 aceita o e-mail. A correção da
RN-06 impede que ele zere os operadores, mas não isso. Fecha se as rotas de
operador e de gravadora conferirem no banco, a cada requisição, que a conta
ainda existe: com duas ou três pessoas operando, a consulta não pesa. Muda uma
consequência do ADR-0008, e ADR não se reescreve: ou ADR novo que o supera, ou
exceção sua para corrigir no lugar, já que os ADR foram escritos hoje e você
ainda não os revisou.

**2. O link do vídeo identifica o participante removido.** A RN-59 mantém os
vídeos das campanhas encerradas como "participante removido", e o UC-19 promete
que nenhum dado pessoal fica. Mas o link do vídeo continua no resultado e no
CSV, e o link completo do TikTok leva à conta do @. Duas saídas:

- apagar também o link desses vídeos, mantendo os números: o ranking final e o
  relatório continuam iguais, e o vídeo aparece sem link;
- manter o link e corrigir a RN-59 e o UC-19 para dizer que ele fica, e por quê.

A primeira cumpre o que a exclusão promete. A leitura de LGPD é de advogado.

**Junto do item 1:** o ADR-0009 diz que o e-mail e o código "não saem para mais
ninguém", e o operador precisa do código (RF-11). A convenção já foi corrigida;
a frase do ADR depende da mesma escolha entre ADR novo e correção no lugar.

## Decidido em 11-10

- **Item 1:** risco aceito, sem conserto. Exige operador removido agindo de
  má-fé dentro de 15 minutos, e a volta dele aparece na lista de acessos. O
  ADR-0008 fica como está.
- **ADR-0009:** a frase foi corrigida no lugar, por exceção do interessado à
  regra de não reescrever ADR. Não é defeito em auditoria futura.
- **Item 2:** caminho (c), apresentado na conversa depois desta auditoria:
  apagar o link dos vídeos do participante removido, como o resto do dado
  pessoal, e guardar, só para a operação, um registro mínimo da conta apagada
  (o @ e se estava bloqueada), para reconhecer o bloqueado que volta com conta
  nova. O interessado respondeu "concordo"; a leitura como (c) se confirma na
  próxima sessão, junto com quanto tempo esse registro fica guardado. Não
  aplicado: mexe em regras, casos de uso, requisitos não funcionais, esquema e
  contrato, e pede auditoria desde o commit em que entrar.

## Derrubados pelo cético

- Regras: a UC-03 3a não teria ação que libere o @ da conta errada. A RF-14
  libera.
- Regras: o primeiro operador ficaria sem saída se já tivesse conta de criador.
  O comando aceita outro e-mail.
- Contrato: campo obrigatório ausente teria dois status. O 422 é corpo
  malformado, e motivo ausente é 400.
- Contrato: o catálogo não teria código para 401 e 422. Eles não vêm de regra.
- Contrato: `USUARIO_TIKTOK_FIXO` para conta bloqueada nunca seria emitido. A
  precedência do `CONTA_BLOQUEADA` está escrita no contrato.
- Contrato: o frontend precisaria do "hoje" do servidor. Valor padrão de
  formulário não decide prazo.
- Contrato: o `GET /auth/callback` muda dado. A exceção está no próprio ADR-0008.
- Contrato: `meus-videos` de quem não participa e @ de pendente corrigido pelo
  operador sem código. Detalhe de implementação.
- Contrato: `CONFIRMACAO_NAO_CONFERE` cita a RN-59. Imprecisão de rastreio.
- Decisões: a janela por vídeo voltaria pela RN-41. A tolerância é necessária
  para vídeo aprovado depois do fechamento.
- Decisões: o teto em criadores não limitaria o trabalho de leitura. O custo já
  está no ADR-0001.
- Decisões: a planilha com hoje como padrão usaria o relógio do aparelho. Não
  decide prazo.
- Decisões: exclusões feitas depois do último dump voltariam numa restauração.
  A perda de 24 horas é aceita, e o pedido externo serve de rastro.
- Decisões: "nenhum GET muda dado", no ADR-0008. Redação.
- Decisões: a checagem do controller no ArchUnit atribuída ao ADR-0012. Está na
  convenção.
- Decisões: a marca do fechamento no gráfico, sem regra que a peça. Citação
  imprecisa, sem efeito na escolha do Recharts.

## Observação

Dois derrubados deixam melhoria opcional, sem defeito: a RNF-14 diria melhor
"antes do dump restaurado" do que "antes do problema", e falta escrever o
procedimento de restauração, que refaz as exclusões pedidas depois do dump.
