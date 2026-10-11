# ADR-0008 Autenticação com Spring Security e JWT em cookie

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

A identidade vem do Google (ADR-0007). O fluxo do OpenID Connect tem quatro
pontos que erram em silêncio: o `state`, o `nonce`, a troca do código pelo
token e a verificação da assinatura do `id_token`. Errar qualquer um ainda
produz um login bem-sucedido.

Depois do login, o sistema reconhece a pessoa e o papel a cada requisição. A
RNF-23 pede sessão de 30 dias renovada a cada uso, e a RF-04, encerramento
explícito. Frontend e api ficam na mesma origem (ADR-0015).

## Decisão

O login com o Google é feito pelo Spring Security, que cuida dos quatro pontos
do contexto. No fim do login, o sistema acha a conta pelo e-mail, ou cria a de
criador (RN-03).

O sistema emite dois tokens, cada um num cookie `httpOnly`, `Secure` e
`SameSite=Lax`:

- um **token de acesso**, JWT assinado com RS256, válido por 15 minutos, com o
  identificador da conta e o papel, conferido só pela assinatura;
- um **token de renovação**, aleatório, válido por 30 dias, guardado no banco
  como hash e enviado só para as rotas de autenticação. Cada uso empurra a
  validade para mais 30 dias, e ele não é trocado a cada uso.

Quando o token de acesso vence, o frontend usa o de renovação para pedir outro.
Sair apaga o token de renovação e expira os dois cookies. Toda falha do retorno
do login volta para a tela de entrada com um motivo, nunca com erro cru. Não há
token de proteção contra CSRF.

## Consequências

O papel vai no token porque não muda (RN-02). A situação da conta de criador
não vai: ela muda a qualquer momento (bloqueio, aprovação) e é lida do banco
nas rotas que dependem dela.

Conta removida (RN-05) perde a renovação na hora, e o token de acesso que já
existia vale no máximo mais 15 minutos. Aceito (UC-18 4a).

`SameSite=Lax`, e não `Strict`, porque o retorno do Google é navegação vinda de
outro site, e `Strict` faria o cookie não ser enviado nesse retorno. Por causa
do `Lax`, nenhum GET muda dado.

O banco guarda só o hash do token de renovação. O teste de ponta a ponta insere
um token de renovação no banco e põe o cookie no navegador, sem código de
produção (ADR-0013).

Durante os segundos do login, o Spring guarda o `state` numa sessão em memória.
Com uma réplica da api (ADR-0015), isso basta.

## Alternativa recusada: sessão guardada no banco

Revoga na hora e basta no volume da RNF-25. Recusada pela consistência com o
Tracking peso, que adotou JWT pelo aprendizado: o mesmo desenho nos dois
projetos.

## Alternativa recusada: JWT de 30 dias com lista de tokens cancelados

Junta o custo das duas opções: o token longo vale em qualquer cópia, e revogar
volta a consultar o banco em toda requisição.

## Alternativa recusada: trocar o token de renovação a cada uso

Com duas abas abertas, as duas renovam ao mesmo tempo, a primeira invalida o
token da segunda, e a segunda cai na tela de login. Em cookie `httpOnly`, o
JavaScript não lê o token, que é o caso em que a troca protege.

## Alternativa recusada: token guardado pelo JavaScript

No `localStorage`, qualquer script injetado na página leria o token.

## Alternativa recusada: o fluxo do Google escrito à mão

Os quatro pontos do contexto erram em silêncio, e o Spring Security já os
resolve.

## Quando rever

Mais de uma réplica da api: o `state` do login sai da memória.
