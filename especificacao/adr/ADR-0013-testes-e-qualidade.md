# ADR-0013 Testes em três camadas, com qualidade na integração contínua

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

A RNF-28 exige cobertura das regras de status, leitura de fechamento,
ranking e encerramento. As regras não importam framework (ADR-0012), o que
permite testá-las sem banco. O isolamento entre funções e entre criadores
(ADR-0009) só se prova contra o banco de verdade.

Qualidade de código são três trabalhos diferentes: formatação, defeito que
compila, e nulo que ninguém tratou.

## Decisão

**Três camadas de teste**, com critério de corte em cada uma.

1. **Regras**, sem banco: função recebe dados e devolve valor. É onde mora a
   maior parte dos testes.
2. **Rotas**, contra Postgres real e descartável, que o Testcontainers sobe. A
   rota é chamada pelo MockMvc, cada teste roda numa transação revertida no
   fim, e o login é simulado pelo suporte de teste do Spring Security, sem o
   Google.
3. **Um teste de fumaça de ponta a ponta**, com Playwright. O preparo insere um
   token de renovação direto no banco e põe o cookie no navegador, sem nenhum
   código de produção envolvido.

No frontend, testa-se com Vitest o componente que **decide**, não o que só
desenha.

**Qualidade**, rodando local e na integração contínua (ADR-0017):

- o Spotless formata o código Java num estilo pronto, o google-java-format;
- o Error Prone roda junto da compilação e acusa defeito, não estilo;
- o NullAway acusa nulo não tratado, só no pacote de regras.

## Consequências

A camada 1 roda em milissegundos e cobre o que mais importa. A camada 2 pega o
defeito de permissão. A camada 3 pega a única classe de defeito que as outras
não pegam: fiação, como botão chamando a rota errada ou cache que não foi
invalidado.

Fica descoberto de propósito: a cadeia real de redirecionamento do Google, que
se cobre por teste de integração no backend contra um `id_token` fabricado.

Custo aceito: configuração no build e uma compilação um pouco mais lenta.

## Alternativa recusada: SQLite como substituto do Postgres nos testes

Mais rápido, sem banco de pé. Recusado porque difere do Postgres em tipo,
restrição e índice parcial, e teste que passa no substituto e falha em produção
entrega confiança falsa.

## Alternativa recusada: o Postgres de desenvolvimento nos testes

Dependeria de ele estar de pé, misturaria dado de teste com o de
desenvolvimento, e não rodaria igual na integração contínua.

## Alternativa recusada: suíte ampla de ponta a ponta

Cobriria de novo, mais devagar e com mais manutenção, o que as camadas 1 e 2 já
cobrem.

## Alternativa recusada: rota de teste que emite sessão

Uma rota que emite sessão sem autenticar ninguém, protegida só por
configuração. Um deploy mal configurado viraria desvio de autenticação exposto
na internet.

## Alternativa recusada: Cypress

Roda o teste dentro do navegador, sob a política de mesma origem, e o
paralelismo depende da nuvem do fornecedor. O Playwright dirige o navegador de
fora.

## Alternativa recusada: NullAway no projeto inteiro

Código de integração lida com tipo de terceiro, e a checagem estrita ali vira
anotação de exceção espalhada, que esvazia a ferramenta.

## Alternativa recusada: Checkstyle

Confere estilo, não defeito, e o estilo já fica com o Spotless.
