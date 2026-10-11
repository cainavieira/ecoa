# ADR-0005 Spring Data JPA, com entidade separada do DTO

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O backend é Spring Boot (ADR-0002) sobre Postgres (ADR-0004). Falta decidir
como o código conversa com o banco, e se tabela e contrato de API compartilham
a mesma declaração.

Eles divergem, e a divergência aqui tem consequência de privacidade. A conta de
criador guarda e-mail e código de verificação (RN-57), e o ranking mostra o @
dela a outros participantes e à gravadora (RN-47). Se a entidade fosse o
contrato, o e-mail e o código iriam junto no dia em que alguém esquecesse uma
anotação. Na direção contrária, o número atual, a posição e o total saem na API
e não existem em coluna (RN-43, RN-45, RN-49).

## Decisão

Spring Data JPA, com o Hibernate por baixo, descreve as tabelas como
entidades. Records Java separados descrevem a entrada e a saída de cada rota, e
uma camada de mapeamento escrita à mão converte entre os dois.

Duas regras fazem parte da decisão. O esquema pertence às migrações: o
Hibernate confere na subida se as entidades batem com as tabelas, e nunca cria
nem altera tabela. E o banco só é consultado dentro do service, nunca enquanto
a resposta é montada.

## Consequências

Cada DTO afirma só o que aquele leitor pode ver. O do ranking não tem campo de
e-mail, então não há o que esquecer.

O JPA traz duas armadilhas que produzem consulta que ninguém escreveu: o
carregamento preguiçoso e o N+1. Como o banco só é consultado dentro do
service, o carregamento fora de lugar aparece como erro no desenvolvimento.

A conferência da subida pega entidade que divergiu da tabela, mas não confere
restrição: as chaves únicas do ADR-0004 existem só no SQL das migrações.

## Alternativa recusada: a entidade como contrato de API

Dispensa o mapeamento. Recusada pelo motivo do contexto: o dado que não pode
sair passaria a depender de anotação para não sair.

## Alternativa recusada: Spring Data JDBC ou JdbcClient

SQL mais explícito, sem carregamento preguiçoso. Recusado pela mesma razão do
Tracking peso: o JPA é o que se encontra em quase todo projeto Spring, e a
stack é a mesma de propósito (ADR-0002).
