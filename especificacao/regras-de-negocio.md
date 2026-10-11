# Regras de negócio

Regras que valem independentemente de tela, rota ou tecnologia. Requisitos
funcionais e casos de uso citam estas regras pelo identificador em vez de
repetir o texto.

O identificador é estável: regra removida tem o número aposentado, nunca
reaproveitado por outra regra. As aposentadas ficam listadas no fim, com o
motivo.

## Conta e papel

- **RN-01** Toda pessoa entra pela conta Google (AT-04), identificada pelo
  e-mail que o Google devolve como verificado, guardado em minúsculas. O
  sistema não guarda senha.
- **RN-02** Cada conta tem um papel só: criador, operador ou gravadora. O papel
  decide o que a pessoa vê e faz, e não muda.
- **RN-03** Conta de operador ou de gravadora nasce de cadastro feito por um
  operador, pelo e-mail, antes do primeiro acesso da pessoa. Quem entra com um
  e-mail sem conta recebe conta de criador.
- **RN-04** Cadastrar como operador ou gravadora um e-mail que já tem conta é
  recusado. Para mudar o papel de alguém, a conta antiga é apagada antes: a de
  criador pela RN-59, a de operador ou gravadora pela RN-05.
- **RN-05** Remover o acesso de um operador ou de alguém da gravadora apaga a
  conta. Se a pessoa entrar de novo com o mesmo e-mail, recebe conta de
  criador, como qualquer outra (RN-03).
- **RN-06** O operador não remove a própria conta, e a remoção que deixaria o
  sistema sem nenhum operador é recusada. Assim sempre existe ao menos um
  operador.
- **RN-07** O primeiro operador é cadastrado por comando de linha no servidor,
  porque antes dele não existe quem cadastre.
- **RN-08** Existe uma única gravadora no sistema, e toda conta de gravadora vê
  todas as campanhas. Várias gravadoras exigiriam separar o que cada uma vê, e
  isso não existe.
- **RN-09** A tela de entrada avisa que entrar implica aceitar os termos de uso
  e a política de privacidade, com link para os dois. A conta de criador guarda
  a data do primeiro acesso como data desse aceite.

## Conta de criador

- **RN-10** A conta de criador tem quatro situações: pendente, recusada,
  aprovada e bloqueada. Nasce pendente.
- **RN-11** O criador informa o @ do TikTok. O @ é guardado sem o arroba, em
  minúsculas e sem espaços. Duas contas aprovadas ou bloqueadas não têm o mesmo
  @. Contas pendentes ou recusadas podem ter, porque quem prova a posse é o
  código na bio (RN-12), e só o dono do @ consegue pô-lo lá.
- **RN-12** Cada conta de criador tem um código de verificação gerado pelo
  sistema, único. O criador põe o código na bio do TikTok, e o operador o
  confere no perfil público. Depois da aprovação, o código pode sair da bio.
- **RN-13** O operador aprova ou recusa a conta pendente que já informou o @,
  olhando o perfil, sem critério numérico. A recusa exige motivo, que aparece
  na tela do criador. Aprovar uma conta cujo @ já pertence a outra conta
  aprovada ou bloqueada é recusado (RN-11).
- **RN-14** O criador pendente ou recusado pode corrigir o @. Corrigir o @ da
  conta recusada a devolve a pendente.
- **RN-15** O @ da conta aprovada ou bloqueada só muda pelo operador, porque a
  aprovação verificou aquele @. Antes de corrigir, o operador confere o código
  de verificação na bio do @ novo, como na aprovação (RN-12).
- **RN-16** Só a conta aprovada entra em campanha e envia vídeo.

## Bloqueio

- **RN-17** O operador bloqueia a conta aprovada, com motivo obrigatório. O
  bloqueio vale para todas as campanhas.
- **RN-18** O criador bloqueado continua entrando no sistema e vê só a tela de
  bloqueio, com o motivo e o contato da operação para contestar. A contestação
  acontece fora do sistema.
