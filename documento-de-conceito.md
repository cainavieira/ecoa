# Documento de Conceito — Projeto "Ecoa"


**Versão:** 0.1 — rascunho inicial
**Data:** 18/07/2026
**Status:** Conceito para discussão

---

## 1. Resumo executivo

Uma plataforma **independente das redes sociais** que conecta **gravadoras/artistas** que querem promover músicas a **criadores de conteúdo** que produzem vídeos usando essas músicas — **em qualquer rede** (YouTube, TikTok, Instagram, Facebook, etc.).

> [!WARNING] É INTERESSANTE INDIVIDUALIZAR AS REDES SOCIAIS QUE QUEREMOS NESSE MCP E DEPOIS AUMENTAR O ESCOPO CONFORME O PRODUTO FOR CRESCENDO.

O criador posta o vídeo na rede que quiser, a plataforma **coleta as métricas públicas** (views, likes, comentários) desse vídeo, e o criador é **pago por performance** seguindo uma **fórmula pública e transparente**. A verba dos pagamentos vem da **gravadora**, que financia campanhas por música. A plataforma faz a intermediação, a verificação (humana) das contas e o cálculo dos pagamentos.

É, na essência, o **"Trabalhe com Artistas" do TikTok — só que multiplataforma, transparente e operado por fora das redes.**

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 2. O problema

**Para a gravadora / artista:**
- O "Trabalhe com Artistas" do TikTok **só funciona dentro do TikTok**. A música não ganha tração organizada no Instagram Reels, no Facebook, no Kwai, etc.
- Contratar influenciadores um a um é **caro, lento e opaco** — difícil pagar de forma justa dezenas ou centenas de micro-criadores.
- Falta uma forma **medível e auditável** de dizer "paguei X e gerou Y visualizações reais".
- Difícil de fazer promoções customizadas que a gravadora/cliente gostaria de fazer de modo agnóstico às plataformas, como por exemplo com gamificações, até mesmo pensando em prêmios que não envolvam apenas dinheiro.

**Para o criador de conteúdo:**
- Quem cria fora do TikTok (ou também no Insta/Facebook) **não tem como monetizar** esse esforço de divulgação musical de forma estruturada.
- Programas das próprias redes são, muitas vezes, **caixa-preta**: não se sabe exatamente por que se ganhou tanto.

**A oportunidade:** um lugar neutro, entre as redes, que organiza essa relação de forma **transparente, multiplataforma e à prova de bot**, dando mais controle ao cliente e mais auditoria ao criador de conteúdo.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 3. A solução (visão)

Uma plataforma web onde:

1. A **gravadora** cria campanhas por música, com verba e regras.
2. O **criador** se cadastra, tem a conta **verificada por uma pessoa** (anti-bot) e escolhe campanhas.
3. O criador **posta o vídeo na rede que preferir** e cadastra o link na plataforma.
4. A plataforma **coleta as métricas públicas** do vídeo periodicamente.
5. O criador é **pago por performance**, segundo uma **métrica pública**.
6. A gravadora acompanha, em um painel, **quanto investiu e qual retorno** (alcance) teve.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 4. Atores

| Ator | O que quer | O que faz na plataforma |
|---|---|---|
| **Gravadora** (cliente-âncora / paga) | Espalhar a música e medir o retorno | Cria campanhas, define verba e regras, acompanha resultados |
| **Artista** | Viralizar a faixa | (Via gravadora) tem a música promovida |
| **Criador de conteúdo** | Ganhar por divulgar | Cadastra-se, é verificado, posta vídeos, envia links, recebe pagamento |
| **Equipe de verificação** (nós) | Manter a base limpa | Aprova/reprova contas manualmente, audita fraude, opera pagamentos |
| **Admin da plataforma** (nós) | Operar o negócio | Configura campanhas, fórmula, comissão, relatórios |

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 5. Como funciona — o fluxo completo

