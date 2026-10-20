# Convenções do backend

Regras de escrita, não de negócio. O que vale como negócio está em
`regras-de-negocio.md`, e o porquê de cada ferramenta está nos ADR. O formato
segue o contrato de arquitetura do paineis-kanban: stack com versão, pastas,
dependência, regras, nomes, testes e proibido.

## 1. Stack (obrigatória)

Conferida em 10/10/2026, nas fontes oficiais: Maven Central, postgresql.org e
o GitHub do Caddy. Versão "do Spring Boot" é a que o Spring Boot gerencia: ela
não se declara no `pom.xml`, para não brigar com o resto do conjunto.
O mesmo vale para o google-java-format, que fica na versão que o Spotless traz:
a 1.37 avulsa quebra o Spotless 3.10.

| Camada | Tecnologia |
|:--|:--|
| Linguagem | Java 25 (LTS) |
| Framework | Spring Boot 4.1, com Spring MVC e Spring Security 7.1 |
| Build | Maven 3.10, pelo Maven Wrapper |
| Acesso ao banco | Spring Data JPA, com Hibernate 7.4, versão do Spring Boot |
| Migração | Flyway, versão do Spring Boot |
| Banco | PostgreSQL 18 |
| Driver do banco | PostgreSQL JDBC, versão do Spring Boot |
| Login Google e JWT | Spring Security OAuth2 Client e Resource Server, versão do Spring Boot |
| Contrato de API | springdoc-openapi 3.1, só no perfil de desenvolvimento |
| Testes | JUnit 6 e Testcontainers 2.0, versões do Spring Boot; ArchUnit 1.5 |
| Formatação | Spotless 3.10, com o google-java-format da versão do Spotless |
| Análise | Error Prone 2.50, com NullAway 0.14 no pacote de regras |
| Contêiner | Docker, com Compose |
| Servidor web | Caddy 2.11 |

Atualizar uma versão é mudança desta tabela, no mesmo commit que a muda no
`pom.xml`.

## 2. Pastas

    backend/
    ├── pom.xml              dependências e plugins (Spotless, Error Prone)
    ├── mvnw                 o Maven Wrapper, que fixa a versão do Maven
    ├── Dockerfile           a imagem da api, em dois estágios
    ├── .env.example         as variáveis de ambiente, sem os valores
    └── src/
        ├── main/java/ecoa/
        │   ├── controller/  Spring MVC: recebe, chama o service, devolve DTO
        │   ├── service/     abre a transação, chama repository e regras
        │   ├── repository/  Spring Data JPA: as consultas
        │   ├── entity/      JPA: as tabelas
        │   ├── dto/         records: entrada e saída de cada rota
        │   ├── mapper/      conversão entre entidade e DTO, à mão
        │   ├── exception/   tratador global e formato único de erro
        │   ├── config/      segurança, relógio e configuração
        │   ├── comando/     os modos migrar e cadastrar-operador
        │   └── regras/      não importa nada além da biblioteca padrão
        ├── main/resources/db/migration/   migrações SQL do Flyway
        └── test/java/ecoa/  mesma divisão de main

Fora do versionamento: `backend/.env` e `backend/target/`.

## 3. Dependência

    controller -> service -> repository -> banco
                     \-> regras

O controller não tem consulta nem decisão; o service não sabe o que é HTTP;
`regras/` não importa ninguém (ADR-0012). O ArchUnit confere.

## 4. Regras

### Idioma dos identificadores

Domínio em português, vocabulário técnico em inglês, sem acento em
identificador.

| O que | Como | Exemplos |
|:--|:--|:--|
| Tabela | português, singular, snake_case | `conta`, `campanha`, `participacao`, `video`, `leitura`, `ganhador` |
| Coluna | português, snake_case | `usuario_tiktok`, `data_inicio`, `data_fechamento`, `views`, `motivo_recusa` |
| Classe | assunto em português, papel em inglês | `Campanha`, `VideoService`, `CampanhaController`, `RankingResponse` |
| Método de regra | português, camelCase | `statusDaCampanha`, `calcularRanking`, `leituraDeFechamento` |
| Vocabulário técnico | inglês | `controller`, `service`, `repository`, `mapper`, `config` |

O motivo: os termos do domínio não têm tradução limpa, e traduzir obriga a
manter um glossário mental entre a regra escrita e o código. "Views" fica como
está, porque é assim que se fala.

### Tipagem

- Views são `long` no Java e `bigint` no banco. Nunca `int`: vídeo viral passa
  de dois bilhões mais rápido do que parece.
