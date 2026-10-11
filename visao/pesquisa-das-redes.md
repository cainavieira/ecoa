# Pesquisa das redes

**Versão:** 0.2 · **Data:** 10/10/2026
**Público:** quem for automatizar a coleta, da Fase 2 em diante. Não é
material de apresentação.

Pesquisa feita para a visão completa (`visao-do-produto.md`): como ler as
métricas de cada rede, o que é permitido e o que cada rede exige para aprovar
um app. **Nada aqui vale para o MVP**, que lê as views à mão (visão §9.1). O que
o MVP decidiu está na `especificacao/`.

API e termos mudam. O que é do TikTok foi conferido na documentação oficial em
10/10/2026. O que é da Meta e do YouTube vem do rascunho de julho de 2026 e não
foi conferido de novo.

Partes: **A)** como automatizar a coleta · **B)** antifraude · **C)**
viabilidade e legalidade · **D)** API por rede · **E)** conexão de conta ·
**F)** aprovação e formalização · **G)** fontes e cadência.

---

## A) Como automatizar a coleta

Cinco peças costumam ser tratadas como alternativas, e não são: são camadas
diferentes.

| Peça | O que é | Papel na coleta |
|---|---|---|
| **Script** | o código que chama a API e grava o resultado | a base; existe sempre |
| **Cron** | o agendador que dispara o script | quem dá o play na coleta periódica |
| **Polling** | a estratégia de perguntar de tempos em tempos "quantas views agora?" | o coração da coleta |
| **Webhook** | a rede avisa quando algo acontece | quase inaplicável aqui |
| **MCP** | protocolo que expõe ferramentas e dados a um agente de IA | camada de IA, não de coleta |

**Polling agendado, recomendado como base.**
- Funciona com todas as APIs em jogo: TikTok, YouTube e Instagram são de
  pergunta e resposta. É simples, previsível, e a cota fica sob controle.
- É barato onde importa: no TikTok, uma chamada lê até 20 vídeos, com limite de
  600 chamadas por minuto; no YouTube, `videos.list` custa 1 unidade e lê até
  50 vídeos.
- Custa requisição mesmo quando nada mudou, e o número só aparece na próxima
  rodada.

**Webhook.** Seria tempo real e sem desperdício, mas as redes não avisam quando
a contagem de views muda. Serve só para gatilho interno, como disparar a
primeira leitura quando o criador cadastra um link.

**MCP.** Vale se entrar um agente de IA no fluxo, como um assistente na
verificação manual ou para consultas ("como está a campanha X?"). Para a coleta
recorrente, é uma camada sem necessidade: bater numa API de tempos em tempos é
trabalho de um serviço comum.

**Recomendação:** serviço de coleta com polling agendado, com leitura mais
frequente logo depois da postagem, quando as métricas crescem rápido,
espaçando até o fechamento da campanha. Webhook só para gatilho interno; MCP
só se houver agente de IA, fora do caminho da coleta.

---

## B) Antifraude: por que pegar carona nas redes

As redes já filtram fraude, mas não dá para confiar só nelas:

1. **Elas filtram para os fins delas** (anúncio, spam), não para proteger o
   orçamento da gravadora. O número da API ainda pode conter views infladas.
2. **Premiar por view cria um incentivo novo:** atrai quem compra views baratas
   para ganhar o prêmio, um vetor que a rede não tem motivo para bloquear.
3. **A fraude que mais ameaça é a que as redes não cobrem:**
   - **contas múltiplas:** uma pessoa com várias contas reais disputando
     vários prêmios. Só verificação de identidade pega isso;
   - **descumprimento da campanha:** usou o som oficial? seguiu o regulamento?
     repostou vídeo de outra pessoa? A rede não confere nada disso.

**O que permite ser leve:** as redes removem views falsas ao longo de alguns
dias. Se o resultado só fecha depois que a métrica estabiliza, a plataforma
herda essa limpeza de graça. Em vez de construir um detector de fraude, espera
a poeira baixar e decide em cima do número já higienizado.

O desenho que sai disso:
- **verificação da conta**, que é insubstituível;
- **estabilização antes do fechamento**, para o grosso da fraude de
  engajamento;
- **guarda-corpos:** teto por vídeo e por criador quando o prêmio for por
  taxa, e conferência dos maiores ganhadores.

No MVP, isso virou código na bio com conferência humana, fechamento depois da
estabilização e conferência dos ganhadores pela operação. O MVP não guarda CPF,
então não impede a mesma pessoa de participar com duas contas; isso só aparece
se as duas ganharem (`especificacao/`).

---

## C) Viabilidade e legalidade da coleta

