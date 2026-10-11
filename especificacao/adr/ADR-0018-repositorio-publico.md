# ADR-0018 Repositório público no GitHub

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O projeto precisa de repositório remoto, para a integração contínua (ADR-0017)
e para a cópia do código fora da máquina de quem desenvolve. O Ecoa é um
negócio entre sócios, feito para uma gravadora, e a primeira versão dos
documentos tinha anotações sobre a negociação entre eles.

## Decisão

Repositório público no GitHub, no plano gratuito, desde o início, com três
condições:

1. **Sem arquivo de licença.** Fica "todos os direitos reservados".
2. **Os commits usam o endereço `noreply` do GitHub**, não o e-mail pessoal.
3. **O que vai ficar público é lido antes do primeiro push.** As anotações
   sobre a negociação entre os sócios ficam num arquivo fora do versionamento,
   e já ficaram fora do primeiro commit, porque o histórico do git também fica
   público.

## Consequências

A especificação e o código ficam visíveis a qualquer pessoa.

Segredos (chaves, senha do banco, o par de chaves do JWT) nunca entram no
repositório: chegam por variável de ambiente, com o `.env.example` versionado
sem os valores.

## Alternativa recusada: repositório privado

Foi a recomendação no grill, porque o projeto é um negócio com sócio e com uma
gravadora. Recusado pelo interessado, que preferiu público desde o início.