- Data de regra (início, fim, fechamento, publicação, leitura, entrega) é
  `LocalDate` (ADR-0014). Instante que não decide regra (aceite, criação) é
  `Instant`, em `timestamptz`: o banco guarda só o instante, e um
  `OffsetDateTime` voltaria com outro fuso, que engana o `equals`.
- O "hoje" vem de um bean só, o relógio do sistema, no horário de Brasília
  (ADR-0014). Nenhum `LocalDate.now()` solto: o teste fixa o relógio, e a
  virada do dia não vira acaso.
- Ausência no retorno é `Optional`, como a leitura de fechamento que ainda não
  existe (RN-41). Nunca valor sentinela.
- `Optional` só no retorno: nunca em campo, parâmetro ou coleção.
- DTO é `record`.
- E-mail e @ são normalizados ao entrar (minúsculas; o @ sem arroba), num ponto
  só (RN-01, RN-11).

### Formato da resposta de erro

Um formato só, para todos os erros: os de regra, os de validação do Bean
Validation e os que o Spring gera sozinho. Um tratador global, em `exception/`,
converte toda exceção nesse formato.

```json
{
  "erro": {
    "codigo": "PUBLICACAO_FORA_DO_PRAZO",
    "campo": "dataPublicacao",
    "mensagem": "A data de publicação precisa estar entre 01/11/2026 e 30/11/2026.",
    "regra": "RN-36",
    "itens": []
  }
}
```

- `codigo` existe para o frontend decidir comportamento sem depender do texto.
- `campo` permite destacar o input certo, e é nulo quando a recusa não é de um
  campo.
- `mensagem` é o que a pessoa lê, e vem do backend porque a regra é do backend
  (ADR-0011).
- `regra` cita o documento que originou a recusa, para depuração.
- `itens` lista o que causou a recusa quando ela é sobre vários objetos, como os
  vídeos que impedem o encerramento. Vazio nos outros casos.

### Catálogo de códigos

Derivado das regras. Todo código novo entra aqui junto com a regra que o gera.

