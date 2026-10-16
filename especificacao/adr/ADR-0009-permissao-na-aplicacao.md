# ADR-0009 Papel e isolamento conferidos na aplicação

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

Há três papéis (RN-02). O criador vê os próprios dados e, das campanhas em que
participa, o ranking (RN-47); a gravadora só lê (RN-08); o operador opera. O
e-mail e o código de verificação do criador não saem para outro criador nem
para a gravadora (RN-57). O operador vê o código na fila de verificação e na
lista de contas, porque é com ele que confere a bio (RF-11, RF-15, RN-12).

As tabelas guardam os dados de todos juntos, e uma consulta que esqueça o
filtro alcança dado alheio.

## Decisão

Na aplicação, e só nela.

- Cada rota declara o papel que a aceita, e o Spring Security recusa os outros.
- Toda consulta de dado de criador recebe o identificador da conta autenticada.
- O ranking e os ganhadores conferem a participação antes de responder.
- Cada rota tem teste de papel errado e, quando lida com dado de criador, teste
  de que um criador não alcança o dado de outro.

## Consequências

Papel errado responde 403. Recurso de outro criador e recurso inexistente dão o
mesmo resultado, 404, porque saber que existe exigiria a consulta sem filtro
que esta decisão proíbe (`endpoints.md`).

O controle de acesso fica numa camada só, verificado por teste de rota contra
Postgres real (ADR-0013).

## Alternativa recusada: segurança em nível de linha do Postgres

O banco recusaria sozinho a consulta que esquecesse o filtro. Recusada pelo
custo, para proteger o que o teste por rota já pega: identidade informada ao
banco em toda transação, uma conta de banco a mais, as tabelas do login fora da
política, e mais uma camada onde depurar quando um teste falha.
