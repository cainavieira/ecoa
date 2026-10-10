# Documento Técnico — Projeto "Ecoa"

**Versão:** 0.1 · **Complementa:** `documento-de-conceito.md` (a parte de negócio)
**Público:** implementação / dev. ⚠️ **Não é material de apresentação.**

> Regra de ouro: **negócio → `documento-de-conceito.md`** · **implementação → aqui.**
> Partes: **A)** requisições · **B)** antifraude · **C)** viabilidade & legalidade · **D)** referência de API · **E)** conexão de conta (OAuth) · **F)** arquitetura do sistema.

---

## A) A parte técnica: como fazer as requisições

O primeiro ponto importante é que **essas cinco coisas não competem entre si — elas são camadas diferentes**. Misturá-las como se fossem alternativas é o erro comum. Separando:

| Peça | O que realmente é | Papel no seu caso |
|---|---|---|
| **Script** | O código que chama a API e salva o resultado | A base — sempre existe, independente do resto |
| **Cron** | Um agendador que dispara o script no tempo | Quem "aperta o play" no polling |
| **Polling** | A *estratégia*: perguntar de tempos em tempos "quantas views agora?" | O coração da coleta |
| **Webhook** | A rede *te avisa* quando algo acontece (push) | Quase inaplicável aqui (ver abaixo) |
| **MCP** | Protocolo pra expor ferramentas/dados a um **agente de IA** | Camada de IA, não de coleta em produção |

**Prós e contras do que importa:**

- **Polling agendado por cron** *(recomendado como backbone)*
  - ✅ Compatível com **todas** as APIs em jogo — YouTube, TikTok e Instagram são todas *pull* (você pergunta, elas respondem). Simples, previsível, e você controla a quota.
  - ✅ Barato onde importa: no YouTube, `videos.list` custa 1 unidade e aceita 50 vídeos por chamada.
  - ❌ Gasta requisição mesmo quando nada mudou; e há a latência da cadência (você vê o número na próxima checagem, não na hora).

- **Webhook**
  - ✅ Tempo real, zero desperdício — *quando existe*.
  - ❌ **O problema:** as redes **não mandam webhook de "a contagem de views mudou"**. YouTube e TikTok não empurram deltas de métrica; a Meta tem alguns webhooks, mas não pra isso. Então, pra o seu caso principal (acompanhar métricas crescendo), webhook basicamente não está disponível. Ele só serve pra **gatilhos internos seus** (ex.: criador cadastrou um link → dispara a *primeira* coleta na hora).

- **MCP**
  - ✅ Excelente se houver um **agente de IA** no fluxo (ex.: um assistente que ajuda na verificação manual, ou consultas ad-hoc "me mostra o desempenho da campanha X"). Padroniza integrações pra esse agente.
  - ❌ É a ferramenta errada pro **pipeline de produção** de coleta recorrente — adiciona uma camada de abstração/IA que você não precisa pra simplesmente bater numa API a cada X horas. *(No teu WARNING do doc, onde você escreveu "individualizar as redes nesse MCP", o mais provável é que você quis dizer a **camada de integração das redes** — e essa camada é melhor como um serviço de coleta comum do que como MCP, a não ser que um agente de IA vá consumi-la.)*

**Recomendação pro plano:**
> Backbone = **serviço de coleta (script) + cron + polling**, com uma **janela de medição adaptativa**: coletar com mais frequência logo após a postagem (métricas crescem rápido no início — ex.: a cada poucas horas) e ir espaçando até o fim da janela da campanha (ex.: diário até 14 dias). Webhook, só pra gatilhos internos. MCP, só se/quando entrar um agente de IA — não no caminho crítico da coleta.

---

## B) A Parte 10 (antifraude/verificação) é mesmo necessária?

Ótima provocação, e a resposta honesta é: **sim, mas com o peso deslocado — não do jeito que você talvez esteja imaginando.** O argumento "as redes já verificam" é meio verdadeiro e meio armadilha:

**Por que não dá pra simplesmente confiar na verificação das redes:**
1. **As redes filtram fraude pra os fins *delas* (cobrança de anúncio, spam), não pra proteger o *seu* orçamento.** O número que você lê na API ainda pode conter views infladas — a rede não garante que aquele contador está limpo pro fim de te pagar.
2. **Você cria um incentivo novo que as defesas genéricas delas não foram feitas pra conter:** agora as pessoas são **pagas por view**. Isso atrai gente comprando views baratas *especificamente pra farmar o seu pagamento* — um vetor que a rede não tem motivo pra bloquear.
3. **O tipo de fraude que mais te ameaça é justamente o que as redes NÃO cobrem:**
   - **Contas falsas / múltiplas (Sybil):** uma pessoa com 10 contas "de verdade" sacando 10 pagamentos. → é exatamente aqui que a sua **verificação humana de identidade** é insubstituível. A rede não te protege disso.
   - **Descumprimento de campanha:** usou o áudio oficial? seguiu as regras? repostou vídeo de outro? — nada disso a rede checa por você.

