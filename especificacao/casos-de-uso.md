# Casos de uso

Cada caso de uso descreve uma interação completa, do gatilho ao estado final.

O fluxo principal é o caminho sem erro. Os **fluxos alternativos** são o valor
deste documento: eles dizem o que acontece quando algo dá errado, e são o
lugar onde as regras são testadas contra a realidade em vez de assumidas.

Atores em `atores.md`, requisitos em `requisitos-funcionais.md`, regras em
`regras-de-negocio.md`.

## UC-01 Entrar no sistema

**Ator:** AT-01, AT-02, AT-03

**Fluxo principal**

1. A pessoa abre o endereço do sistema, lê o aviso de que entrar implica aceitar
   os termos de uso e a política de privacidade, e escolhe entrar com a conta
   Google. (RN-09)
2. O AT-04 autentica e devolve nome e e-mail. (RF-01)
3. O sistema procura a conta daquele e-mail. (RN-01)
4. Existindo conta, o sistema abre a sessão e apresenta a tela do papel: o
   criador vê as campanhas e a situação da conta; o operador, a operação; a
   gravadora, o painel. (RF-05)
5. Não existindo, o sistema cria conta de criador pendente, com a data do
   aceite, e pede o @ (UC-02). (RF-02, RN-03, RN-10)

**Fluxos alternativos**

- **2a** A pessoa cancela na tela do Google, ou a verificação do retorno falha:
  o sistema volta à tela de entrada dizendo que o login não foi concluído e que
  dá para tentar de novo, sem abrir sessão. (RF-01)
- **2b** O AT-04 não devolve o e-mail como verificado: o sistema volta à tela de
  entrada dizendo que a conta Google precisa ter o e-mail verificado, e nenhuma
  conta é criada. (RF-01, RN-01)
- **2c** O AT-04 está indisponível: nenhum login novo acontece. Sessão já
  aberta continua válida.
- **4a** A conta foi cadastrada por um operador e este é o primeiro acesso: o
  sistema completa o nome vindo do AT-04 e abre a tela do papel cadastrado.
  (RF-03, RN-03)
- **4b** A conta de criador está bloqueada: o sistema abre a tela de bloqueio, e
  só ela. (RF-17, RN-18)
- **4c** A conta de criador está pendente sem @, ou recusada: a tela pede o @ ou
  mostra o motivo da recusa (UC-02). (RF-13)
- **5a** A pessoa já teve conta de criador apagada: recebe conta nova, sem nada
  da anterior. (RN-60)
- **5b** A pessoa teve o acesso de operador ou de gravadora removido: recebe
  conta de criador, como qualquer outra. (RN-05)

**Pós-condição:** sessão aberta, ou nenhuma mudança de estado.

## UC-02 Informar o @ e pedir verificação

**Ator:** AT-01
**Pré-condição:** conta pendente ou recusada.

**Fluxo principal**

1. O criador informa o @ do TikTok. (RF-09)
2. O sistema guarda o @ sem o arroba e em minúsculas, e apresenta o código de
   verificação com a instrução: pôr o código na bio do TikTok e esperar a
   conferência. (RF-10, RN-11, RN-12)
3. A conta fica pendente, na fila do operador (UC-03). (RF-11)

**Fluxos alternativos**

- **1a** O @ vem com arroba ou com maiúsculas: aceito, e guardado sem o arroba e
  em minúsculas. (RN-11)
- **1b** O @ está vazio ou tem espaço: recusado. (RN-11)
- **1c** O @ já pertence a outra conta: aceito, porque a posse se prova pelo
  código na bio, e a conferência decide (UC-03 3a e 3b). (RN-11)
- **1d** A conta está recusada: corrigir o @ a devolve a pendente, e ela volta à
  fila. (RN-14)
- **1e** A conta está aprovada ou bloqueada: o criador não edita o @. Quem
  trocou de @ no TikTok pede ao operador, por fora (UC-03 4a). (RN-15)