```
GRAVADORA                PLATAFORMA                    CRIADOR
   │                        │                             │
   │ 1. cria campanha       │                             │
   │   (música + verba +    │                             │
   │    regras)             │                             │
   ├───────────────────────>│                             │
   │                        │ 2. campanha fica visível    │
   │                        │────────────────────────────>│
   │                        │                             │ 3. cadastra conta
   │                        │  4. VERIFICAÇÃO MANUAL       │    (rede social)
   │                        │<────────────────────────────┤
   │                        │  (humano confirma que é      │
   │                        │   pessoa real, não bot)      │
   │                        │─── aprovado ───────────────>│
   │                        │                             │ 5. posta vídeo
   │                        │                             │   na rede dele
   │                        │  6. cadastra o link do post │
   │                        │<────────────────────────────┤
   │                        │ 7. COLETA MÉTRICAS PÚBLICAS  │
   │                        │  (views/likes/comentários,   │
   │                        │   periodicamente)            │
   │                        │ 8. calcula pontos e pagamento│
   │                        │   pela fórmula pública       │
   │ 9. relatório de ROI    │                             │
   │<───────────────────────┤ 10. paga o criador (Pix)    │
   │                        │────────────────────────────>│
```
> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.
---

## 6. Diferenciais vs. "Trabalhe com Artistas" do TikTok

| | TikTok WWA | Projeto Ecoa |
|---|---|---|
| Plataformas | Só TikTok | **Multiplataforma** (YouTube, TikTok, Instagram, Facebook…) |
| Métrica de pagamento | Caixa-preta | **Fórmula pública e auditável** |
| Verificação | Automática (da rede) | **Humana + anti-bot** |
| Quem opera | TikTok | **Neutro**, entre as redes |
| Foco | Genérico | Sob medida pra **gravadoras** |

> O grande trunfo é o **"multiplataforma"**: a mesma música rende campanha no TikTok **e** no Reels **e** no Facebook, com um único orçamento e um único padrão de medição.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 7. Modelo de negócio

**Fonte da verba:** a **gravadora** financia cada campanha (cliente-âncora já definido).

**Como a plataforma ganha** (opções, podem ser combinadas):
- **Comissão sobre a campanha** — ex.: a gravadora deposita R$ 10.000; a plataforma retém uma taxa (ex.: 15–25%) e o restante vira pool de pagamento aos criadores.

> [!WARNING] É INTERESSANTE ENTENDER MELHOR EU COMO PLATAFORMA COMO ESSA PRECIFICAÇÃO DO SERVIÇO FUNCIONARIA OU O QUE EU VENDERIA SE É A PLATAFORMA OU A PLATAFORMA + MANUTENÇÃO.
- **Taxa de serviço fixa / mensalidade** por gravadora (modelo SaaS) — previsível.
- **Taxa por criador pago** — pequena taxa operacional por repasse.

**Recomendação para a v1:** **comissão sobre a verba da campanha** (simples de explicar e alinha o incentivo — só ganhamos se rodarmos campanha). Mensalidade entra quando houver mais de uma gravadora.

---

## 8. A métrica pública de pagamento *(o coração do projeto)*

O diferencial é ser **transparente**. Proposta de fórmula única e publicada:

### 8.1. Ponto de Performance (PP)
Cada vídeo acumula pontos combinando as métricas, com **pesos públicos**:

```
PP = (views × Pv) + (likes × Pl) + (comentários × Pc)
```

Exemplo de pesos (ajustáveis, mas sempre publicados):
- `Pv = 1` (view)
- `Pl = 8` (like — mais difícil que uma view)
- `Pc = 20` (comentário — engajamento mais raro/valioso)

### 8.2. Normalização por plataforma
Uma view não vale o mesmo em toda rede. Aplica-se um **fator de plataforma público** `Fp`:

```
PP_ajustado = PP × Fp
```
(ex.: TikTok `1,0`; Reels `1,1`; YouTube Shorts `1,0`; Facebook `0,8` — calibrados com dados reais.)

### 8.3. Como o dinheiro é dividido — 3 modelos

| Modelo | Como funciona | Prós | Contras |
|---|---|---|---|
| **A. Taxa fixa por performance** *(preferido)* | `R$ X por 1.000 pontos` — o criador sabe **de antemão** quanto ganha por view/like/comentário | **Transparente e fácil de anunciar** ("ganhe R$ X por mil views"); criador sabe o valor exato; a gravadora só paga por resultado real | Custo total fica em aberto → resolvido com **tetos** (abaixo) |
| **B. Rateio do pool** *(modo "competição", opcional)* | `Pagamento = (pontos do criador ÷ pontos totais) × verba` | Verba 100% usada; todos disputam o mesmo bolo | Valor final só é conhecido no fim |
| **C. Ranking estilo WWA** *(modo "competição", opcional)* | Top N ganham prêmios fixos | Bem gamificado | Excludente; poucos ganham |

