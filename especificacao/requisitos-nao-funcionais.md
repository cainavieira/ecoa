# Requisitos não funcionais

O que o sistema tem que ser, em contraste com o que ele faz. Todo item traz um
número quando existe número, e diz explicitamente quando não existe meta e por
quê. RNF sem número é adjetivo.

## Escala

- **RNF-01** Premissa do piloto: uma gravadora (RN-08), uma campanha por vez e
  até 50 criadores. O teto vem da leitura manual (ADR-0001): com 50 criadores e
  três vídeos cada, o fechamento pede 150 leituras, por volta de 75 minutos de
  trabalho. Acima disso, a leitura manual deixa de caber, e a API do TikTok
  volta à mesa. O sistema não impõe o teto: é premissa de operação (RN-29).

## Custo

- **RNF-02** O custo de operação é a mensalidade da VPS e o domínio, e os
  valores ainda não foram conferidos. O backup não tem custo à parte, porque
  vai para a máquina de um operador (ADR-0016).

## Disponibilidade

- **RNF-03** Não há meta de disponibilidade. Fora do ar, o cadastro e o envio
  esperam, e a leitura manual se faz depois. O pior caso é o criador não
  conseguir se cadastrar durante a divulgação da campanha.
- **RNF-04** Um monitor externo chama o sistema e avisa quando ele não
  responde. Monitor dentro da VPS cairia junto e ficaria quieto.

## Preservação do dado

- **RNF-05** A leitura é o dado que não se recupera: o TikTok mostra só o número
  atual, e a view de uma data passada não se lê de novo. É ela que os requisitos
  desta seção protegem.
- **RNF-06** A perda máxima aceitável é de 24 horas de dado.
- **RNF-07** Um dump diário do banco fica na VPS. Um agendamento na máquina de
  um operador baixa o dump, guarda os últimos 30 dias e restaura o mais recente
  num Postgres em Docker local, o que testa a restauração todo dia (ADR-0016).
- **RNF-08** O dump que não chegou à máquina do operador gera alerta, por aviso
  de homem morto num serviço externo: o agendamento avisa o serviço depois de
  restaurar, e o silêncio no horário esperado dispara o alerta. Máquina
  desligada é a causa mais provável, e é justamente o que o alerta precisa
  pegar. O agendamento só avisa quando o dump baixado é do próprio dia: se o
  dump da VPS parou de ser feito, o mais recente é velho, a restauração dele
  passa, e sem essa conferência o alerta ficaria quieto. Pelo mesmo motivo, o
  ensaio do deploy recusa dump com mais de um dia (RNF-09).
- **RNF-09** Antes de cada deploy, o dump mais recente é restaurado num banco
  descartável e recebe as migrações novas. Se uma das duas etapas falhar, o
  deploy para (ADR-0006, ADR-0017).
- **RNF-10** Depois da leitura de fechamento, o operador exporta o CSV da
  campanha (RN-62): uma segunda cópia do resultado, fora do sistema, no dia em
  que ele é decidido. É prática de operação, sem verificação automática.

## Privacidade

- **RNF-11** O dado pessoal guardado é o mínimo da RN-57. CPF, chave Pix e
  endereço ficam com a gravadora, que os pede só ao ganhador (RN-55).
- **RNF-12** O @ e as views de um criador aparecem só para os participantes da
  mesma campanha, para a gravadora e para os operadores (RN-47). Não existe
  página pública.
- **RNF-13** As cópias de backup na máquina do operador contêm os dados
  pessoais dos criadores, e proteger aquele disco é dever de quem mantém a
  máquina.
- **RNF-14** Conta apagada continua nas cópias de backup até o fim da retenção
  de 30 dias. A restauração vai sempre ao dump mais recente, para as exclusões
  feitas antes do problema continuarem valendo.
- **RNF-15** O sistema trata dado pessoal com fim econômico e está inteiro sob a
  LGPD. Os termos de uso e a política de privacidade (RN-09) existem antes do
  piloto, e o texto deles não é escrito aqui (`instrucao.md`, seção 6).

## Registro operacional

- **RNF-16** O log da aplicação registra só erro, e nunca e-mail nem @,
  inclusive pelo caminho indireto: mensagem de exceção com o corpo da
  requisição, erro de restrição do Postgres que repete a linha recusada, objeto
  inteiro registrado. Operação bem-sucedida não gera linha.
- **RNF-17** O log de acesso do Caddy é mantido, porque é o que revela varredura
  e tentativa de entrada num serviço exposto na internet.
- **RNF-18** Retenção de 7 dias para os dois. O uso do log é o operador
  relatando um problema no dia seguinte ou depois do fim de semana.

## Segurança

- **RNF-19** A VPS aceita SSH só com chave, abre só as portas 22, 80 e 443, e
  instala atualizações de segurança sozinha (ADR-0015).
- **RNF-20** O banco não é alcançável de fora da rede interna do Compose.
- **RNF-21** Todo acesso é por HTTPS, com certificado obtido e renovado pelo
  Caddy.
- **RNF-22** Os escopos pedidos ao AT-04 se limitam a identidade: `openid`,
  e-mail e perfil. Para esses escopos, o Google não exige verificação do app
  nem lista de usuários de teste (ADR-0007).
- **RNF-23** A sessão dura 30 dias e renova a cada uso, com encerramento
  explícito disponível (ADR-0008). Token de renovação vencido é removido pelo
  mesmo agendamento diário que faz o dump, em vez de acumular linha morta.

## Idioma

- **RNF-24** Português do Brasil apenas, sem estrutura de tradução (RN-64).

## Desempenho

- **RNF-25** Não há meta de latência, e a ausência é deliberada: 50 criadores e
  duas pessoas operando não fazem volume que o banco sinta. Fixar número aqui
  seria inventar restrição.
- **RNF-26** A meta que existe é de trabalho do operador: as leituras de uma
  campanha se registram numa tela só, uma linha por vídeo, sem abrir vídeo por
  vídeo no sistema (RF-32). O fechamento, com até 150 leituras, é o momento de
  mais trabalho da operação.

## Ambientes

- **RNF-27** O desenvolvimento acontece todo na máquina de quem desenvolve, com
  o Postgres em Docker. A VPS recebe só o MVP pronto, e não há ambiente de
  homologação (ADR-0015).

## Manutenibilidade

- **RNF-28** As transições de situação (conta, vídeo, campanha), a leitura de
  fechamento, o ranking e as pendências do encerramento são cobertos por teste
  automatizado. São a parte onde o erro passa despercebido: um bloqueado que
  continua no ranking, um vídeo fora do prazo aprovado, um encerramento com
  leitura faltando (ADR-0012, ADR-0013).

## Requisitos aposentados

Nenhum até agora. O identificador é estável: requisito removido tem o número
aposentado e listado aqui, com o motivo, e o número não volta.