**O que muda (e por que dá pra ser mais leve):** você **não precisa reconstruir do zero** a integridade de contagem de views. Dá pra **delegar boa parte à rede + a uma "janela de estabilização"** — e essa é a sacada elegante que responde sua pergunta:

> As redes **removem views falsas retroativamente** ao longo de dias. Se você **só paga depois que a métrica estabiliza** (ex.: fecha a contagem X dias após o post), você **herda a limpeza de fraude da rede de graça**. Ou seja: em vez de construir um detector de fraude pesado, você "espera a poeira baixar" e paga em cima do número já higienizado pela própria plataforma.

**Recomendação pro plano:** **não apague a Parte 10 — mas reenquadre e enxugue.** Fica assim:
- **Mantém, e é o mais importante:** verificação **de identidade/conta** (é pessoa real, não é multi-conta). Isso é seu, insubstituível.
- **Delega às redes + janela de estabilização:** o grosso da fraude de *engajamento*. Você fica leve aqui.
- **Guarda-corpos de pagamento** (que você já tem no modelo): teto por vídeo/criador, e **auditoria por amostragem** dos maiores ganhadores.
- **No MVP (YouTube-first):** pode começar **bem enxuto** — verificação de identidade + janela de estabilização + tetos já cobrem 90% do risco real.

Então a Parte 10 não morre; ela deixa de ser "vamos construir um antifraude robusto" e vira "**integridade de pagamento que pega carona nas redes**" — mais barata e mais honesta com o tamanho do projeto.

---

## C) Viabilidade e legalidade da coleta

Há diferença entre **ver** e **coletar automaticamente em escala**:

| | O que é | Situação |
|---|---|---|
| **Visível** | Um humano vê views/likes/comentários num post público | ✅ Sempre |
| **Permitido** | Coletar de forma **automatizada** é autorizado? | ⚠️ Cinza. Os ToS (Meta à frente) proíbem coleta automatizada. A Justiça dos EUA deu ganho a scrapers de dado público *deslogado* (Meta v. Bright Data, 2024), mas a Meta bloqueia/processa quando quer, e a **LGPD** vale mesmo pra dado "público" |
| **Viável em escala** | Um robô pega confiável, milhares de vezes? | ❌ Difícil sem API (login wall, captcha, fingerprint, rate limit, HTML que muda) |

**A favor:** o criador é parceiro e **quer receber** → coopera **conectando a conta** (OAuth) → acesso à **API oficial**, de forma legal e confiável. Isso praticamente elimina o problema de scraping.

---

## D) Referência de API por plataforma (2026)

| Rede | Oficial? | Como | Pegadinha |
|---|---|---|---|
| **TikTok** | Sim — view/like/comentário/share | **Display API** + conta conectada (Login Kit) | Só vídeos **da própria conta** autenticada |
| **Instagram** | Sim — Reels: plays/alcance/likes/comentários | **Graph API** + conta **Profissional/Creator** | Conta pessoal não tem API (Basic Display morreu 04/12/2024); **nosso app** aprovado pela Meta 1x (~4–6 semanas); "views" de vídeo comum descontinuado jan/2025 |
| **Facebook** | Sim — posts de Páginas | **Graph API** | Precisa ser **Página**, não perfil |
| **YouTube** | Sim — **o mais fácil** | **Data API v3** + API key: `viewCount`/`likeCount`/`commentCount` de qualquer vídeo público | Likes podem estar ocultos se o criador desativou |

**YouTube em detalhe (ponto de partida ideal):** `videos.list` com **API key** (sem OAuth, sem app review). Cada chamada custa **1 unidade** e aceita **50 vídeos/chamada**; cota grátis de **10.000/dia** → ~**500 mil vídeos/dia**. Shorts entram normalmente; desde **31/03/2025** contam view como TikTok/Reels (comparação mais justa).

> **"views" é a métrica mais instável** entre as redes → a fórmula (conceito §8) usa o que cada rede entrega de forma confiável (*normalização por plataforma*).

---

## E) Conexão de conta — o fluxo OAuth (token)

Pra **TikTok e Instagram**, o criador **conecta a conta** e volta um **token**:

