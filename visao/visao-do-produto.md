# Visão do produto: Ecoa

**Versão:** 0.2
**Data:** 10/10/2026
**Status:** visão completa, para aprovação

> Este documento descreve a ideia inteira. O que se constrói agora é o MVP
> (§14), descrito em detalhe na `especificacao/`. Pesquisa de APIs, legalidade
> e aprovação nas redes: `pesquisa-das-redes.md`.

---

## 1. Resumo executivo

Uma plataforma **independente das redes sociais** que conecta **gravadoras e
artistas** que querem promover músicas a **criadores de conteúdo** que produzem
vídeos com essas músicas, **em qualquer rede** (TikTok, YouTube, Instagram,
Facebook).

O criador posta o vídeo na rede que quiser e cadastra o link. A plataforma
acompanha as **métricas públicas** do vídeo, e o criador é **premiado por
performance**, segundo **regras públicas e transparentes**. O prêmio pode ser
dinheiro ou produto, e quem paga é a **gravadora**, direto ao criador. A
plataforma faz a intermediação, a verificação humana das contas e a
transparência dos resultados.

É, na essência, o **"Trabalhe com Artistas" do TikTok, só que multiplataforma,
transparente e operado por fora das redes.**

A plataforma começa pelo TikTok (§14) e ganha uma rede por vez, conforme o
produto cresce.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 2. O problema

**Para a gravadora e o artista:**
- O "Trabalhe com Artistas" do TikTok **só funciona dentro do TikTok**. A
  música não ganha tração organizada no Instagram Reels, no Facebook, no Kwai.
- Contratar influenciadores um a um é **caro, lento e opaco**: é difícil
  premiar de forma justa dezenas ou centenas de micro-criadores.
- Falta uma forma **medível e auditável** de dizer "investi X e gerou Y
  visualizações reais".
- É difícil fazer promoções sob medida, independentes de plataforma, com
  gamificação e prêmios que não sejam só dinheiro.

**Para o criador de conteúdo:**
- Quem cria fora do TikTok, ou também no Instagram e no Facebook, **não tem
  como monetizar** esse esforço de divulgação musical de forma estruturada.
- Os programas das próprias redes são, muitas vezes, **caixa-preta**: não se
  sabe exatamente por que se ganhou tanto.

**A oportunidade:** um lugar neutro, entre as redes, que organiza essa relação
de forma **transparente, multiplataforma e à prova de bot**, dando mais
controle à gravadora e mais auditoria ao criador.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 3. A solução

Uma plataforma web onde:

1. A **gravadora** define campanhas por música, com regulamento e prêmios.
2. O **criador** entra, tem a conta **verificada por uma pessoa** (anti-bot) e
   participa das campanhas.
3. O criador **posta o vídeo na rede que preferir** e cadastra o link na
   plataforma.
4. A plataforma **acompanha as métricas públicas** do vídeo ao longo da
   campanha.
5. Os resultados ficam **visíveis aos participantes**: ranking e ganhadores.
6. A **gravadora premia** os ganhadores direto e acompanha, num painel, o
   alcance que a campanha gerou.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 4. Atores

| Ator | O que quer | O que faz na plataforma |
|---|---|---|
| **Gravadora** | Espalhar a música e medir o retorno | Define campanhas e prêmios, acompanha resultados, paga os ganhadores |
| **Artista** | Viralizar a faixa | Tem a música promovida, pela gravadora |
| **Criador de conteúdo** | Ganhar por divulgar | Entra, é verificado, posta vídeos, envia links, recebe prêmio |
| **Operação** (nós) | Manter a base limpa e o jogo justo | Verifica contas, cria campanhas, confere vídeos e números, registra ganhadores |

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 5. Como funciona

    GRAVADORA                 PLATAFORMA                     CRIADOR
        |                         |                              |
        | 1. define a campanha    |                              |
        |   (música, regulamento, |                              |
        |    prêmios, datas)      |                              |
        |------------------------>|                              |
        |                         | 2. campanha fica visível     |
        |                         |----------------------------->|
        |                         |                              | 3. entra na plataforma
        |                         | 4. VERIFICAÇÃO MANUAL        |
        |                         |<-----------------------------|
        |                         |   (uma pessoa confirma que   |
        |                         |    a conta é real e dele)    |
        |                         |---------- aprovado --------->|
        |                         |                              | 5. posta o vídeo
        |                         |                              |    na rede dele
        |                         | 6. cadastra o link do vídeo  |
        |                         |<-----------------------------|
        |                         | 7. ACOMPANHA AS MÉTRICAS     |
        |                         |    PÚBLICAS até o fechamento |
        |                         | 8. publica ranking e         |
        |                         |    ganhadores                |
        | 9. painel e relatório   |----------------------------->|
        |<------------------------|                              |
        | 10. paga o prêmio direto ao criador                    |
        |------------------------------------------------------->|

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 6. Diferenciais diante do "Trabalhe com Artistas" do TikTok