**Recomendação:** **Modelo A — taxa fixa por performance.** É o que faz sentido pro projeto: mostrar ao criador **quanto ele ganha por view**, não "quanto eu tenho pra dividir". Pra proteger o orçamento (que numa taxa fixa fica, em tese, ilimitado), usam-se **dois tetos**:
- **Teto de verba por campanha:** a campanha acumula pagamentos até o limite; ao atingir, encerra. A gravadora **nunca gasta mais que o combinado**.
- **Teto por vídeo / por criador:** limite do quanto um único vídeo ou criador pode ganhar (ex.: R$ 500/vídeo), pra um viral não "comer" a verba toda e manter o jogo justo.

> Resultado: o criador vê **"ganhe R$ X por mil views, até R$ Y por vídeo"** (previsível) e a gravadora vê um **custo com teto** (também previsível). Os modelos B e C ficam guardados como **modos "competição"** opcionais, **desligados por padrão**.

> > [!WARNING] Conversar com Mateus para entender melhor o modelo competição que ele deseja implementar.

### 8.4. Regras de qualificação (anti-gaming)
- Post **público** por no mínimo *N* dias.
- Conta **verificada**.
- Uso correto da **música/áudio oficial** (e hashtag da campanha, se houver).
- **Janela de medição** definida (ex.: contam métricas até 14 dias após a publicação).
- Métricas só "fecham" após **período de estabilização** (evita views infladas que somem).

> [!WARNING] Conversar com Mateus para entender melhor o modelo as regras do modelo de competição que ele quer implementar
---

## 9. Coleta de métricas — a parte mais importante ⭐

> Tem uma diferença entre **ver** e **coletar automaticamente em escala** as métricas de redes sociais.

### 9.1. Como pegamos os dados, de forma legal

A plataforma coleta as métricas **públicas** de forma **oficial**. O pulo do gato: o **criador é parceiro e quer receber**, então ele **conecta a própria conta** (login oficial / OAuth) e a gente lê os números pela **API oficial** da rede — sem depender de scraping. *(Análise de viabilidade e legalidade → `documento-tecnico.md` §C.)*

### 9.2. Por plataforma (resumo)

Dá pra pegar as métricas **oficialmente** em todas as redes-alvo: **YouTube** é o mais fácil (API pública, sem conta conectada); **TikTok** e **Instagram** exigem o criador conectar a conta. Detalhes de API, escopos e cotas → **`documento-tecnico.md` §D**.

> **"views" é a métrica mais instável** entre as redes → a fórmula (seção 8) usa o que cada rede entrega de forma confiável (*normalização por plataforma*).

### 9.3. Estratégia de coleta

No **MVP**, começa simples: o criador **manda o link** e a equipe confere. Na **escala**, migra pra **conta conectada** (API oficial), automatizável. *(Camadas de fonte e cadência técnica → `documento-tecnico.md` §G.)*

### 9.4. Por que o YouTube é o melhor ponto de partida ▶️

O **YouTube é a única rede onde o criador não precisa conectar nada** — dados públicos via API, coleta automática e de **baixo risco**. Por isso é o **ponto de partida**: prova o loop inteiro antes de ligar TikTok e Instagram (que dependem de conta conectada). *(Detalhes de cota e API → `documento-tecnico.md` §D.)*

### 9.5. Conexão de conta (OAuth)

Pra TikTok e Instagram, a coleta depende de o criador **conectar a conta**, e o acesso volta como um **token**. O passo a passo do fluxo OAuth e os dois "aprovar" do Instagram → **`documento-tecnico.md` §E**.

---

## 10. Verificação e antifraude

As redes já filtram bot; como a gente injeta **dinheiro por view**, mantemos uma camada **leve e focada**: **verificação de identidade / KYC** (saber quem receber; uma pessoa = uma conta) e **pagar só sobre métrica já estável** (herda a limpeza das redes), com **tetos** e **auditoria por amostragem** no topo. *(Análise completa e mecânica → `documento-tecnico.md` §B.)*

---

## 11. Pagamentos (Brasil)

