# ADR-0012 Regras de situação e de ranking sem framework

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O Ecoa não tem fórmula de prêmio (RN-51). O que ele tem de regra está em
situação e em data: a situação da campanha derivada das datas (RN-25), as
transições da conta e do vídeo (RN-10, RN-33), a leitura de fechamento
(RN-41), o ranking com empate (RN-45, RN-46), o total por data (RN-49) e as
pendências do encerramento (RN-50).

A RNF-28 exige teste dessas regras, porque o erro nelas passa despercebido: um
bloqueado que continua no ranking parece um ranking normal.

## Decisão

Uma regra só, em vez de camadas: o pacote que contém essas regras não importa
Spring nem JPA. Ele recebe dados e devolve valores.

Da mesma família, duas regras concretas: o controller não decide nada, ele
chama quem decide; e a função de regra recebe dados, não o repositório.

Um teste de arquitetura, com o ArchUnit, falha se alguma classe do pacote de
regras importar `org.springframework` ou `jakarta.persistence`.

## Consequências

O teste das regras roda sem banco e sem servidor, em milissegundos, e é o que
torna a RNF-28 barata (ADR-0013).

O pacote de regras é o único com o NullAway (ADR-0013), possível porque ele não
toca em tipo de biblioteca de terceiro.

## Alternativa recusada: camadas completas de Clean Architecture

Entidades, casos de uso, adaptadores e infraestrutura, com inversão de
dependência em cada fronteira, custam muita indireção para um sistema deste
tamanho. O núcleo da ideia que vale aqui é a regra acima, e ela se escreve numa
linha.