| Código | Status | Quando | Regra |
|:--|:--|:--|:--|
| `LOGIN_NAO_CONCLUIDO` | redirecionamento | cancelamento no Google ou verificação do retorno falha; vai como `motivo` na URL da tela de entrada | RN-01 |
| `EMAIL_NAO_VERIFICADO` | redirecionamento | o Google não devolveu o e-mail como verificado; vai como `motivo` | RN-01 |
| `SEM_PERMISSAO` | 403 | a função não permite a rota | RN-02 |
| `CONTA_BLOQUEADA` | 403 | criador bloqueado chamando rota que não seja a da própria conta ou as de autenticação | RN-18 |
| `NAO_ENCONTRADO` | 404 | recurso inexistente, ou de outro criador, sem distinguir os dois; rota inexistente, ou que não aceita o método | RN-02 |
| `CORPO_INVALIDO` | 422 | corpo malformado, ou valor que não tem o tipo do campo, como views com parte decimal, antes de chegar à regra | |
| `ERRO_INTERNO` | 500 | erro que nenhuma regra previu; a mensagem é genérica, e o log guarda só a classe e o lugar de cada causa (RNF-16) | |
| `EMAIL_JA_CADASTRADO` | 409 | cadastrar operador ou gravadora com e-mail que já tem conta | RN-04 |
| `PROPRIA_CONTA` | 409 | operador removendo a própria conta | RN-06 |
| `ULTIMO_OPERADOR` | 409 | remoção que deixaria o sistema sem nenhum operador | RN-06 |
| `USUARIO_TIKTOK_INVALIDO` | 400 | @ vazio ou com espaço no meio | RN-11 |
| `USUARIO_TIKTOK_EM_USO` | 409 | aprovar conta, ou corrigir @, quando o @ já é de outra conta aprovada ou bloqueada | RN-11, RN-13, RN-15 |
| `USUARIO_TIKTOK_FIXO` | 409 | criador tentando mudar o @ da conta aprovada ou bloqueada | RN-15 |
| `CONTA_NAO_PENDENTE` | 409 | aprovar ou recusar conta que não está pendente | RN-13 |
| `CONTA_SEM_USUARIO_TIKTOK` | 409 | aprovar ou recusar conta pendente que ainda não informou o @ | RN-13 |
| `CONTA_NAO_APROVADA` | 409 | entrar em campanha, ou bloquear, com conta que não está aprovada; ou o operador corrigir o @ de conta pendente ou recusada | RN-15, RN-16, RN-17 |
| `CONTA_NAO_BLOQUEADA` | 409 | desbloquear conta que não está bloqueada | RN-21 |
| `MOTIVO_AUSENTE` | 400 | recusa, bloqueio, desbloqueio ou desqualificação sem motivo | RN-13, RN-17, RN-21, RN-37, RN-38 |
| `LINK_REFERENCIA_AUSENTE` | 400 | campanha sem link de referência | RN-22 |
| `LINK_INVALIDO` | 400 | link de referência que não é endereço http ou https | RN-23 |
| `DATAS_FORA_DE_ORDEM` | 400 | início, fim e fechamento fora de ordem | RN-24 |
| `FECHAMENTO_NAO_FUTURO` | 400 | fechamento novo igual ou anterior a hoje | RN-26 |
| `DATAS_EXCLUEM_VIDEO` | 409 | início ou fim novo deixaria vídeo aprovado de fora; `itens` lista os vídeos | RN-27 |
| `CAMPANHA_ENCERRADA` | 409 | mudança em campanha encerrada, fora dos ganhadores | RN-28 |
| `CAMPANHA_COM_PARTICIPANTE` | 409 | apagar campanha com participante | RN-30 |
| `ACEITE_AUSENTE` | 400 | entrar na campanha sem o aceite | RN-31 |
| `ENTRADA_ENCERRADA` | 409 | entrar em campanha em apuração ou encerrada | RN-32 |
| `NAO_PARTICIPANTE` | 403 | criador enviando vídeo, ou vendo resultado, de campanha em que não participa | RN-31, RN-47 |
| `CAMPANHA_NAO_ABERTA` | 409 | enviar vídeo com a campanha agendada, em apuração ou encerrada | RN-34 |
| `LINK_NAO_TIKTOK` | 400 | link de vídeo que não é https, ou cujo domínio não é `tiktok.com` nem termina em `.tiktok.com` | RN-34 |
| `LINK_JA_ENVIADO` | 409 | link já enviado e não recusado | RN-35 |
| `VIDEO_NAO_EM_ANALISE` | 409 | aprovar ou recusar vídeo que não está em análise | RN-36, RN-37 |
| `PUBLICACAO_FORA_DO_PRAZO` | 409 | data de publicação fora do início e do fim | RN-36 |
| `CRIADOR_BLOQUEADO` | 409 | aprovar vídeo de criador bloqueado, ou registrá-lo como ganhador | RN-19, RN-52 |
| `VIDEO_NAO_APROVADO` | 409 | desqualificar ou registrar leitura em vídeo que não está aprovado | RN-38, RN-39 |
| `VIEWS_INVALIDAS` | 400 | views negativas | RN-39 |
| `DATA_FUTURA` | 400 | leitura ou entrega com data futura | RN-40, RN-52 |
| `LEITURA_ANTES_DA_PUBLICACAO` | 400 | data da leitura anterior à publicação | RN-40 |
| `LEITURA_DEPOIS_DO_FECHAMENTO` | 409 | leitura a partir do fechamento, em data diferente da leitura de fechamento que o vídeo já tem | RN-41 |
| `ENCERRAMENTO_ANTES_DO_FECHAMENTO` | 409 | encerrar antes da data de fechamento | RN-50 |
| `ENCERRAMENTO_COM_PENDENCIA` | 409 | vídeo em análise, ou vídeo que conta sem leitura de fechamento; `itens` lista os vídeos | RN-50 |
| `GANHADOR_ANTES_DO_INICIO` | 409 | registrar ganhador antes do início da campanha | RN-52 |
| `GANHADOR_NAO_PARTICIPANTE` | 409 | registrar como ganhador quem não participa da campanha | RN-52 |
| `CONFIRMACAO_NAO_CONFERE` | 400 | e-mail digitado na exclusão de conta não confere | RN-59 |

### Onde mora o quê

**Validação de forma e de campo isolado** fica no DTO, por Bean Validation:
obrigatoriedade, tipo, tamanho.

**Validação que depende de outro dado** fica em `regras/`: o prazo de
publicação precisa das datas da campanha, a leitura de fechamento precisa das
outras leituras.

**Transação** é aberta e fechada no service, com `@Transactional`. A função de
regra nunca recebe o repositório (ADR-0012).

**Decisão** nunca fica no controller. O controller recebe, chama o service,
devolve.

**Consulta ao banco** só acontece dentro do service (ADR-0005).

**Conversão entre entidade e DTO** fica em `mapper/`, escrita à mão (ADR-0005).
O DTO servido a outro criador ou à gravadora não tem e-mail nem código de
verificação (RN-57). O operador recebe o código na fila de verificação e na
lista de contas, porque é com ele que confere a bio (RF-11, RF-15, RN-12).
Na lista de contas, recebe também o e-mail, que digita para confirmar a
exclusão (UC-19).

**Restrição de integridade que o banco consegue impor** vai para o banco
**também**, e não em vez de: a aplicação devolve mensagem que a pessoa entende,
e o banco pega o caminho que alguém esqueceu de validar (ADR-0004).

