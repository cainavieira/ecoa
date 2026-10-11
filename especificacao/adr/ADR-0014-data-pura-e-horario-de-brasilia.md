# ADR-0014 Data pura, com hoje no horário de Brasília

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O "hoje" decide quase tudo que é prazo: se a campanha aceita entrada e vídeo
(RN-32, RN-34), se o fechamento novo é futuro (RN-26), se a leitura tem data
futura (RN-40), se já dá para encerrar (RN-50).

O servidor roda em UTC. Às 21h de Brasília, o dia do servidor já virou, e uma
campanha que termina hoje fecharia três horas antes.

As datas do Ecoa são da campanha, que é brasileira e tem um regulamento só. No
Tracking peso, o dia era de cada pessoa, que pode viajar, e por isso lá o dia
vinha do aparelho.

## Decisão

Datas de campanha, de publicação, de leitura e de entrega são **data pura**, sem
hora e sem fuso. O "hoje" é calculado no servidor, no horário de Brasília
(`America/Sao_Paulo`), e o regulamento informa isso (RN-63).

## Consequências

Todo prazo vira comparação de datas, com um único ponto do código dizendo qual
dia é hoje.

Nenhuma rota precisa de cabeçalho com a data do aparelho.

No Acre, uma hora atrás de Brasília, o prazo fecha às 22h59 locais. Aceito, e
dito no regulamento.

O instante de eventos que não são prazo (data do aceite, criação de registro)
pode ser guardado com fuso, porque não decide regra.

## Alternativa recusada: a data do aparelho, como no Tracking peso

Cada pessoa mandaria o próprio dia num cabeçalho. Recusada porque o prazo é da
campanha, e não de cada participante: dois criadores em fusos diferentes
veriam a mesma campanha fechar em momentos diferentes, e a regra de cabeçalho
apareceria em toda rota.

## Alternativa recusada: o dia do servidor em UTC

Nenhuma configuração. Recusado pelo defeito do contexto: o dia vira às 21h de
Brasília.
