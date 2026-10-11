# ADR-0004 Postgres como banco de dados

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

Criadores, operadores e gravadora usam o sistema ao mesmo tempo. Há restrições
que o banco consegue impor, algumas só para parte das linhas: o @ é único entre
contas aprovadas e bloqueadas (RN-11), o link é único entre envios não
recusados (RN-35), e há uma leitura por vídeo por data (RN-40).

O teste de rota roda contra o mesmo banco da produção (ADR-0013).

## Decisão

PostgreSQL, na versão das convenções do backend.

## Consequências

As restrições do contexto viram chave única e índice único parcial, que valem
**junto** com a verificação na aplicação, e não no lugar dela: a aplicação
devolve a recusa com mensagem, e o banco pega o caminho que alguém esqueceu de
validar.

Aparecem a migração versionada (ADR-0006) e o backup com restauração testada
(ADR-0016). Em desenvolvimento, o Postgres roda em Docker (RNF-27).

## Alternativa recusada: SQLite

Daria conta da carga (RNF-25), e o backup seria copiar um arquivo. Recusado por
três motivos: o teste de rota quer o mesmo banco da produção (ADR-0013); a
migração, o backup e o ensaio já estão desenhados para Postgres no Tracking
peso; e a Fase 2 traz coleta automática escrevendo ao mesmo tempo que as telas,
onde um escritor por vez começa a pesar.
