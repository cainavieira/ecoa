# ADR-0006 Flyway, com a migração antes da api e ensaio antes do deploy

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O esquema pertence às migrações (ADR-0005). Com o Flyway no projeto, o Spring
Boot aplica as migrações ao subir a api, a menos que isso seja desligado.
Migração que falha nessa hora derruba a api nova no meio do deploy, com o banco
pela metade.

O backup só serve se restaurar (RNF-07), e um dump deixa de restaurar
justamente quando o esquema muda (RNF-09). A produção roda em Docker Compose
(ADR-0015), sem Kubernetes.

## Decisão

Flyway, com as migrações escritas em SQL à mão e empacotadas no `.jar` da api.

A api nunca migra ao subir. A migração é um passo separado do deploy: o mesmo
`.jar`, em modo de migração, roda num contêiner de uma execução só, antes de a
api nova subir. Se ele falhar, a api nova não sobe.

Antes de tocar a produção, o deploy faz um ensaio: restaura o dump mais recente
num banco descartável, no mesmo Postgres, e aplica nele as migrações novas. Se
a restauração ou a migração falhar, o deploy para. O ensaio roda em todo
deploy, com ou sem migração nova.

Nos testes, a migração roda na subida, sobre o banco que o teste cria do zero.

## Consequências

O `.jar` tem três modos: a api, a migração e o cadastro do primeiro operador
(RF-08).

A ordem do deploy fica no script do ADR-0017.

Não há migração de reversão: o plano de recuperação é o backup. Em
desenvolvimento, desfazer é recriar o banco.

## Alternativa recusada: migração na subida da api

É o padrão do Spring Boot, e uma peça a menos. Recusada porque a falha de
migração derrubaria a api nova sem volta, e o ensaio não teria onde acontecer
antes.

## Alternativa recusada: initContainer do Kubernetes

É o desenho do Tracking peso. Não há Kubernetes aqui (ADR-0015), e o contêiner
de uma execução só do Compose faz o mesmo papel.