**Função e isolamento** ficam na aplicação, e só nela (ADR-0009).

### Configuração

| O quê | Valor | Por quê |
|:--|:--|:--|
| `spring.jpa.hibernate.ddl-auto` | `validate` | o esquema pertence às migrações (ADR-0005) |
| `spring.jpa.open-in-view` | `false` | consulta só dentro do service (ADR-0005) |
| `spring.flyway.enabled` | `false` na api; ligado no modo `migrar` e nos testes | a api nunca migra ao subir (ADR-0006) |
| springdoc e Swagger UI | só no perfil de desenvolvimento | os tipos do frontend são gerados lá (ADR-0010) |
| nível de log | só erro | RNF-16 |
| fuso do relógio do sistema | `America/Sao_Paulo` | ADR-0014 |

Senha, chave e token chegam por variável de ambiente, nunca por arquivo
versionado (ADR-0018). Variável obrigatória ausente derruba a api na subida,
em vez de virar nulo três camadas adiante.

O mesmo `.jar` tem três modos:

- sem argumento, é a api;
- `migrar` aplica as migrações e termina (ADR-0006);
- `cadastrar-operador <e-mail>` cria a conta de operador e termina (RF-08).

### Migração de esquema

- Toda migração é um arquivo SQL novo, escrito à mão, em
  `src/main/resources/db/migration/`, com nome
  `V<ano>_<mes>_<dia>_<hora>__<acao>.sql`, por exemplo
  `V2026_11_02_1402__cria_tabelas.sql`. Zero à esquerda, para a ordem dos nomes
  ser a ordem em que rodam.
- O arquivo não tem `BEGIN` nem `COMMIT`: o Flyway abre a transação.
- Não há reversão: o plano de recuperação é o backup (ADR-0006).
- O ensaio do deploy restaura o dump e aplica as migrações antes da produção
  (RNF-09). Migração que falha no ensaio não chega ao banco de produção.

## 5. Nomes

| O quê | Regra | Exemplo |
|:--|:--|:--|
| Tabela | singular, snake_case, nome da entidade | `participacao` |
| Coluna | snake_case, sem repetir o nome da tabela, fora a chave primária | `video.link`, nunca `video.video_link` |
| Chave primária | `id_<tabela>` | `campanha.id_campanha` |
| Chave estrangeira | `xid_<tabela apontada>` | `video.xid_participacao` |
| Campo Java e coluna | camelCase no código, snake_case no banco | `dataFechamento` e `data_fechamento` |
| Classe de regra | `RegrasDa` ou `RegrasDo` mais o assunto, em `regras/` | `regras/RegrasDaCampanha.java` |
| Campo no JSON | camelCase, igual ao campo do DTO | `numeroAtual`, `dataPublicacao` |
| Teste | classe testada + `Test` | `RegrasDaCampanhaTest` |

Nas chaves, o prefixo diz se a coluna é a chave da própria tabela ou aponta
para outra.

## 6. Testes

Seguem o ADR-0013 e a seção Testes do `fluxo-de-trabalho.md`.

- Regra é testada sem banco. Rota é testada contra Postgres real, pelo
  Testcontainers, nunca contra SQLite ou um falso no lugar do banco.
- Toda rota tem teste de função errada; a que lida com dado de criador tem teste
  de que um criador não alcança o dado de outro (ADR-0009).
- Toda recusa do catálogo tem teste que a provoca.
- Dado de teste é inventado, nunca copiado de criador real: e-mail e @ são de
  pessoas.

## 7. Proibido

1. `catch (Exception e)` largo que não relança. Capturar exceção específica, ou
   deixar subir até o tratador global.
2. `LocalDateTime` ou `Instant` onde a regra fala de data (ADR-0014).
3. `LocalDate.now()` fora do relógio do sistema (ADR-0014).
4. Importar `org.springframework` ou `jakarta.persistence` dentro de `regras/`
   (ADR-0012). O ArchUnit verifica.
5. Decisão de regra dentro do controller.
6. Injeção por campo, com `@Autowired` no atributo. A dependência entra pelo
   construtor.
7. `System.out.println` ou `printStackTrace`. O log existe e é só erro
   (RNF-16).
8. Valor sentinela para representar ausência.
9. Editar migração já aplicada. Correção entra como migração nova.
10. Consulta dentro de laço, inclusive o N+1 do JPA, que acontece sem laço
    visível (ADR-0005).
11. E-mail ou @ em linha de log, inclusive pelo caminho indireto (RNF-16).
12. Devolver entidade no controller. A rota devolve DTO (ADR-0005).
13. Segredo no código ou no repositório (ADR-0018).
