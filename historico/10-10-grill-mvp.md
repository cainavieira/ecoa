# Grill do MVP

- **Data:** 09-10-2026 e 10-10-2026
- **Estado:** decisões fechadas; a especificação ainda não foi escrita

Registro das decisões que recortaram o MVP do Ecoa, com o porquê e o que se
aceitou perder. A especificação sai daqui. Quando ela existir, é ela que vale,
e este registro não se reescreve.

Q1 a Q43 são as perguntas do grill, na ordem em que foram feitas. A1 a A10 são
pontos assumidos e depois confirmados.

## Recorte

Os documentos iniciais descrevem a ideia completa: várias redes, coleta
automática, painel da gravadora, pagamento pela plataforma. O MVP é só TikTok,
com coleta manual.

- A visão completa vai para `visao/visao-do-produto.md`, que substitui o
  documento de conceito e continua sendo o que o Mateus aprova. A
  `especificacao/` descreve só o MVP e é a única fonte do que se constrói:
  especificação do que não vai ser construído envelhece sem nada que a
  contradiga. (Q1, Q9)
- O documento técnico se divide: o que vale para o MVP vira regra e ADR; o que
  é de outras redes vai para `visao/pesquisa-das-redes.md`. O `conversa.md` vai
  para `historico/` depois de absorvido. (Q9)
- Piloto: uma gravadora, uma campanha por vez, até 50 criadores. É premissa de
  operação, não regra do sistema. (Q3)
- A gravadora paga os criadores direto, por fora do sistema. O que a visão diz
  sobre como a plataforma ganha se decide ao reescrevê-la. (Q5, A7)

## Atores

| Quem | Tela | O que faz |
|:--|:--|:--|
| Criador | sim | entra com Google, entra na campanha, envia vídeos, acompanha situação, ranking e ganhadores |
| Operador | sim | Caina e Mateus, com as mesmas permissões: aprova e bloqueia conta, cria campanha, aprova vídeo, digita leitura, registra ganhador |
| Gravadora | só leitura | outras pessoas da gravadora: estatísticas da campanha e exportação |
| Google | não | sistema externo que autentica todos |

Não são atores o artista, que age pela gravadora, e o TikTok, porque no MVP não
há integração com ele. O primeiro operador entra por comando de linha no
servidor; os outros operadores e a gravadora, pela tela do operador. (Q4, Q13,
A5, A6)

## Coleta manual no MVP, API do TikTok na Fase 2

A primeira escolha foi a API do TikTok desde o início. A documentação oficial,
conferida em 10-10-2026, a derrubou (Q2, Q10):

- a leitura (`POST /v2/video/query/`, escopo `video.list`) alcança só vídeos
  públicos da conta que autorizou, e o oEmbed não traz contagem: sem conta
  conectada, não há número;
- app não aprovado funciona só no sandbox, com até 10 contas que sejam nossas;
- a revisão exige site público completo, termos e política visíveis, URLs
  verificadas e vídeo do fluxo gravado no sandbox, recusa app em
  desenvolvimento e não informa prazo;
- a Display API se descreve para exibir perfil e vídeos do criador, e medir
  para premiar não aparece;
- os Termos do Desenvolvedor, modificados em 26-12-2025, proíbem sem
  consentimento escrito do TikTok o uso comercial que facilite anúncio
  (III.3(c)), montar registro sobre pessoa ou conteúdo (III.3(h)), repassar
  dado a terceiro (II.1 e III.3(b)) e replicar serviço do TikTok (III.3(p));
  uma campanha paga de música encosta nas quatro;
- o login pelo TikTok é do mesmo programa e passa pelo mesmo portão.

Sem API, os Termos do Desenvolvedor não se aplicam. A API entra na Fase 2,
depois de leitura jurídica dos termos. Para lá ficam guardados: até 20 vídeos
por chamada; views, curtidas, comentários e compartilhamentos; token de acesso
de 24 horas e de renovação de 365 dias; 600 chamadas por minuto por endpoint; o
vídeo não informa o som usado. O link `tiktok-api-v1-video-list` do documento
técnico é da v1, marcada como legado.

## Campanha

- Uma música, com regulamento em texto escrito pela gravadora e três datas
  definidas pelo operador: **início**, a partir de quando vale publicar;
  **fim**, último dia para publicar, conferido pela data de publicação;
  **fechamento**, quando os números congelam. A distância entre fim e
  fechamento é a estabilização. Janela por vídeo foi recusada, porque cada
  vídeo fecharia num dia diferente. Custo aceito: o vídeo publicado no último
  dia tem menos tempo para crescer. (Q14)
- O material de referência (som, vídeo de exemplo, regras) é link, do TikTok ou
  de qualquer lugar. Não há upload no sistema: a ideia de guardar arquivo foi
  descartada. (A2, Q43)
- Para enviar vídeo, o criador entra na campanha, aceitando o regulamento e o
  ranking aberto. (A1)

## Prêmio