- **2a** O criador não põe o código na bio: a conta segue pendente até a
  conferência, que a recusa com esse motivo (UC-03 2a).

**Pós-condição:** conta pendente, com @.

## UC-03 Verificar conta de criador

**Ator:** AT-02
**Pré-condição:** existe conta pendente que já informou o @.

**Fluxo principal**

1. O operador abre a fila de contas pendentes, com o @ e o código de cada uma.
   (RF-11)
2. Abre o perfil público do @ no TikTok, por fora do sistema, e confere se o
   código está na bio e se a conta parece de pessoa real. (RN-12, RN-13)
3. Aprova. A conta passa a aprovada, e o criador pode entrar em campanha.
   (RF-12, RN-16)

**Fluxos alternativos**

- **1a** O @ foi de conta bloqueada e apagada: a fila mostra o aviso, com a
  data da exclusão. A aprovação continua possível, e o operador decide olhando
  o perfil. (RF-11, RN-65)
- **2a** O código não está na bio: o operador recusa, com o motivo. A conta fica
  recusada, e o criador vê o motivo na tela dele. (RF-12, RF-13, RN-13)
- **2b** O @ não existe no TikTok: recusa, com o motivo. (RN-13)
- **2c** A conta parece de robô ou de outra pessoa: recusa, com o motivo. Não há
  critério numérico. (RN-13)
- **2d** O operador tenta recusar sem motivo: o sistema não aceita. (RN-13)
- **3a** O @ já pertence a outra conta aprovada ou bloqueada: o sistema recusa a
  aprovação. O operador confere qual das duas tem o código na bio e age sobre a
  errada. (RN-11, RN-13)
- **3b** Duas contas pendentes têm o mesmo @: só o dono consegue pôr o código na
  bio. O operador aprova essa e recusa a outra. (RN-11, RN-12)
- **4a** O criador aprovado trocou de @ no TikTok e pediu a correção por fora: o
  operador confere o código de verificação na bio do @ novo e corrige o @ na
  lista de contas. O @ novo não pode pertencer a outra conta aprovada ou
  bloqueada. (RF-14, RF-15, RN-11, RN-12, RN-15)
- **4b** O código não está na bio do @ novo: o operador não corrige, e avisa o
  criador por fora. A conta continua aprovada com o @ antigo. (RN-15)
- **4c** O @ novo foi de conta bloqueada e apagada: a correção acontece, e a
  conta passa a aparecer com o aviso na lista de contas. O operador decide se
  a bloqueia (UC-04). (RF-15, RN-17, RN-65)

**Pós-condição:** conta aprovada ou recusada.

## UC-04 Bloquear e desbloquear criador

**Ator:** AT-02
**Pré-condição:** conta aprovada, para bloquear; bloqueada, para desbloquear.

**Fluxo principal**

1. O operador escolhe a conta na lista de contas e pede o bloqueio. (RF-15,
   RF-16)
2. Informa o motivo e confirma. (RN-17)
3. A conta fica bloqueada, e o criador sai do ranking das campanhas não
   encerradas. (RN-19)

**Fluxos alternativos**

- **1a** O criador, no próximo acesso ou na próxima tela que abrir, vê só a
  tela de bloqueio, com o motivo e o contato da operação para contestar. A
  contestação acontece fora do sistema. (RF-17, RN-18)
- **1b** A conta não está aprovada: o bloqueio não está disponível. Para conta
  pendente, o caminho é recusar (UC-03). (RN-17)
- **2a** O operador não informa o motivo: o sistema não aceita. (RN-17)
- **3a** O criador tem vídeo em análise: o vídeo continua em análise e não pode
  ser aprovado enquanto durar o bloqueio. Pode ser recusado. (RN-19, RN-37)
- **3b** O criador já é ganhador registrado: o registro continua. Apagá-lo é
  decisão separada (UC-15 4a). (RN-20)
- **3c** O criador está no ranking final de campanha encerrada: o ranking final
  não muda. (RN-48)
