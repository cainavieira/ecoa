# ADR-0016 Backup baixado para a máquina de um operador

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

A leitura é o dado que não se recupera (RNF-05), e a perda máxima aceitável é
de 24 horas (RNF-06). Uma cópia que mora só na VPS some junto com ela: falha de
disco, problema na conta do provedor, servidor apagado. A produção não tem
plataforma gerenciada com backup incluído (ADR-0015).

## Decisão

Na VPS, um agendamento diário faz o dump do banco e guarda os dos últimos 7
dias. O mesmo agendamento apaga os tokens de renovação vencidos (RNF-23).

Na máquina de um operador, um agendamento diário:

1. baixa o dump mais recente da VPS, por SSH;
2. guarda os dumps dos últimos 30 dias;
3. restaura o mais recente num Postgres em Docker local;
4. avisa um serviço externo de homem morto (RNF-08).

## Consequências

Há duas cópias em dois lugares: a VPS e a máquina do operador. A restauração
diária testa o backup todo dia, de graça.

Os 7 dias na VPS cobrem dias seguidos com a máquina do operador desligada: o
agendamento seguinte baixa o que estiver lá. O silêncio do aviso de homem morto
é o que avisa que isso está acontecendo.

Os dados pessoais dos criadores passam a morar também na máquina do operador
(RNF-13), e as contas apagadas continuam nas cópias até o fim dos 30 dias
(RNF-14).

O ensaio do deploy usa o dump que já está na VPS (ADR-0006).

## Alternativa recusada: cópia num armazenamento na nuvem

É o desenho do Tracking peso, com o Cloudflare R2. Recusada por ora, por
escolha do interessado de manter o backup local, sem conta em mais um serviço.

## Alternativa recusada: segundo Postgres na própria VPS

Protege contra dado apagado por engano ou migração ruim, mas some junto com a
VPS, que é a falha que o backup mais precisa cobrir.

## Quando rever

Quando a máquina do operador ficar desligada com frequência, ou quando o
sistema tiver mais de uma gravadora e o dado pesar mais: aí a cópia na nuvem
volta à mesa.