| | O que é | Situação |
|---|---|---|
| **Visível** | uma pessoa vê views, curtidas e comentários num post público | sempre |
| **Permitido** | coletar de forma automatizada | cinzento. Os termos (a Meta à frente) proíbem coleta automatizada. A Justiça dos EUA deu ganho a scraper de dado público deslogado (Meta v. Bright Data, 2024), mas a Meta bloqueia e processa quando quer, e a LGPD vale mesmo para dado público |
| **Viável em escala** | um robô pegar de forma confiável, milhares de vezes | difícil sem API: login, captcha, impressão digital do navegador, limite de requisição, HTML que muda |

**A favor:** o criador é parceiro e quer ganhar, então coopera conectando a
conta, e a plataforma lê pela API oficial. Isso elimina o scraping.

**Mas a API tem contrato próprio.** Os Termos do Desenvolvedor do TikTok,
modificados em 26/12/2025, proíbem, sem consentimento escrito do TikTok:
- uso "for any commercial or unauthorized purpose, including without
  limitation communicating or facilitating any commercial advertisement or
  solicitation" (III.3(c));
- montar "profiles, databases, or similar records" sobre pessoa ou conteúdo
  (III.3(h));
- repassar a terceiro o que vem pela API (II.1 e III.3(b));
- usar a API "to compete with or replicate any TikTok Services" (III.3(p)).

Uma campanha paga de música, com histórico de views por criador e painel para a
gravadora, encosta nas quatro. O dado ser público não muda isso: os termos
limitam o uso do que chega pela API. Leitura jurídica antes da Fase 2. Os
termos de desenvolvedor da Meta e do YouTube não foram lidos.

---

## D) API por rede

| Rede | Oficial? | Como | Pegadinha |
|---|---|---|---|
| **TikTok** | Sim: views, curtidas, comentários, compartilhamentos | Display API com conta conectada (Login Kit): `POST /v2/video/query/`, escopo `video.list` | Só vídeos **públicos da própria conta** autenticada; o vídeo não informa o som usado; o oEmbed público não traz contagem |
| **Instagram** | Sim: Reels com plays, alcance, curtidas, comentários | Graph API com conta **Profissional ou Creator** | Conta pessoal não tem API (a Basic Display acabou em 04/12/2024); o app precisa ser aprovado pela Meta, em cerca de 4 a 6 semanas; "views" de vídeo comum foi descontinuado em janeiro de 2025 |
| **Facebook** | Sim: posts de Páginas | Graph API | Precisa ser **Página**, não perfil |
| **YouTube** | Sim, e é **o mais fácil** | Data API v3 com chave de API: `viewCount`, `likeCount`, `commentCount` de qualquer vídeo público | Curtidas podem estar ocultas, se o criador desativou |

**TikTok em detalhe**, conferido em 10/10/2026: até 20 vídeos por chamada; 600
chamadas por minuto por endpoint, em janela deslizante; `/v2/video/list/`
lista os vídeos públicos mais recentes da conta, 20 por página, do mais novo
para o mais velho. A documentação da v1 (`tiktok-api-v1-video-list`) está
marcada como legado.

**YouTube em detalhe:** `videos.list` com chave de API, sem login do criador e
sem revisão do app. Cada chamada custa 1 unidade e lê até 50 vídeos; a cota
grátis é de 10.000 unidades por dia, cerca de 500 mil vídeos por dia. Desde
31/03/2025, Shorts contam view como TikTok e Reels, o que deixa a comparação
mais justa.

> **Views é a métrica mais instável** entre as redes, e por isso a fórmula da
> visão (§8.1) tem fator por plataforma.

---

## E) Conexão de conta (OAuth)

Para TikTok e Instagram, o criador conecta a conta e a plataforma recebe um
token:

1. clica em "Conectar conta";
2. entra na página da própria rede, e a plataforma nunca vê a senha;
3. autoriza a permissão de ler vídeos e estatísticas;
4. a rede devolve um código, que o servidor troca por um **token de acesso** e
   um **token de renovação**;
5. a plataforma lê as estatísticas dos vídeos daquela conta;
6. o token de renovação renova o de acesso, para medir o vídeo por dias sem
   novo login.

No TikTok, conferido em 10/10/2026: o token de acesso vale 24 horas e o de
renovação, 365 dias; a renovação pode devolver um token de renovação novo, que
passa a valer no lugar do antigo; o endereço de retorno do login precisa ser
`https` e estar registrado no app.

O link do vídeo (qual vídeo, de qual campanha) e a conta conectada (que libera
os dados) são complementares.

**Instagram: dois "aprovar" que se confundem.**

| O quê | Quem faz | Quanto demora |
|---|---|---|
| Deixar a conta Profissional ou Creator | o criador, nas configurações do Instagram | na hora, sem custo |
| Revisão do app pela Meta | a plataforma, uma vez, antes de lançar | cerca de 4 a 6 semanas |

O YouTube não tem nada disso: só chave de API.

---

## F) Aprovação e formalização