- **4a** Desbloquear: o operador escolhe a conta bloqueada, informa o motivo e
  confirma. A conta volta a aprovada, e o criador volta ao ranking das campanhas
  não encerradas com os vídeos e as leituras que tinha. (RF-18, RN-21)
- **4b** O operador desbloqueia sem motivo: o sistema não aceita. (RN-21)
- **4c** Uma campanha foi encerrada durante o bloqueio: o criador não volta ao
  ranking final dela. (RN-48)

**Pós-condição:** conta bloqueada, ou de volta a aprovada.

## UC-05 Criar campanha

**Ator:** AT-02

**Fluxo principal**

1. O operador informa o nome da música, o artista, o regulamento escrito pela
   gravadora e ao menos um link de referência. (RF-19, RN-22, RN-23)
2. Informa o início, o fim e o fechamento. (RN-24)
3. Confirma. A campanha aparece na lista, com a situação derivada das datas.
   (RF-22, RN-25)

**Fluxos alternativos**

- **1a** Nenhum link de referência: recusado. (RN-22)
- **1b** Link que não é endereço http ou https: recusado. (RN-23)
- **2a** Datas fora da ordem início, fim, fechamento: recusado. (RN-24)
- **2b** Fechamento igual ou anterior a hoje: recusado. (RN-26)
- **2c** Fim igual ao fechamento: aceito, sem estabilização. (RN-24)
- **2d** Início no passado: aceito, e a campanha já nasce aberta ou em apuração,
  conforme as datas. (RN-25)
- **3a** Já existe outra campanha não encerrada: aceito. Uma por vez é premissa
  de operação, não regra. (RN-29, RNF-01)

**Pós-condição:** campanha criada, com datas em ordem e fechamento futuro.

## UC-06 Editar campanha

**Ator:** AT-02
**Pré-condição:** campanha não encerrada.

**Fluxo principal**

1. O operador abre a campanha e altera textos, links ou datas. (RF-20)
2. Confirma.

**Fluxos alternativos**

- **1a** A campanha está encerrada: nada é editável, exceto os ganhadores
  (UC-15). (RN-28)
- **2a** O início ou o fim novo deixaria um vídeo aprovado com a data de
  publicação de fora: recusado, dizendo qual vídeo. (RN-27)
- **2b** O fechamento novo é igual ou anterior a hoje: recusado. (RN-26)
- **2c** Datas fora de ordem: recusado. (RN-24)
- **2d** Todos os links removidos: recusado. (RN-22)
- **2e** O fechamento é adiado quando já havia leituras de fechamento: elas
  passam a parciais, porque a leitura de fechamento é derivada da data, e o
  operador lê de novo no fechamento novo. (RN-26, RN-41)
- **2f** O fim é adiado com a campanha em apuração: a campanha volta a aberta, e
  o envio de vídeo reabre. (RN-25, RN-34)
- **2g** O fim é antecipado para antes de hoje: a campanha passa a em apuração,
  e o envio fecha. Vídeo em análise publicado depois do fim novo é recusado na
  conferência (UC-10 2b). (RN-25, RN-36)

**Pós-condição:** todo vídeo aprovado continua com a publicação entre início e
fim. (RN-27)

## UC-07 Apagar campanha

**Ator:** AT-02

**Fluxo principal**

1. O operador pede para apagar a campanha.
2. O sistema confere que ela não tem participante e pede confirmação. (RF-21,
   RN-30)
3. Confirmada, a campanha é removida.

**Fluxos alternativos**

- **2a** A campanha tem participante: recusado. (RN-30)

**Pós-condição:** campanha removida, ou nenhuma mudança.

## UC-08 Entrar na campanha

**Ator:** AT-01
**Pré-condição:** conta aprovada.

**Fluxo principal**

1. O criador abre a campanha e lê o regulamento, os links de referência e as
   datas. (RF-23)
2. Escolhe participar, e o sistema mostra o que o aceite implica: o @ e as views
   dele aparecem para os outros participantes e para a gravadora. (RN-31)
