# Atores

Define quem interage com o sistema. Todo requisito funcional e todo caso de
uso referencia um ator daqui pelo identificador.

## AT-01 Criador

Criador de conteúdo que posta vídeos no TikTok com a música de uma campanha e
quer ser premiado por isso.

- Entra sozinho, pela conta Google (AT-04). Não precisa de convite (RN-03).
- Informa o @ do TikTok e prova que é dele pondo um código na bio (RN-11,
  RN-12). Até o operador aprovar, não entra em campanha (RN-16).
- Participa de campanhas, envia vídeos por link e acompanha a situação de cada
  um, o ranking e os ganhadores das campanhas em que participa (RN-31, RN-47).
- Vê os próprios dados e, das campanhas em que participa, o @ e as views dos
  outros participantes. Nada além disso (RN-47).
- Bloqueado, continua entrando, mas vê só a tela de bloqueio (RN-18).

## AT-02 Operador

Quem opera a plataforma. No piloto são duas pessoas, Caina e Mateus, com as
mesmas permissões.

- Cadastra quem é operador e quem é da gravadora (RN-03). O primeiro operador
  entra por comando de linha (RN-07).
- Verifica as contas de criador, bloqueia e desbloqueia (RN-13, RN-17, RN-21).
- Cria as campanhas, a pedido da gravadora, com o regulamento que ela escreve
  (RN-22).
- Confere os vídeos, digita as leituras de views a partir da página pública do
  TikTok, encerra a campanha e registra os ganhadores (RN-36, RN-39, RN-50,
  RN-51).
- Apaga a conta de criador que pedir exclusão (RN-58).
- Um dos operadores também mantém o sistema no ar: deploy, backup e
  restauração, por fora do sistema, pelo terminal da VPS e da própria máquina.
  Isso não é caso de uso do sistema.

## AT-03 Gravadora

Pessoas da gravadora que financia as campanhas e paga os prêmios.

- Tem tela, só de leitura: o painel de cada campanha, com ranking, gráfico e
  ganhadores, e a exportação em CSV (RN-61, RN-62).
- Entra com o e-mail que o operador cadastrou (RN-03).
- Não opera nada no sistema. O que ela decide (regulamento, prêmio) chega ao
  sistema pelo operador.
- Paga o prêmio direto ao ganhador, por fora do sistema (RN-55).

## AT-04 Google

Sistema externo. Autentica todos os atores e devolve nome e e-mail.

- É a única fonte de identidade do sistema (RN-01).
- Os escopos pedidos se limitam a identidade, o que dispensa verificação do app
  (RNF-22).
- Indisponibilidade dele impede login novo. Sessão já aberta não cai.

## Quem não é ator, e por quê

- **Artista**: tem a música promovida, mas age pela gravadora. Não executa
  nenhum caso de uso.
- **TikTok**: o criador posta lá e o operador lê lá, mas o sistema não conversa
  com ele. No MVP não há integração (ADR-0001), e por isso ele não aparece como
  sistema externo.
- **Visitante sem conta**: não existe página pública. Quem não entrou vê só a
  tela de entrada (RNF-12).
- **Criador pendente, recusado ou bloqueado**: são situações do AT-01 (RN-10),
  não atores. Eles executam os mesmos casos de uso, com o que a situação
  permite.

## Atores aposentados

Nenhum até agora. O identificador é estável: ator removido tem o número
aposentado e listado aqui, com o motivo, e o número não volta.