### Níveis de acesso na Meta

| Nível | Para que serve | Precisa de App Review? |
|---|---|---|
| **Standard Access**, modo desenvolvimento | contas que têm papel no app: o administrador e os testadores adicionados | Não |
| **Advanced Access**, produção | qualquer criador, inclusive contas que a plataforma não controla | Sim |

A fronteira não é volume. É de desenho: sem aprovação, a API só funciona para
as contas testadoras cadastradas, e um estranho simplesmente não conecta, seja
1 ou 1.000.

### O portão de cada rede

| Rede | Aprovar o app para produção? | CNPJ ou MEI? |
|---|---|---|
| **TikTok** | Sim. Sem aprovação, só o sandbox, com até 10 contas que sejam nossas. A revisão pede site público completo (não pode ser só login), termos e política de privacidade visíveis, URLs verificadas e vídeo do fluxo inteiro gravado no sandbox; app em desenvolvimento não é aprovado; não há prazo informado | Não exigido para este tipo de app; organização "highly recommended". Verificação de empresa só é obrigatória para minijogos e monetização |
| **Instagram e Facebook (Meta)** | Sim: App Review para Advanced Access | Sim, a Verificação de Negócio; MEI serve |
| **YouTube** | Não: dados públicos com chave de API | Não |

### Ordem

1. **Teste**, sem custo nem burocracia: construir, testar com as próprias
   contas (testadoras no TikTok e na Meta) e validar o loop.
2. **Preparar a produção**, começando cedo porque demora: termos, política de
   privacidade, site público, Verificação de Negócio na Meta e revisão do app
   em cada rede.
3. **Escalar:** com tudo aprovado, abrir para qualquer criador.

> Dá para ir longe sem gastar nada. Formalize quando for escalar de verdade,
> mas peça as aprovações com semanas de antecedência, porque a análise demora.

---

## G) Fontes e cadência da coleta

**Camadas de fonte, da melhor para a pior:**
1. **Conta conectada, pela API oficial:** legal, confiável e automatizável.
2. **Link com conferência humana:** a operação abre o vídeo e anota o número.
   É a fonte do MVP.
3. **Leitura pública pontual** (oEmbed, página do link): frágil e sujeita a
   bloqueio. No TikTok, o oEmbed não traz contagem nenhuma.
4. **Scraping:** último recurso; evitar na Meta; revisão jurídica obrigatória.

**Cadência:** mais frequente logo depois da postagem, quando as métricas
crescem rápido, espaçando até o fechamento; depois, congela. O resultado só
sai depois da estabilização (parte B).

---

## Referências

- **TikTok**, conferidas em 10/10/2026: [Display API](https://developers.tiktok.com/doc/display-api-overview) · [Query Videos](https://developers.tiktok.com/docs/en/tiktok-api-v2-video-query) · [List Videos](https://developers.tiktok.com/doc/tiktok-api-v2-video-list) · [Escopos](https://developers.tiktok.com/doc/tiktok-api-scopes) · [Tokens](https://developers.tiktok.com/doc/oauth-user-access-token-management) · [Limites](https://developers.tiktok.com/doc/tiktok-api-v2-rate-limit) · [oEmbed](https://developers.tiktok.com/doc/embed-videos) · [Login Kit Web](https://developers.tiktok.com/docs/en/login-kit-web) · [Sandbox](https://developers.tiktok.com/docs/en/add-a-sandbox) · [Revisão do app](https://developers.tiktok.com/docs/en/app-review-guidelines) · [Registrar o app](https://developers.tiktok.com/docs/en/getting-started-create-an-app) · [Verificação de empresa](https://developers.tiktok.com/docs/en/verify-your-business) · [Termos do Desenvolvedor](https://www.tiktok.com/legal/page/global/tik-tok-developer-terms-of-service/en)
- **Instagram e Meta:** [Media Insights](https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/insights/) · [Instagram Platform](https://developers.facebook.com/docs/instagram-platform/overview/) · [App Review](https://developers.facebook.com/docs/instagram-platform/app-review/) · [Verificação da empresa](https://pt-br.facebook.com/business/help/1095661473946872) · [Documentos aceitos na verificação](https://docs.360dialog.com/docs/resources/meta-business-verification)
- **YouTube:** [Data API, chave de API](https://developers.google.com/youtube/v3/getting-started) · [Custo de cota](https://developers.google.com/youtube/v3/determine_quota_cost) · [Recurso videos](https://developers.google.com/youtube/v3/docs/videos) · [Histórico de revisões](https://developers.google.com/youtube/v3/revision_history)
- **Legalidade:** [Meta, Automated Data Collection Terms](https://www.facebook.com/legal/automated_data_collection_terms) · [Meta v. Bright Data (2024)](https://www.zyte.com/blog/california-court-meta-ruling/)