- **RN-19** Enquanto durar o bloqueio, o criador sai do ranking das campanhas
  não encerradas, não pode ser registrado como ganhador, e vídeo dele em
  análise não pode ser aprovado. Os vídeos e as leituras dele não mudam.
- **RN-20** Prêmio registrado antes do bloqueio continua registrado, e apagá-lo
  é decisão do operador (RN-53).
- **RN-21** O operador desbloqueia, com motivo, e a conta volta a aprovada. O
  criador volta ao ranking das campanhas não encerradas com os vídeos e as
  leituras que já tinha.

## Campanha

- **RN-22** A campanha promove uma música e tem nome da música, artista,
  regulamento em texto, ao menos um link de referência e três datas: início,
  fim e fechamento.
- **RN-23** Link de referência (o som, um vídeo de exemplo, as regras) é um
  endereço http ou https para qualquer lugar, do TikTok ou não. O sistema não
  recebe arquivo.
- **RN-24** As datas obedecem a início ≤ fim ≤ fechamento. Início é o primeiro
  dia em que vale publicar; fim, o último; fechamento, o dia a partir do qual a
  leitura vale para o resultado (RN-41). A distância entre fim e fechamento é a
  estabilização, e pode ser zero.
- **RN-25** A situação da campanha é derivada: agendada antes do início; aberta
  do início ao fim; em apuração depois do fim, até ser encerrada; encerrada
  depois do encerramento (RN-50).
- **RN-26** Ao criar a campanha, ou ao mudar a data de fechamento, o fechamento
  novo é posterior a hoje. Como leitura não tem data futura (RN-40), nenhuma
  leitura já registrada fica depois do fechamento novo.
- **RN-27** Mudar o início ou o fim mantém todo vídeo aprovado da campanha com a
  data de publicação entre os dois. A mudança que deixaria um vídeo aprovado de
  fora é recusada.
- **RN-28** Campanha encerrada não muda: dados, datas, vídeos e leituras ficam
  como estão. Só os registros de ganhador continuam editáveis (RN-52, RN-53).
- **RN-29** Pode haver mais de uma campanha não encerrada ao mesmo tempo. Uma
  campanha por vez é premissa de operação do piloto (RNF-01), não regra.
- **RN-30** Campanha sem participante pode ser apagada. Com participante, não.

## Participação

- **RN-31** Para enviar vídeo, o criador entra na campanha, aceitando o
  regulamento e que o @ e as views dele apareçam para os outros participantes e
  para a gravadora (RN-47). A participação guarda a data do aceite.
- **RN-32** Entrar na campanha é possível enquanto ela está agendada ou aberta.

## Vídeo

- **RN-33** O vídeo tem quatro situações: em análise, aprovado, recusado e
  desqualificado. Nasce em análise.
- **RN-34** O participante envia o vídeo por link enquanto a campanha está
  aberta. O link é um endereço https cujo domínio é `tiktok.com` ou termina em
  `.tiktok.com`, com o ponto. Não há limite de vídeos por criador.
- **RN-35** O mesmo link não entra de novo, em campanha nenhuma, enquanto o
  envio anterior estiver em análise, aprovado ou desqualificado. Depois de
  recusado, pode ser enviado de novo. O mesmo vídeo por outro link (o link
  curto do aplicativo e o link completo) é pego pelo operador na conferência.
- **RN-36** O operador aprova o vídeo em análise depois de conferir que ele é
  público, é da conta do @ verificado, usa o som da campanha e cumpre o
  regulamento. Na aprovação, informa a data de publicação, que fica entre o
  início e o fim da campanha, e registra a primeira leitura (RN-39).
- **RN-37** O operador recusa o vídeo em análise, com motivo, que aparece na
  tela do criador. Enquanto a campanha estiver aberta, o criador pode enviar
  outro. Vídeo aprovado não se troca nem volta à análise.