- O sistema mede e mostra, e o prêmio é texto. O regulamento diz o que se ganha
  ("R$ 1.000 a dividir", "iPhone para quem passar de 100 mil views"), e o
  operador registra cada ganhador: quem, o prêmio em texto e a data de entrega.
  Nenhuma regra de prêmio vai para o código, porque a forma da gravadora ainda
  não é conhecida e o prêmio pode ser dinheiro ou produto. Recusados por ora:
  metas de views marcadas pelo sistema e taxa em reais por mil views. Quando a
  regra existir, entra como regra nova. (Q7, Q11)
- O prêmio é por view. Curtidas, comentários e fator de plataforma saem do MVP;
  os modos competição (rateio, ranking com prêmio fixo) ficam na visão. (Q7)
- O sistema não guarda valor devido: guarda ganhador, prêmio e entrega. (Q5,
  Q11)

## Criador

- Entra com Google, em cadastro aberto: a gravadora divulga o link, e a
  aprovação manual é o filtro. (Q16, Q17)
- Posse do @: o sistema gera um código, o criador o põe na bio do TikTok, e o
  operador confere. Pessoa real: o operador olha a conta e decide, sem critério
  numérico. (Q18)
- O sistema guarda nome e e-mail, que vêm do Google, e o @. Não guarda CPF,
  chave Pix nem endereço: a gravadora pede ao ganhador na entrega. Custo
  aceito: nada impede a mesma pessoa de participar com duas contas do TikTok, e
  isso só aparece se as duas ganharem. (Q18)
- Os dados ficam até o criador pedir a exclusão ao operador. Nas campanhas
  encerradas, somem nome, e-mail e @, e as views continuam nos totais como
  "participante removido", para o relatório já entregue à gravadora não mudar.
  (Q30, A4)

## Vídeo, leitura e ranking

- Vários vídeos por criador, sem limite. (Q19)
- O criador cola o link. O operador confere se o vídeo é público, da conta
  verificada, usa o som da campanha (e a hashtag, se o regulamento pedir) e foi
  publicado entre início e fim. Aprovando, faz a primeira leitura. Recusado, o
  motivo aparece na tela e o criador pode enviar outro dentro do prazo. Vídeo
  aprovado não se troca. (Q20)
- A leitura é manual e só de views, em qualquer data. A do fechamento é
  obrigatória e é a única que vale para o resultado. A frequência das outras é
  prática do operador, não regra. Cada número mostra "lido em". O gráfico tem
  um ponto por leitura. (Q15, Q21)
- O ranking é por criador, somando as views dos vídeos aprovados, com os vídeos
  de cada um abaixo do nome. Todos os participantes e a gravadora o veem.
  Página pública foi recusada: expõe dado de pessoa a quem não participa. (Q12,
  Q24)
- O criador vê o resultado das campanhas passadas. (A3)

## Bloqueio e desqualificação

- Bloqueio é ação do operador, com motivo obrigatório na tela do criador e um
  contato para contestar por fora. Vale para a conta inteira. O bloqueado sai
  do ranking e não pode ser ganhador; prêmio já registrado não muda sozinho.
  Desbloquear é possível, com motivo. (Q6, Q22)
- Vídeo desqualificado (saiu do ar, ficou privado, conta banida pelo TikTok)
  sai da campanha, e o criador não é bloqueado. (Q6, Q22)

## Avisos e painel da gravadora

- Avisos só na tela, sem e-mail. A gravadora contata o ganhador por fora. Custo
  aceito: quem teve vídeo recusado só descobre ao voltar, e pode perder o
  prazo. Se isso acontecer no piloto, o e-mail entra. (Q41)
- O painel da gravadora mostra views totais, criadores e vídeos aprovados,
  ranking, gráfico das views somadas por leitura e ganhadores, e exporta CSV.
  (Q23)

## Requisitos não funcionais

- Sem meta de disponibilidade. (Q33)
- Perda máxima de 24 horas. A leitura é o dado que não se recupera, porque o
  TikTok só mostra o número atual. Prática do operador: exportar o CSV logo
  depois da leitura do fechamento. (Q33)
- Datas de campanha, publicação e leitura são data pura. O "hoje" é do
  servidor, no horário de Brasília, e o regulamento diz isso. (Q37)

## Stack e código

- Herdado do Tracking peso: React com Vite e TypeScript, TanStack Query, React
  Router declarativo, React Hook Form com Zod, tipos gerados do OpenAPI,
  Tailwind com shadcn/ui, Recharts; Spring Boot com Java e Maven, Spring Data
  JPA, Flyway, Spring Security com JWT em cookie; Postgres. Ajustes: Google em
  produção, três papéis no lugar do convite, migração como comando do Compose.
  Não herdados: servidor em casa, k3s, Traefik, CloudNativePG, convite, peso
  decimal, sobreposição de fase. (Q26, Q31)
- Django com páginas montadas no servidor foi recusado: a interface é
  carro-chefe, e React com shadcn/ui dá mais controle. Custo aceito: as telas
  do operador são escritas à mão. (Q26)