- **Pix como trilho principal** — barato, instantâneo, universal no Brasil. Ideal pra pagar muitos criadores.
- **Limite mínimo de saque** (ex.: R$ 50) pra reduzir custo operacional.
- **Fiscal/jurídico:** contrato/termos claros com o criador; tratamento de imposto conforme enquadramento; emissão de comprovantes. *(Definir com contador.)*
- **LGPD:** a plataforma guarda dados pessoais (CPF, contas sociais) — precisa de política de privacidade e base legal.

---

## 12. Arquitetura em alto nível

O diagrama de componentes (painéis, backend, coletor de métricas, motor de pontos, fila de verificação/KYC, módulo Pix) e o desenho do sistema → **`documento-tecnico.md` §F**.

---

## 13. Riscos e mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| Scraping viola Termos de Uso das redes | Alto | Priorizar APIs oficiais; revisão jurídica; conta conectada pelo criador |
| Fraude de engajamento (views compradas) | Alto | Retenção + estabilização; detecção de anomalias; auditoria manual |
| APIs das redes mudam/quebram | Médio | Arquitetura com fallback; múltiplas fontes |
| Dependência de uma única gravadora | Médio | Usar a v1 pra provar o modelo e captar outras labels |
| Métrica "gameada" pelos criadores | Médio | Pesos e janelas bem calibrados; regras de qualificação |
| Jurídico/fiscal do pagamento em massa | Alto | Contador + advogado desde o MVP; contrato com criador |
| LGPD (dados pessoais) | Médio | Política de privacidade; minimizar dados; consentimento |

---

## 14. MVP — escopo da v1 ("concierge")

A ideia é **provar o loop** com o mínimo de automação, aproveitando a gravadora âncora:

- **1 gravadora, 1–2 artistas.**
- **3 plataformas no lançamento:** **YouTube Shorts** (coleta mais fácil, menor risco) + TikTok + Instagram Reels — as mais fortes pra música. Começar a automação **pelo YouTube**, que é o único sem necessidade de conta conectada. *(Confirmar.)*
- **Verificação manual** das contas (já é o plano).
- **Coleta semi-manual:** criador envia o link; equipe/robô simples checa métricas periodicamente.
- **Pagamento via Pix**, operado manualmente no começo.
- **Painel simples:** lista de campanhas, cadastro + fila de verificação, envio de links, acompanhamento de métricas, cálculo de pagamento.

> Filosofia: fazer **manualmente o que ainda não vale a pena automatizar**. Se o loop funciona no braço, aí investimos em automação.

---

## 15. Roadmap

- **Fase 0 — Validação:** fechar com a gravadora as regras (verba, fórmula, plataformas). Rodar 1 campanha piloto quase toda manual.
- **Fase 1 — MVP concierge:** painel simples + verificação manual + Pix manual. Uma gravadora.
- **Fase 2 — Automação:** coletor de métricas automático, Pix automático, mais artistas.

---

## 16. Perguntas em aberto (decisões pendentes)

1. **Nome** da plataforma (ver sugestões abaixo).
2. **Quais plataformas** entram no lançamento? (TikTok + Reels é a aposta.)
3. **Verba típica** de uma campanha e **comissão** da plataforma.
4. **Qual fórmula** de pagamento adotar.
5. **Marca:** a plataforma tem marca própria ou aparece como ferramenta da gravadora?
6. **KPIs de sucesso** do piloto (ex.: nº de criadores, custo por 1.000 views, alcance total).

---

## 18. Aprovação e formalização (a parte "burocrática") 🗂️

### 18.1. Dois níveis de acesso (Meta)

| Nível | Pra que serve | Precisa de App Review? |
|---|---|---|
| **Standard Access / modo desenvolvimento** | Contas que **têm papel no app** — você (admin) e amigos adicionados como **testadores** | ❌ Não |
| **Advanced Access / produção** | Atender **qualquer criador** (contas que você não controla) | ✅ Sim |

> **Importante:** a fronteira **não é volume** ("poucas contas eles não ligam"). É **por design**: sem aprovação, a API **só funciona pras contas testadoras que você cadastrou** — um estranho simplesmente **não conecta**, seja 1 ou 1.000. Não existe "zona cinza".

### 18.3. Precisa de CNPJ?
- **Pra desenvolver/testar:** ❌ **Não** — nada de CNPJ.
- **Pra ir ao ar com o público:** ✅ **Sim**, a Meta exige **Verificação de Negócio**.

### 18.4. O portão de cada plataforma