- **RN-38** O operador desqualifica o vídeo aprovado que saiu do ar, ficou
  privado, teve a conta banida pelo TikTok ou se mostrou fora do regulamento,
  com motivo, que aparece na tela do criador. A desqualificação tira o vídeo do
  ranking, é definitiva e não bloqueia o criador.

## Leitura

- **RN-39** A leitura é o número de views de um vídeo aprovado numa data,
  digitado pelo operador a partir da página pública do vídeo. Views é inteiro,
  zero ou maior, e pode ser menor que o da leitura anterior, porque as redes
  removem views falsas.
- **RN-40** A data da leitura não é futura nem anterior à data de publicação do
  vídeo. Há no máximo uma leitura por vídeo por data, e registrar outra na
  mesma data substitui a anterior.
- **RN-41** A leitura de fechamento é a leitura do vídeo com data igual ou
  posterior ao fechamento da campanha, e cada vídeo tem no máximo uma. Gravar
  outra leitura a partir do fechamento, em data diferente da que existe, é
  recusado; para trocar a data, o operador apaga a leitura de fechamento antes.
  As leituras anteriores ao fechamento são parciais.
- **RN-42** Até o encerramento, o operador corrige ou apaga qualquer leitura.
- **RN-43** O número atual do vídeo é a leitura mais recente dele, mostrada com
  a data ("lido em"). Vídeo aprovado cujas leituras foram todas apagadas conta
  zero e mostra que não tem leitura.
- **RN-44** A frequência das leituras parciais é escolha do operador. O sistema
  não exige nem agenda leitura.

## Ranking

- **RN-45** Numa campanha não encerrada, conta o vídeo aprovado de criador não
  bloqueado. O ranking lista os participantes com ao menos um vídeo que conta,
  em ordem decrescente da soma do número atual desses vídeos. Embaixo de cada
  criador ficam os vídeos que contam, com o número atual e a data da leitura.
- **RN-46** Empate divide a posição: criadores com a mesma soma ficam na mesma
  posição, e a posição seguinte pula tantas quantas empataram (1, 1, 3).
- **RN-47** Veem o ranking os participantes da campanha, a gravadora e os
  operadores. Quem não participa não vê, inclusive depois do encerramento.
- **RN-48** No encerramento, o ranking é calculado uma última vez, com as
  leituras de fechamento, e fica gravado como ranking final. Na campanha
  encerrada, contam os vídeos do ranking final. Bloqueio, desbloqueio e
  exclusão de conta posteriores não o mudam.
- **RN-49** O total da campanha numa data soma, de cada vídeo que conta, a
  leitura mais recente até aquela data. O gráfico da campanha tem um ponto em
  cada data com leitura; o gráfico de um vídeo, um ponto por leitura.

## Encerramento

- **RN-50** O operador encerra a campanha a partir da data de fechamento,
  quando nenhum vídeo dela está em análise e todo vídeo que conta tem leitura
  de fechamento. Encerrar é definitivo.

## Prêmio

- **RN-51** O sistema não calcula prêmio. O regulamento diz o que se ganha, e o
  operador registra cada ganhador: o participante, o prêmio em texto e, depois,
  a data de entrega.
- **RN-52** O ganhador é participante da campanha e não está bloqueado. O
  registro é possível a partir do início da campanha, inclusive depois do
  encerramento, e um criador pode ganhar mais de um prêmio na mesma campanha. A
  data de entrega não é futura.
- **RN-53** O operador apaga o registro de ganhador a qualquer momento.
- **RN-54** Os ganhadores aparecem para quem vê o ranking (RN-47).
- **RN-55** A gravadora paga o prêmio direto ao ganhador, por fora do sistema, e
  pede a ele o que precisar para isso (Pix, endereço, CPF). O sistema não guarda
  valor devido nem dado de pagamento.

## Avisos

- **RN-56** O sistema não envia e-mail nem notificação. A situação da conta, a
  de cada vídeo, com o motivo da recusa ou da desqualificação, e os prêmios
  aparecem na tela quando o criador entra.

## Dados pessoais

