# Ecoa

Plataforma que conecta uma gravadora a criadores de conteúdo que postam vídeos
com a música dela. O criador posta no TikTok e cadastra o link, a operação
acompanha as views, e o ranking e os ganhadores ficam visíveis a quem participa.
A gravadora define o prêmio, que pode ser dinheiro ou produto, e paga direto ao
ganhador.

A ideia inteira, com várias redes e coleta automática, está em
[visao/visao-do-produto.md](visao/visao-do-produto.md). O que se constrói agora
é o **MVP**: só TikTok, com as views lidas à mão pela operação, uma gravadora,
uma campanha por vez e até 50 criadores.

## Estado atual

**A especificação do MVP está completa. Não há código de aplicação ainda.**

O que existe hoje:

- `especificacao/`: 65 regras de negócio, 42 requisitos funcionais, 20 casos de
  uso, 28 requisitos não funcionais, 18 ADR, arquitetura, convenções e o
  contrato da API.
- `visao/`: a visão do produto, para aprovação, e a pesquisa das redes, para a
  Fase 2 em diante.
- `historico/`: o registro das decisões que recortaram o MVP e o que o Mateus
  pediu.

## Onde ler o quê

| Se você quer saber | Abra |
|:--|:--|
| a ideia completa, além do MVP | [visao/visao-do-produto.md](visao/visao-do-produto.md) |
| APIs, legalidade e aprovação em cada rede | [visao/pesquisa-das-redes.md](visao/pesquisa-das-redes.md) |
| por que o MVP é como é | [historico/10-10-grill-mvp.md](historico/10-10-grill-mvp.md) |
| quem usa o sistema | [especificacao/atores.md](especificacao/atores.md) |
| o que é permitido, recusado e por quê | [especificacao/regras-de-negocio.md](especificacao/regras-de-negocio.md) |
| o que o sistema faz | [especificacao/requisitos-funcionais.md](especificacao/requisitos-funcionais.md) |
| como cada interação acontece, inclusive quando dá errado | [especificacao/casos-de-uso.md](especificacao/casos-de-uso.md) |
| o que o sistema tem que ser (custo, backup, privacidade) | [especificacao/requisitos-nao-funcionais.md](especificacao/requisitos-nao-funcionais.md) |
| por que cada tecnologia foi escolhida, e o que perdeu | [especificacao/adr/](especificacao/adr/) |
| como se escreve um ADR novo | [especificacao/modelo-adr.md](especificacao/modelo-adr.md) |
| o que roda onde, e por onde a requisição passa | [especificacao/arquitetura.md](especificacao/arquitetura.md) |
| como se escreve código aqui, com as versões | [convenções do backend](especificacao/convencoes-backend.md) e [do frontend](especificacao/convencoes-frontend.md) |
| o contrato da API | [especificacao/endpoints.md](especificacao/endpoints.md) |
| como o trabalho entra no repositório | [especificacao/fluxo-de-trabalho.md](especificacao/fluxo-de-trabalho.md) |
| **como começar a implementar: armadilhas, recusas e o desenho do esquema** | [instrucao.md](instrucao.md) |
| o que construir e em que ordem | [tarefas.md](tarefas.md) |

Cada documento cita os outros por identificador (`RN-41`, `RF-28`, `UC-10`,
`ADR-0001`) em vez de repetir texto. Identificador é estável: número aposentado
nunca volta.

A seção de **decisões recusadas** das regras e a **alternativa recusada** de
cada ADR são a parte que impede a mesma discussão de recomeçar com o argumento
já gasto.

## Stack

Decidida com alternativa registrada em cada caso, e com a versão de cada peça
nas convenções.

- **Backend:** Java 25 com Spring Boot 4.1 e Maven, Spring Data JPA, Flyway,
  Spring Security com JWT em cookie
- **Frontend:** React 19 com Vite e TypeScript 5.9, TanStack Query, React Router
  8, Tailwind com shadcn/ui, React Hook Form com Zod, Recharts
- **Banco:** PostgreSQL 18
- **Identidade:** Google, sem senha própria
- **Produção:** VPS com Docker Compose, e o Caddy na frente fazendo HTTPS
- **Backup:** dump diário baixado para a máquina de um operador
- **Qualidade:** Spotless, Error Prone com NullAway, JUnit com Testcontainers e
  ArchUnit, Vitest, Playwright, tudo em integração contínua

## O que falta

O design das telas e o código. O ponto de partida está no
[instrucao.md](instrucao.md), e a ordem de construção, no
[tarefas.md](tarefas.md).