3. Aceita. A participação é registrada com a data do aceite. (RF-25)

**Fluxos alternativos**

- **1a** A conta está pendente ou recusada: o criador vê a campanha, e
  participar fica indisponível, com o motivo. (RN-16)
- **2a** A campanha está em apuração ou encerrada: participar não está
  disponível. (RN-32)
- **3a** O criador não aceita: nada é registrado.
- **3b** O criador já participa: a campanha aparece como sua, e participar não
  aparece de novo. (RF-22)

**Pós-condição:** o criador é participante da campanha.

## UC-09 Enviar vídeo

**Ator:** AT-01
**Pré-condição:** participante, com a campanha aberta.

**Fluxo principal**

1. O criador posta o vídeo no TikTok, por fora do sistema, com o som da
   campanha.
2. Cola o link no sistema e envia. (RF-26)
3. O vídeo fica em análise e aparece na lista dele com essa situação. (RN-33,
   RF-31)

**Fluxos alternativos**

- **2a** O link não é https ou não é de `tiktok.com`: recusado. (RN-34)
- **2b** O link já foi enviado e não foi recusado: recusado, dizendo que o vídeo
  já está na plataforma. (RN-35)
- **2c** O link já foi enviado e recusado: aceito, como envio novo. (RN-35)
- **2d** A campanha está agendada ou em apuração: o envio não está disponível.
  (RN-34)
- **2e** É o mesmo vídeo de outro envio, com outro link: o sistema aceita, e o
  operador recusa na conferência (UC-10 2d). (RN-35)
- **3a** O criador quer enviar mais vídeos: repete o fluxo, sem limite. (RN-34)

**Pós-condição:** vídeo em análise.

## UC-10 Conferir vídeo

**Ator:** AT-02
**Pré-condição:** existe vídeo em análise.

**Fluxo principal**

1. O operador abre a fila de vídeos em análise. (RF-27)
2. Abre o link, por fora do sistema, e confere: o vídeo é público, é da conta do
   @ verificado, usa o som da campanha e cumpre o regulamento. (RN-36)
3. Informa a data de publicação e as views que a página mostra, e aprova. O
   vídeo passa a aprovado, com a primeira leitura, e entra no ranking. (RF-28,
   RN-36, RN-39, RN-45)

**Fluxos alternativos**

- **2a** O vídeo não cumpre (privado, de outra conta, com outro som, fora do
  regulamento): o operador recusa, com o motivo. O criador vê o motivo e, com a
  campanha aberta, pode enviar outro. (RF-29, RN-37)
- **2b** A data de publicação fica fora do início e do fim: o sistema recusa a
  aprovação, e o operador recusa o vídeo com esse motivo. (RN-36)
- **2c** O operador tenta recusar sem motivo: o sistema não aceita. (RN-37)
- **2d** É o mesmo vídeo de outro envio, com outro link: recusa, com esse motivo.
  (RN-35)
- **3a** O criador está bloqueado: o sistema recusa a aprovação enquanto durar o
  bloqueio. O operador espera ou recusa o vídeo. (RN-19)
- **3b** Views negativas ou não inteiras: recusado. (RN-39)
- **3c** A campanha já passou do fim: a aprovação acontece normalmente, porque o
  que vale é a data de publicação. (RN-36)

**Pós-condição:** vídeo aprovado, com uma leitura, ou recusado, com motivo.

## UC-11 Registrar leituras

**Ator:** AT-02
**Pré-condição:** campanha não encerrada, com vídeo que conta.

**Fluxo principal**

1. O operador abre a tela de leituras da campanha: uma linha por vídeo que
   conta, com o @, o link, o número atual e a data dele. (RF-32, RN-43, RN-45)
2. Abre cada vídeo no TikTok, por fora do sistema, e digita as views na linha,
   com a data de hoje como padrão. (RN-39, RN-40)
3. Cada linha é gravada ao confirmar, e o ranking passa a usar o número novo.
   (RN-45)

**Fluxos alternativos**