| Rede | Precisa aprovar o app pra produção? | Precisa CNPJ/MEI? |
|---|---|---|
| **YouTube** | ❌ Não (dados públicos via API key) | ❌ Não |
| **TikTok** | ✅ Sim (revisão do app pra sair do sandbox) | A confirmar |
| **Instagram/Facebook (Meta)** | ✅ Sim (App Review → Advanced Access) | ✅ Sim (MEI serve) |

### 18.5. Checklist por fase
- **Fase de teste** *(sem custo, sem burocracia)*: construir o sistema, testar com você + amigos como **testadores**, e validar o loop inteiro **no YouTube** (público, sem aprovação).
- **Preparar a produção** *(começar cedo — demora semanas)*: e iniciar a **Verificação de Negócio** e a **App Review** de Meta e TikTok.
- **Escalar**: com tudo aprovado, abrir pra criadores do público em geral.

> **Regra de ouro:** dá pra ir **longe sem gastar nada** (dev + YouTube). Só formalize e peça as aprovações quando for **escalar de verdade** — mas **inicie os pedidos com semanas de antecedência**, porque a análise demora.

---

## 19. Referências e links úteis 📚

Fontes usadas na elaboração deste documento, agrupadas por tema pra facilitar a consulta.

### Conceito de origem (TikTok "Trabalhe com Artistas")
- [TikTok Creator Academy — Using Work With Artists](https://www.tiktok.com/creator-academy/article/introducing-work-with-artist) — como o programa oficial funciona.
- [TikTok Newsroom (PT-BR) — TikTok para Artistas](https://newsroom.tiktok.com/pt-br/tiktok-para-artistas) — visão da própria TikTok.
- [Digital Music News — Work With Artists](https://www.digitalmusicnews.com/2023/05/09/tiktok-work-with-artists-feature-how-it-works/) — explicação da mecânica e dos pagamentos.

### Coleta de dados — TikTok
- [TikTok — Display API (visão geral)](https://developers.tiktok.com/doc/display-api-overview) — API pra ler dados da conta conectada.
- [TikTok — Escopos da API](https://developers.tiktok.com/doc/tiktok-api-scopes) — permissões (ex.: `user.info.basic`, `video.list`).
- [TikTok — Endpoint de listagem de vídeos](https://developers.tiktok.com/doc/tiktok-api-v1-video-list) — campos como `view_count`, `like_count`, `comment_count`.

### Coleta de dados — Instagram / Meta
- [Meta — Instagram Media Insights](https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/insights/) — métricas oficiais disponíveis.
- [Meta for Developers — Instagram Platform (visão geral)](https://developers.facebook.com/docs/instagram-platform/overview/) — modo desenvolvimento, Standard vs Advanced Access.
- [Meta — Instagram Platform App Review](https://developers.facebook.com/docs/instagram-platform/app-review/) — como conseguir o Advanced Access.

### Coleta de dados — YouTube
- [YouTube Data API — visão geral / API key](https://developers.google.com/youtube/v3/getting-started) — acesso a dados públicos só com uma chave.
- [YouTube Data API — custo de cota](https://developers.google.com/youtube/v3/determine_quota_cost) — `videos.list` = 1 unidade.
- [YouTube Data API — recurso `videos`](https://developers.google.com/youtube/v3/docs/videos) — campos de `statistics` (view/like/comment).
- [YouTube Data API — histórico de revisões](https://developers.google.com/youtube/v3/revision_history) — mudança na contagem de views de Shorts (2025).

### Legalidade de scraping
- [Meta — Automated Data Collection Terms](https://www.facebook.com/legal/automated_data_collection_terms) — o que os Termos de Uso proíbem.
- [Zyte — decisão Meta v. Bright Data (2024)](https://www.zyte.com/blog/california-court-meta-ruling/) — análise sobre scraping de dado público deslogado.

### Aprovação e formalização (Meta)
- [Meta — Sobre a verificação da empresa](https://pt-br.facebook.com/business/help/1095661473946872) — o que é a Verificação de Negócio.
- [Documentos aceitos na verificação (inclui CNPJ e MEI)](https://docs.360dialog.com/docs/resources/meta-business-verification) — lista de documentos aceitos.

---

*Próximos passos sugeridos: a partir deste documento, evoluir para (a) uma **apresentação/pitch** para a gravadora e (b) um **diagrama de sistema** mais detalhado do fluxo técnico.*