| | TikTok WWA | Ecoa |
|---|---|---|
| Plataformas | Só TikTok | **Multiplataforma** (TikTok, YouTube, Instagram, Facebook) |
| Regra de premiação | Caixa-preta | **Regras públicas e auditáveis** |
| Verificação | Automática, da rede | **Humana e anti-bot** |
| Quem opera | TikTok | **Neutro**, entre as redes |
| Foco | Genérico | Sob medida para **gravadoras** |

> O grande trunfo é ser **multiplataforma**: a mesma música rende campanha no
> TikTok, no Reels e no Facebook, com um único regulamento e um único padrão
> de medição.

> [!NOTE] Se tudo estiver bom, abaixo desse TIP Mateus deverá dar o aceite.

---

## 7. Modelo de negócio

- **Quem paga os prêmios:** a gravadora, direto ao criador, em dinheiro (Pix)
  ou produto. A plataforma não movimenta o dinheiro dos prêmios.
- **Como a plataforma é remunerada:** acordo entre os sócios, fora deste
  documento.

---

## 8. Regras de premiação (o coração do projeto)

O diferencial é a **transparência**: o regulamento de cada campanha é público
para os participantes, e os números que decidem o resultado também.

### 8.1 O que se mede

- **No MVP:** views, o número que se anuncia ao criador.
- **Na visão completa:** curtidas e comentários entram com **pesos públicos**,
  e cada rede ganha um **fator de plataforma público**, porque uma view não
  vale o mesmo em toda rede:

      Pontos = (views × Pv + curtidas × Pl + comentários × Pc) × Fp

  Exemplo de pesos, sempre publicados: `Pv = 1`, `Pl = 8`, `Pc = 20`; fator
  TikTok `1,0`, Reels `1,1`, YouTube Shorts `1,0`, Facebook `0,8`, calibrados
  com dados reais.

### 8.2 Formas de premiar

A gravadora escolhe a forma em cada campanha, e o prêmio pode ser dinheiro ou
produto:

| Forma | Exemplo | Prós | Contras |
|---|---|---|---|
| **Taxa por performance** | "R$ X por mil views, até R$ Y por vídeo" | o criador sabe de antemão quanto ganha | custo total em aberto, resolvido com teto por campanha e por vídeo |
| **Meta de views** | "iPhone para quem passar de 100 mil views" | serve para produto e é fácil de anunciar | não se sabe antes quantos vão bater a meta |
| **Rateio** | "R$ 1.000 divididos pelas views de cada um" | a verba é usada inteira | o valor final só se sabe no fim |
| **Ranking** | "os 3 primeiros ganham" | bem gamificado | poucos ganham |

No MVP, o sistema não calcula prêmio: mostra os números e o ranking, e a
operação registra os ganhadores conforme o regulamento. Assim, qualquer uma das
formas cabe no piloto sem mudar o sistema.

### 8.3 Regras de qualificação (anti-gaming)

- Conta **verificada**.
- Vídeo **público**, publicado dentro do prazo da campanha.
- Uso do **som da campanha** (e da hashtag, se o regulamento pedir).
- **Datas da campanha:** início, a partir de quando vale publicar; fim, último
  dia para publicar; fechamento, quando os números congelam.
- **Estabilização:** o intervalo entre o fim e o fechamento deixa as redes
  removerem views falsas antes de o resultado sair.

---

## 9. Coleta de métricas, a parte mais importante

Há diferença entre **ver** um número e **coletar automaticamente em escala**.

### 9.1 No MVP: leitura manual

A operação abre o vídeo público e anota o número de views, com a data. Ninguém
usa API nem robô, e por isso não há termo de desenvolvedor a cumprir nem risco
de scraping. Funciona porque o piloto é pequeno (§14).

### 9.2 Na escala: conta conectada

Para automatizar, o criador **conecta a própria conta** (login oficial) e a
plataforma lê os números pela **API oficial** da rede. O criador é parceiro e
quer ganhar, então coopera. Antes de ligar isso no TikTok, os termos do
desenvolvedor dele precisam de leitura jurídica: eles restringem uso comercial,
registro sobre pessoas e repasse de dados a terceiros. Detalhes por rede em
`pesquisa-das-redes.md`.

### 9.3 Por que começar pelo TikTok, com leitura manual

O TikTok é onde a música ganha tração e é a rede que a gravadora quer
primeiro. A leitura manual tira do caminho do lançamento a aprovação do app
pelo TikTok, que não tem prazo, e as dúvidas sobre os termos. Para automatizar
depois, o YouTube é a rede mais simples: ele entrega dados públicos sem o
criador conectar nada.

---

## 10. Verificação e antifraude

As redes já filtram bot. Como a plataforma premia por view, ela mantém uma
camada **leve e focada**:

- **Verificação da conta:** o criador prova que o @ é dele pondo um código na
  bio, e uma pessoa olha a conta para confirmar que é real.