- **1a** O criador está bloqueado: os vídeos dele não aparecem na tela, porque
  não contam. (RN-19, RN-45)
- **1b** A campanha está encerrada: leitura não se registra nem se corrige.
  (RN-28)
- **2a** Data futura: recusada. (RN-40)
- **2b** Data anterior à publicação do vídeo: recusada. (RN-40)
- **2c** Já existe leitura naquela data: a nova substitui. (RN-40)
- **2d** Views menores que as da leitura anterior: aceitas, porque as redes
  removem views falsas. (RN-39)
- **2e** Data igual ou posterior ao fechamento: é a leitura de fechamento do
  vídeo. (RN-41)
- **2f** O vídeo já tem leitura de fechamento, e a data nova também é igual ou
  posterior ao fechamento, mas diferente da dela: recusada. Para corrigir o
  número, o operador grava de novo na mesma data; para trocar a data, apaga a
  leitura de fechamento antes. (RN-41, RN-42)
- **2g** O vídeo saiu do ar, ficou privado ou a conta foi banida: o operador o
  desqualifica (UC-12). (RN-38)
- **2h** O operador digitou errado: corrige ou apaga a leitura, no histórico do
  vídeo. (RF-33, RN-42)
- **2i** Todas as leituras de um vídeo foram apagadas: ele conta zero e mostra
  que não tem leitura. (RN-43)

**Pós-condição:** leituras gravadas, ranking atualizado.

## UC-12 Desqualificar vídeo

**Ator:** AT-02
**Pré-condição:** vídeo aprovado, em campanha não encerrada.

**Fluxo principal**

1. O operador escolhe o vídeo, informa o motivo e confirma. (RF-30, RN-38)
2. O vídeo sai do ranking. O criador vê o motivo e não é bloqueado. (RN-38,
   RN-56)

**Fluxos alternativos**

- **1a** O operador não informa o motivo: o sistema não aceita. (RN-38)
- **1b** A campanha está encerrada: não há desqualificação. (RN-28)
- **2a** Era o único vídeo que contava do criador: ele sai do ranking. (RN-45)
- **2b** O vídeo volta ao ar depois: a desqualificação é definitiva, e o mesmo
  link não entra de novo. Com a campanha aberta, o criador pode enviar outro
  vídeo. (RN-35, RN-38)

**Pós-condição:** vídeo desqualificado, fora do ranking.

## UC-13 Acompanhar a campanha

**Ator:** AT-01
**Pré-condição:** participante da campanha.

**Fluxo principal**

1. O criador abre a campanha.
2. Vê os próprios vídeos, com a situação, o motivo de recusa ou
   desqualificação, o número atual e o gráfico das leituras. (RF-31)
3. Vê o ranking: posição, @ e soma de cada participante, com os vídeos de cada
   um embaixo. (RF-34, RN-45, RN-46)
4. Vê os ganhadores registrados. (RF-38, RN-54)

**Fluxos alternativos**

- **1a** O criador não participa: vê a campanha, sem ranking nem ganhadores.
  (RN-47)
- **2a** Vídeo em análise, recusado ou desqualificado: aparece só para ele, fora
  do ranking. (RN-45)
- **3a** Nenhum vídeo dele conta ainda: ele não aparece no ranking, mas vê o dos
  outros. (RN-45)
- **3b** Dois criadores com a mesma soma: ficam na mesma posição. (RN-46)
- **3c** Cada número aparece com a data da leitura, para ninguém achar que é ao
  vivo. (RN-43)
- **3d** A campanha está encerrada: o ranking é o final. (RN-48)
- **4a** O criador ganhou: o prêmio aparece na tela dele. É o aviso. (RN-56)

**Pós-condição:** nenhuma mudança de estado.

## UC-14 Encerrar campanha

**Ator:** AT-02
**Pré-condição:** hoje é igual ou posterior ao fechamento.

**Fluxo principal**