1. Clica em **"Conectar conta"** → 2. loga **na página da própria rede** (nunca vemos a senha) → 3. autoriza a permissão ("ver vídeos e estatísticas") → 4. a rede devolve um **código**, que o servidor troca por **access token** + **refresh token** → 5. lemos as estatísticas dos vídeos **daquela conta** → 6. o **refresh token** renova sozinho, pra medir o vídeo por dias sem novo login.

- O token só acessa a **própria conta** do criador — é tudo o que precisamos.
- **"Link do vídeo"** (qual vídeo/campanha) e **"conectar conta"** (libera os dados) são complementares.
- Fallback do MVP: **link + print + conferência manual**.

**Instagram — dois "aprovar" que se confundem:**

| O quê | Quem faz | Quanto demora |
|---|---|---|
| Deixar a conta **Profissional/Creator** | O **criador**, nas configs do Insta | **Instantâneo** e grátis |
| **Revisão do nosso app** pela Meta | **Nós** (uma vez, antes de lançar) | ~**4 a 6 semanas** |

*(O YouTube não tem nada disso — só API key.)* Detalhes de App Review / CNPJ / MEI → conceito §18.

---

## F) Arquitetura do sistema

```
┌────────────────────────────────────────────────────────────┐
│                     PLATAFORMA ECOA                         │
│                                                             │
│  [Painel Gravadora]   [App/Painel Criador]  [Painel Admin]  │
│         │                    │                    │         │
│         └──────────┬─────────┴──────────┬─────────┘         │
│                    │                     │                  │
│              [API / Backend]      [Fila de Verificação      │
│                    │               manual / KYC]            │
│        ┌───────────┼───────────┐                            │
│        │           │           │                            │
│  [Coletor de   [Motor de   [Módulo de                       │
│   Métricas]    Pontos/     Pagamento                        │
│   (worker+     Pagamento]  (Pix)]                           │
│    cron+fila)     │                                         │
│        │          │                                         │
│        └──────> [Banco de Dados] <──────┘                   │
└────────────────────────────────────────────────────────────┘
        │                              │
   Redes sociais                  Provedor Pix
 (YT/TikTok/Insta/FB)            (PSP/banco)
```

**Componentes:**
1. **Painéis** (gravadora, criador, admin) — web.
2. **Backend/API** — regras de negócio, campanhas, autenticação.
3. **Coletor de métricas** — worker + cron + fila (parte A); busca via API oficial → link → scraping.
4. **Motor de pontos e pagamento** — aplica a fórmula pública (conceito §8).
5. **Fila de verificação / KYC** — aprovação de identidade (parte B).
6. **Módulo de pagamento** — integração Pix.

---

## G) Estratégia de fontes e cadência de coleta

**Camadas de fonte (da melhor pra pior):**
1. **Conta conectada via API oficial** — legal, confiável, automatizável (TikTok Display API, Instagram/Facebook Graph API, YouTube Data API).
2. **Link + comprovante + conferência** — o criador manda o link/print; a equipe confere. **Ideal pro MVP.**
3. **Leitura pública pontual** (oEmbed / página do link) — funciona às vezes; frágil e sujeito a bloqueio.
4. **Scraping "raiz"** — último recurso; evitar na Meta; **revisão jurídica obrigatória**.

**Recomendação:** MVP na **camada 2**; escala na **camada 1**; scraping só como tapa-buraco.

**Cadência (polling adaptativo):** coletar com mais frequência logo após a postagem (métricas crescem rápido no início) e ir espaçando até o fim da janela da campanha (ex.: diário até 14 dias); depois **congela**. Pagar só após a **estabilização** (ver parte B) — herda de graça a limpeza de fraude das redes.

---

## Referências técnicas

- **TikTok:** [Display API](https://developers.tiktok.com/doc/display-api-overview) · [Escopos](https://developers.tiktok.com/doc/tiktok-api-scopes) · [Listagem de vídeos](https://developers.tiktok.com/doc/tiktok-api-v1-video-list)
- **Instagram/Meta:** [Media Insights](https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/insights/) · [Platform (dev mode / access)](https://developers.facebook.com/docs/instagram-platform/overview/) · [App Review](https://developers.facebook.com/docs/instagram-platform/app-review/)
- **YouTube:** [Data API (API key)](https://developers.google.com/youtube/v3/getting-started) · [Custo de cota](https://developers.google.com/youtube/v3/determine_quota_cost) · [Recurso `videos`](https://developers.google.com/youtube/v3/docs/videos) · [Revisões](https://developers.google.com/youtube/v3/revision_history)
- **Legalidade:** [Meta — Automated Data Collection Terms](https://www.facebook.com/legal/automated_data_collection_terms) · [Meta v. Bright Data (2024)](https://www.zyte.com/blog/california-court-meta-ruling/)