- **RN-57** Do criador, o sistema guarda o nome e o e-mail vindos do Google, o
  @, o código de verificação e as datas de aceite (RN-09, RN-31). Não guarda
  CPF, chave Pix, endereço nem foto.
- **RN-58** O criador pede a exclusão da conta ao operador, por fora do
  sistema, e o operador a apaga.
- **RN-59** Apagar a conta de criador remove nome, e-mail, @ e código. Nas
  campanhas não encerradas, remove também a participação, os vídeos, as
  leituras e os registros de ganhador dele. Nas encerradas, vídeos, leituras,
  posição final e prêmios permanecem como "participante removido", para o
  ranking final e o relatório já entregue não mudarem.
- **RN-60** Depois da exclusão, a mesma pessoa pode entrar de novo e recebe
  conta de criador nova, pendente, sem nada da anterior.

## Painel e exportação

- **RN-61** Para cada campanha, o painel da gravadora mostra o total de views na
  data de hoje (RN-49), o número de criadores no ranking, o número de vídeos que
  contam, o ranking, o gráfico do total e os ganhadores.
- **RN-62** A exportação gera um CSV com uma linha por vídeo que conta: posição
  do criador, @, link, data de publicação, número atual e data da leitura. Na
  campanha encerrada, os números são os do ranking final, e o participante
  removido aparece assim (RN-59). Gravadora e operador exportam.

## Data e formato

- **RN-63** Datas são datas puras, sem hora. Hoje é o dia corrente no horário de
  Brasília, e o regulamento informa isso.
- **RN-64** O sistema opera só em português do Brasil. Data é exibida em
  DD/MM/AAAA e trafegada em AAAA-MM-DD; views são exibidas com ponto separando
  o milhar.

## Regras aposentadas

Nenhuma até agora. Quando houver, os números ficam aqui, com o que diziam e por
que saíram, para que uma citação antiga possa ser entendida e ninguém
reaproveite o número.

## Decisões recusadas

Registradas para que não voltem como novidade.

| Recusado | Motivo |
|:--|:--|
| Coleta de views pela API do TikTok no MVP | ADR-0001 |
| Login pelo TikTok | ADR-0007 |
| Upload de arquivo da campanha | O material de referência é link (RN-23). Guardar arquivo traria armazenamento e uma segunda parte no backup, sem ganho no piloto |
| Cálculo automático de prêmio (meta de views, taxa por mil views, rateio) | A forma de premiar da gravadora ainda não existe, e o prêmio pode ser produto. Quando existir, entra como regra nova |
| Guardar CPF, chave Pix ou endereço | Dado sensível que a gravadora pede só ao ganhador. Custo aceito: o sistema não impede a mesma pessoa de participar com duas contas do TikTok, e isso só aparece se as duas ganharem |
| Curtidas, comentários e fator de plataforma | O prêmio é por view. Entram na Fase 2, quando a API os traz sem digitação |
| Janela de medição por vídeo | Cada vídeo fecharia num dia diferente. As três datas da campanha valem para todos. Custo aceito: o vídeo publicado no último dia tem menos tempo para crescer |
| Leitura com frequência obrigatória ou agendada | A leitura é manual, e a frequência das parciais é prática do operador (RN-44) |
| Limite de vídeos por criador | Indesejável para a gravadora, que ganha alcance com mais vídeos. O trabalho de leitura se ajusta pela frequência |
| Ranking em página pública | Expõe dado de pessoa a quem não participa |
| E-mail ou notificação | Exige serviço de e-mail, domínio verificado e DNS. Com 50 criadores, o operador avisa por fora o que importa. Custo aceito: quem teve vídeo recusado só descobre ao voltar ao site |
| Exclusão de conta pelo próprio criador | Rara com 50 criadores, e o operador já apaga pela tela dele |
| Apagar tudo do criador também nas campanhas encerradas | Mudaria o ranking final e o relatório já entregue à gravadora |
| Painel da gravadora em PDF | A gravadora vai querer mexer nos números numa planilha, e PDF exige layout |