1. O operador pede para encerrar a campanha.
2. O sistema confere que nenhum vídeo está em análise e que todo vídeo que conta
   tem leitura de fechamento. (RN-50)
3. Confirmado, o ranking final é gravado, e a campanha fica encerrada. (RF-24,
   RN-48)

**Fluxos alternativos**

- **1a** Antes do fechamento: encerrar não está disponível. (RN-50)
- **2a** Há vídeo em análise: recusado, listando os vídeos. O operador confere
  cada um (UC-10). (RN-50)
- **2b** O vídeo em análise é de criador bloqueado: ele não pode ser aprovado
  (RN-19). O operador o recusa, ou desbloqueia o criador antes de conferir.
- **2c** Há vídeo que conta sem leitura de fechamento: recusado, listando os
  vídeos. O operador faz a leitura (UC-11). (RN-41, RN-50)
- **2d** Há criador bloqueado: os vídeos dele não contam e não pedem leitura de
  fechamento, e ele fica fora do ranking final. (RN-45, RN-48)
- **3a** Depois de encerrar, o operador exporta o CSV (UC-17), como prática de
  operação. (RNF-10)
- **3b** Os ganhadores ainda podem ser registrados ou apagados (UC-15). (RN-52)

**Pós-condição:** campanha encerrada, com ranking final gravado e imutável.
(RN-28, RN-48)

## UC-15 Registrar ganhadores e entrega

**Ator:** AT-02
**Pré-condição:** campanha a partir do início.

**Fluxo principal**

1. O operador escolhe o participante na lista de participantes da campanha,
   conforme o regulamento e o ranking, e escreve o prêmio. (RF-35, RN-51)
2. Confirma. O ganhador aparece para quem vê o ranking. (RN-54)
3. Quando a gravadora entrega o prêmio, por fora do sistema, o operador
   informa a data de entrega. (RF-36, RN-55)

**Fluxos alternativos**

- **1a** O participante está bloqueado: recusado. (RN-52)
- **1b** Quem não participa da campanha não aparece para escolha. (RN-52)
- **1c** A campanha ainda não começou: registrar não está disponível. (RN-52)
- **1d** O criador já ganhou outro prêmio na campanha: aceito. (RN-52)
- **1e** A campanha está encerrada: aceito. (RN-52)
- **3a** Data de entrega futura: recusada. (RN-52)
- **4a** O operador apaga um registro: o ganhador some da lista. (RF-37, RN-53)
- **4b** O ganhador teve a conta apagada, em campanha encerrada: o prêmio
  continua, como "participante removido". (RN-59)

**Pós-condição:** ganhador registrado, com ou sem data de entrega.

## UC-16 Acompanhar pelo painel

**Ator:** AT-03, e o AT-02 vê o mesmo painel

**Fluxo principal**

1. A pessoa abre o painel e vê a lista de campanhas, com a situação de cada
   uma. (RF-22)
2. Escolhe uma, e vê o total de views na data de hoje, o número de criadores no
   ranking, o número de vídeos que contam, o ranking, o gráfico do total e os
   ganhadores. (RF-39, RN-61)

**Fluxos alternativos**

- **1a** Não existe campanha: a lista informa isso.
- **2a** A campanha está agendada, ou nenhum vídeo conta ainda: números em zero
  e ranking vazio, em vez de erro.
- **2b** A campanha está encerrada: os números são os do ranking final. (RN-48)
- **2c** Um participante teve a conta apagada, em campanha encerrada: aparece
  como "participante removido", com os vídeos sem link. (RN-59)
- **2d** A gravadora não tem ação de escrita: o painel só lê. (RF-05)

**Pós-condição:** nenhuma mudança de estado.

## UC-17 Exportar o resultado

**Ator:** AT-03, AT-02

**Fluxo principal**

1. No painel da campanha, a pessoa pede a exportação. (RF-40)
2. Recebe um CSV com uma linha por vídeo que conta. (RN-62)

**Fluxos alternativos**

- **2a** Nenhum vídeo conta: o arquivo sai só com o cabeçalho, em vez de erro.
  Arquivo vazio é resposta válida para campanha sem resultado.
