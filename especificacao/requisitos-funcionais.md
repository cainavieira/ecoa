# Requisitos funcionais

Cada requisito diz o que o sistema faz, citando o ator pelo identificador de
`atores.md` e a regra pelo identificador de `regras-de-negocio.md`. Requisito
não repete o texto da regra: ele aponta.

O identificador é estável pelas mesmas razões do documento de regras. Os
aposentados ficam listados no fim.

## Acesso e conta

- **RF-01** O sistema deve autenticar toda pessoa por meio do AT-04, e recusar
  o e-mail que o AT-04 não devolver como verificado. (RN-01)
- **RF-02** O sistema deve criar conta de criador, pendente, no primeiro acesso
  de um e-mail sem conta, guardando a data desse acesso como data do aceite dos
  termos. (RN-03, RN-09, RN-10)
- **RF-03** O sistema deve dar à conta cadastrada por um operador o papel
  cadastrado, e completar nela o nome vindo do AT-04 no primeiro acesso da
  pessoa. (RN-03)
- **RF-04** O sistema deve permitir que qualquer ator encerre a própria sessão.
- **RF-05** O sistema deve restringir cada tela e cada rota ao papel: o AT-01 vê
  os próprios dados, as campanhas e o ranking das campanhas em que participa; o
  AT-03 só lê; o AT-02 opera. (RN-02, RN-08, RN-47)
- **RF-06** O sistema deve permitir que o AT-02 cadastre um e-mail como
  operador ou como gravadora. (RN-03, RN-04)
- **RF-07** O sistema deve permitir que o AT-02 remova o acesso de um operador
  ou de alguém da gravadora, menos o próprio. (RN-05, RN-06)
- **RF-08** O sistema deve oferecer um comando de linha que cadastra o primeiro
  operador. (RN-07)

## Conta de criador

- **RF-09** O sistema deve permitir que o AT-01 informe e corrija o @ enquanto a
  conta está pendente ou recusada. (RN-11, RN-14, RN-15)
- **RF-10** O sistema deve apresentar ao AT-01 o código de verificação e como
  usá-lo. (RN-12)
- **RF-11** O sistema deve listar ao AT-02 as contas pendentes que já
  informaram o @, com o @ e o código. (RN-12, RN-13)
- **RF-12** O sistema deve permitir que o AT-02 aprove ou recuse conta
  pendente, com motivo na recusa. (RN-11, RN-13)
- **RF-13** O sistema deve apresentar ao AT-01 a situação da conta, com o motivo
  da recusa quando houver. (RN-10, RN-13, RN-56)
- **RF-14** O sistema deve permitir que o AT-02 corrija o @ de conta aprovada ou
  bloqueada. (RN-11, RN-15)
- **RF-15** O sistema deve listar ao AT-02 todas as contas de criador, com
  situação e @.

## Bloqueio

- **RF-16** O sistema deve permitir que o AT-02 bloqueie conta aprovada, com
  motivo. (RN-17, RN-19)
- **RF-17** O sistema deve apresentar ao AT-01 bloqueado só a tela de bloqueio,
  com o motivo e o contato da operação. (RN-18)
- **RF-18** O sistema deve permitir que o AT-02 desbloqueie conta bloqueada,
  com motivo. (RN-21)

## Campanha

- **RF-19** O sistema deve permitir que o AT-02 crie campanha. (RN-22, RN-23,
  RN-24, RN-26)
- **RF-20** O sistema deve permitir que o AT-02 edite campanha não encerrada.
  (RN-24, RN-26, RN-27, RN-28)
- **RF-21** O sistema deve permitir que o AT-02 apague campanha sem
  participante. (RN-30)
- **RF-22** O sistema deve listar as campanhas com a situação de cada uma ao
  AT-01 não bloqueado, ao AT-02 e ao AT-03, indicando ao AT-01 as campanhas em
  que participa. (RN-25, RN-29)
- **RF-23** O sistema deve apresentar o regulamento, os links de referência e
  as datas de cada campanha. (RN-22, RN-63)
- **RF-24** O sistema deve permitir que o AT-02 encerre a campanha, gravando o
  ranking final. (RN-48, RN-50)

## Participação e vídeo

- **RF-25** O sistema deve permitir que o AT-01 aprovado entre na campanha,
  registrando o aceite. (RN-16, RN-31, RN-32)
- **RF-26** O sistema deve permitir que o AT-01 participante envie o link de um
  vídeo. (RN-34, RN-35)
- **RF-27** O sistema deve listar ao AT-02 os vídeos em análise de todas as
  campanhas, com o link e o @ do criador.
- **RF-28** O sistema deve permitir que o AT-02 aprove vídeo em análise,
  informando a data de publicação e a primeira leitura. (RN-19, RN-36, RN-39,
  RN-40)
- **RF-29** O sistema deve permitir que o AT-02 recuse vídeo em análise, com
  motivo. (RN-37)
- **RF-30** O sistema deve permitir que o AT-02 desqualifique vídeo aprovado,
  com motivo. (RN-38)
- **RF-31** O sistema deve apresentar ao AT-01 os próprios vídeos de cada
  campanha, com a situação, o motivo de recusa ou desqualificação, o número
  atual e o gráfico das leituras. (RN-43, RN-49, RN-56)

## Leitura

- **RF-32** O sistema deve permitir que o AT-02 registre as leituras de uma
  campanha numa tela só, com uma linha por vídeo que conta. (RN-39, RN-40,
  RN-41, RN-45)
- **RF-33** O sistema deve permitir que o AT-02 veja, corrija e apague as
  leituras de cada vídeo até o encerramento. (RN-42)

## Ranking e resultado

- **RF-34** O sistema deve apresentar o ranking da campanha a quem pode vê-lo.
  (RN-45, RN-46, RN-47, RN-48)
- **RF-35** O sistema deve permitir que o AT-02 registre ganhador, com o prêmio
  em texto. (RN-51, RN-52)
- **RF-36** O sistema deve permitir que o AT-02 informe a data de entrega do
  prêmio. (RN-52)
- **RF-37** O sistema deve permitir que o AT-02 apague registro de ganhador.
  (RN-53)
- **RF-38** O sistema deve apresentar os ganhadores a quem vê o ranking.
  (RN-54)

## Painel e exportação

- **RF-39** O sistema deve apresentar ao AT-03 e ao AT-02 o painel de cada
  campanha. (RN-49, RN-61)
- **RF-40** O sistema deve permitir que o AT-03 e o AT-02 exportem o resultado
  da campanha em CSV. (RN-62)

## Dados pessoais

- **RF-41** O sistema deve permitir que o AT-02 apague conta de criador.
  (RN-58, RN-59, RN-60)

## Formato

- **RF-42** O sistema deve apresentar números e datas no formato do idioma
  único do sistema. (RN-64)

## Requisitos aposentados

Nenhum até agora.