- Login Google em produção, só com `openid`, e-mail e perfil: para esses
  escopos, o Google não exige lista de usuários de teste nem verificação; a
  verificação de marca só põe nome e logo na página do Google. (Q16)
- Testes: regras puras para as transições de estado e o ranking, rotas contra
  Postgres, um teste de fumaça com Playwright; Spotless e Error Prone em tudo,
  NullAway nas regras. (Q36)
- Convenções de backend e de frontend no formato dos contratos do
  paineis-kanban (stack com versão, pastas, dependência, regras, nomes, testes,
  proibido), mais formato de erro, idioma dos identificadores e acessibilidade
  do Tracking peso. As versões são conferidas na fonte oficial no lote 2, e o
  nome do arquivo não leva "v1". (Q38)
- O código é escrito pela assistência e revisado pelo Caina; nas partes que ele
  quiser aprender, inverte. (Q25)

## Ambientes, produção e backup

- Desenvolvimento todo na máquina do Caina: Postgres em Docker, api e frontend
  rodando local. A VPS só recebe o MVP pronto. (Q27)
- Produção numa VPS com Docker Compose: api, frontend e Postgres, e o Caddy na
  frente fazendo HTTPS, entregando o build do React e repassando `/api`. Sem
  k3s, sem nginx. Plataforma gerenciada recusada pelo custo (preços não
  conferidos); servidor em casa recusado porque cai com a casa. Deveres
  aceitos: atualizações de segurança automáticas, SSH só com chave, firewall,
  backup e monitor externo. (Q27)
- Banco só na rede interna do Compose. Backup com dump diário; um agendamento
  na máquina do Caina baixa o dump, guarda 30 dias e restaura o mais recente
  num Postgres em Docker local, que serve de teste de restauração. O aviso de
  homem morto vigia esse download. Ensaio no deploy: restaurar o dump num banco
  descartável e aplicar as migrações novas. Cópia na nuvem: quando rever. (Q39,
  Q42)
- A integração contínua testa e, na `main`, constrói as imagens e as envia ao
  registro do GitHub. O deploy é manual, por script na VPS: baixa, ensaia,
  migra, sobe e confere `GET /saude`. Deploy automático recusado. (Q40)
- Repositório público no GitHub, sem licença, com commits pelo endereço noreply
  e leitura do que fica público antes do primeiro push. (Q35)
- Nome público e domínio se decidem com o Mateus antes do deploy. (Q28)

## Documentos, em ordem

0. `git init`, primeiro commit e este registro.
1. Lote 1, produto: os dois documentos de `visao/` e, na `especificacao/`,
   atores, regras de negócio, requisitos funcionais, casos de uso e requisitos
   não funcionais.
2. Design das telas com shadcn/ui, a partir dos casos de uso, no Claude Design
   se a conta tiver. Tela antes de endpoint, porque há um cliente só. (Q34)
3. Lote 2, técnico: ADR com o modelo do Tracking peso, arquitetura (contexto e
   contêiner), convenções de backend e de frontend, endpoints e fluxo de
   trabalho, herdado do Tracking peso. (A9)
4. Lote 3, partida: `instrucao.md`, `tarefas.md`, `README.md` e `CLAUDE.md`.

A skill de auditoria de coerência entra depois do lote 1. (Q29)

## Fase 2, na visão

API do TikTok depois da leitura jurídica, curtidas e comentários, metas de
views, e-mail, cópia do backup na nuvem, outras redes, tendências e projeções.
(A10)

## Depende do mundo real

Termos de uso e política de privacidade, com o consentimento do ranking; nome
público e domínio; a VPS e o monitor externo. (A8, Q28)

## Fontes

- TikTok for Developers: [Display API](https://developers.tiktok.com/doc/display-api-overview),
  [Query Videos](https://developers.tiktok.com/docs/en/tiktok-api-v2-video-query),
  [List Videos](https://developers.tiktok.com/doc/tiktok-api-v2-video-list),
  [Escopos](https://developers.tiktok.com/doc/tiktok-api-scopes),
  [Tokens](https://developers.tiktok.com/doc/oauth-user-access-token-management),
  [Limites](https://developers.tiktok.com/doc/tiktok-api-v2-rate-limit),
  [oEmbed](https://developers.tiktok.com/doc/embed-videos),
  [Login Kit Web](https://developers.tiktok.com/docs/en/login-kit-web),
  [Sandbox](https://developers.tiktok.com/docs/en/add-a-sandbox),
  [Revisão do app](https://developers.tiktok.com/docs/en/app-review-guidelines),
  [Registrar o app](https://developers.tiktok.com/docs/en/getting-started-create-an-app),
  [Verificação de empresa](https://developers.tiktok.com/docs/en/verify-your-business),
  [Termos do Desenvolvedor](https://www.tiktok.com/legal/page/global/tik-tok-developer-terms-of-service/en)
- Google: [Manage App Audience](https://support.google.com/cloud/answer/15549945?hl=en),
  [When is verification not needed](https://support.google.com/cloud/answer/13464323?hl=en)