- **2b** A campanha está encerrada: os números são os do ranking final. (RN-62)
- **2c** Participante removido: aparece assim, no lugar do @, e a linha vai sem
  link. (RN-62)

**Pós-condição:** nenhuma mudança de estado.

## UC-18 Cadastrar e remover acesso

**Ator:** AT-02

**Fluxo principal**

1. O operador informa um e-mail e o papel: operador ou gravadora. (RF-06,
   RN-03)
2. A conta fica cadastrada, à espera do primeiro acesso da pessoa (UC-01 4a).
   (RF-03)
3. O operador avisa a pessoa por fora e passa o endereço do sistema. (RN-56)

**Fluxos alternativos**

- **1a** O e-mail já tem conta de criador: recusado. Para mudar o papel, a conta
  de criador é apagada antes (UC-19). (RN-04)
- **1b** O e-mail já tem conta de operador ou de gravadora: recusado, dizendo
  que já está cadastrado. Para trocar o papel, o acesso é removido antes (4a).
  (RN-04)
- **1c** O e-mail vem com maiúsculas: guardado em minúsculas. (RN-01)
- **4a** Remover: o operador remove o acesso, e a conta é apagada. A sessão
  aberta da pessoa cai quando o token de acesso vencer, em no máximo 15
  minutos. (RF-07, RN-05, ADR-0008)
- **4b** O operador tenta remover a própria conta: recusado. (RN-06)
- **4c** A pessoa removida entra de novo: recebe conta de criador. (RN-05)
- **4d** A remoção deixaria o sistema sem nenhum operador, como quando dois
  operadores removem um ao outro ao mesmo tempo: recusada. (RN-06)

**Pós-condição:** conta de operador ou gravadora cadastrada, ou removida.

## UC-19 Apagar conta de criador

**Ator:** AT-02
**Pré-condição:** o criador pediu a exclusão, por fora do sistema. (RN-58)

**Fluxo principal**

1. O operador escolhe a conta na lista de contas e pede a exclusão. (RF-15,
   RF-41)
2. O sistema explica o que será removido e exige que o operador digite o e-mail
   da conta para confirmar.
3. Confirmado, a conta é apagada. (RN-59)

**Fluxos alternativos**

- **2a** O texto digitado não confere: nada acontece.
- **3a** O criador participa de campanha não encerrada: a participação, os
  vídeos, as leituras e os prêmios dele nessa campanha somem, e o ranking dela
  se recalcula sem ele. (RN-59)
- **3b** O criador participou de campanha encerrada: ali ele permanece como
  "participante removido", com a posição, os números e os prêmios, e os vídeos
  perdem o link. (RN-59)
- **3c** A pessoa entra de novo: recebe conta nova, pendente, sem nada da
  anterior. (RN-60)
- **3d** As cópias de backup ainda têm a conta até o fim da retenção. (RNF-14)
- **3e** A conta estava bloqueada: o @ fica guardado, sem prazo, para o aviso
  na verificação e na lista de contas. (RN-65)

**Pós-condição:** nenhum dado pessoal do criador permanece no banco, além do @
da conta bloqueada. (RN-59, RN-65)

## UC-20 Cadastrar o primeiro operador

**Ator:** AT-02
**Observação:** acontece fora das telas, por comando de linha, porque antes do
primeiro operador não existe quem cadastre. (RN-07)

**Fluxo principal**

1. Quem mantém o sistema roda o comando no servidor, informando o e-mail.
   (RF-08)
2. A conta de operador fica cadastrada, e a pessoa entra pelo UC-01.

**Fluxos alternativos**

- **2a** O e-mail já tem conta: o comando informa e não muda nada. (RN-04)

**Pós-condição:** existe ao menos um operador. (RN-06)

## Casos de uso aposentados

Nenhum até agora. O identificador é estável: caso de uso removido tem o número
aposentado e listado aqui, com o motivo, e o número não volta.