- **Resultado só depois da estabilização:** a plataforma herda a limpeza que as
  redes fazem nas views falsas.
- **Conferência dos ganhadores** pela operação, antes de registrá-los.

O raciocínio completo está em `pesquisa-das-redes.md`.

---

## 11. Prêmios e dados pessoais

- A gravadora paga o prêmio direto ao ganhador: Pix, se for dinheiro; entrega,
  se for produto.
- A plataforma guarda o mínimo: nome e e-mail da conta Google e o @ da rede.
  CPF, chave Pix e endereço, a gravadora pede ao ganhador na hora da entrega.
- **Fiscal e jurídico:** termos claros com o criador e tratamento fiscal do
  prêmio. (Definir com contador.)
- **LGPD:** política de privacidade e base legal. O ranking aberto entre
  participantes depende do aceite do criador.

---

## 12. Arquitetura

A arquitetura do que se constrói fica na especificação (`especificacao/`).

---

## 13. Riscos e mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| Termos do desenvolvedor do TikTok restringem o uso da API em campanha paga | Alto | MVP com leitura manual; leitura jurídica antes da Fase 2 |
| Leitura manual não escala | Médio | Piloto com teto de criadores; API na Fase 2 |
| Scraping viola os termos de uso das redes | Alto | Não fazer scraping: leitura humana no MVP, API oficial na escala |
| Fraude de engajamento (views compradas) | Alto | Estabilização antes do fechamento; verificação humana; conferência dos ganhadores |
| APIs das redes mudam ou quebram | Médio | Leitura manual como alternativa |
| Dependência de uma única gravadora | Médio | Usar o piloto para provar o modelo e captar outras |
| Regras "gameadas" pelos criadores | Médio | Regulamento claro e regras de qualificação (§8.3) |
| Fiscal e jurídico dos prêmios | Médio | Contador e advogado desde o piloto; termos com o criador |
| LGPD (dados pessoais) | Médio | Dados mínimos, sem CPF; política de privacidade; aceite do ranking |

---

## 14. MVP: o piloto no TikTok

A ideia é **provar o loop** com o mínimo de automação.

- **Só TikTok.** Uma gravadora, uma campanha por vez, até 50 criadores.
- **Entrada com conta Google**, cadastro aberto e **verificação manual** da
  conta, com código na bio.
- **Vídeos por link**, vários por criador, conferidos pela operação.
- **Leitura manual das views**; a leitura do fechamento é a que vale para o
  resultado.
- **Ranking aberto** entre os participantes, somando as views de cada criador.
- **Prêmio em texto:** o regulamento diz o que se ganha, a operação registra os
  ganhadores e a gravadora paga direto.
- **Painel da gravadora**, só de leitura, com exportação em planilha.
- **Avisos na tela**, sem e-mail.

> Filosofia: fazer **manualmente o que ainda não vale a pena automatizar**. Se
> o loop funciona no braço, aí se investe em automação.

O detalhe está na `especificacao/`.

---

## 15. Roadmap

- **Fase 0, validação:** fechar com a gravadora o regulamento e o prêmio da
  campanha piloto.
- **Fase 1, MVP:** o piloto do §14.
- **Fase 2, automação no TikTok:** API com conta conectada, depois da leitura
  jurídica e da aprovação do app; curtidas e comentários; metas de views
  calculadas pelo sistema; aviso por e-mail; tendências e projeções.
- **Fase 3, outras redes:** YouTube primeiro, por ser o mais simples de
  automatizar; depois Instagram e Facebook. Cada rede tem o próprio portão de
  aprovação (`pesquisa-das-redes.md`).

---

## 16. Perguntas em aberto

1. **Nome público** e domínio.
2. **Forma de premiação** da campanha piloto (§8.2), que a gravadora define.
3. **Marca:** a plataforma tem marca própria ou aparece como ferramenta da
   gravadora?
4. **KPIs** do piloto: número de criadores, views totais, custo por mil views.
5. **Leitura jurídica** dos termos do TikTok, antes da Fase 2.
6. **Termos de uso e política de privacidade**, com o aceite do ranking.

---

## 17. Referências

Conceito de origem, o "Trabalhe com Artistas" do TikTok:

- [TikTok Creator Academy: Using Work With Artists](https://www.tiktok.com/creator-academy/article/introducing-work-with-artist): como o programa oficial funciona.
- [TikTok Newsroom (PT-BR): TikTok para Artistas](https://newsroom.tiktok.com/pt-br/tiktok-para-artistas): a visão do próprio TikTok.
- [Digital Music News: Work With Artists](https://www.digitalmusicnews.com/2023/05/09/tiktok-work-with-artists-feature-how-it-works/): explicação da mecânica e dos pagamentos.

Referências técnicas (APIs, legalidade, aprovação nas redes): `pesquisa-das-redes.md`.
