# Identidade visual

Como o sistema se parece: cor, fonte, tamanho, densidade e logotipo. Vale para
as telas, para o design delas (T-25) e para qualquer peça fora do sistema, como
a imagem de compartilhamento de uma campanha. Como isso vira código está no
`convencoes-frontend.md`.

## Modo

Só o modo escuro.

## Cor

| Nome | Valor | Papel |
|:--|:--|:--|
| fundo | `#141210` | a página, quase preto quente |
| superfície | `#1C1916` | cartão, planilha, painel |
| superfície de campo | `#25211D` | fundo de campo, de aba e de item em foco |
| borda | `#3A342E` | divisória e borda de cartão |
| borda de campo | `#78716C` | borda de campo e de caixa de seleção |
| texto | `#F5F1EC` | texto, quase branco quente |
| texto suave | `#A8A29E` | texto secundário, data e status de espera |
| laranja | `#E8742E` | ação: botão, link e foco |
| texto sobre o laranja | `#141210` | o texto dentro do botão |
| verde | `#4ADE80` | status que está valendo |
| amarelo | `#FACC15` | status que pede atenção |
| vermelho | `#F87171` | status que está fora, e ação que apaga ou bloqueia |
| texto sobre o vermelho | `#141210` | o texto dentro do botão que apaga ou bloqueia |

Fundo e texto não são preto e branco puros. O par puro dá contraste de 21 para
1, cansa a vista e vibra na tela; o par escolhido dá 16,6 e continua muito
acima do mínimo.

**Contraste.** Texto comum tem razão de contraste de no mínimo 4,5 contra o
fundo; texto grande e borda de componente, 3 (WCAG 2, nível AA). Branco sobre
o laranja dá 3,0 e não passa, por isso o texto em cima do laranja e do
vermelho é escuro (6,2 e 6,8). A divisória é decorativa e não tem mínimo.

**O laranja é ação:** botão, link e foco. Nunca indica status nem destaca
linha.

## Status

A cor segue o significado. O selo é texto colorido, com contorno da mesma cor,
sobre o fundo; não é preenchido. A cor reforça o texto, nunca diz sozinha.

| Cor | Significa | Conta | Vídeo | Campanha |
|:--|:--|:--|:--|:--|
| verde | está valendo | aprovada | aprovado | aberta |
| amarelo | pede atenção, e tem volta | recusada, e o aviso da RN-65 | recusado | em apuração |
| vermelho | está fora, sem volta pela própria pessoa | bloqueada | desqualificado | |
| cinza (texto suave) | espera | pendente | em análise | agendada |
| contorno sem cor | acabou | | | encerrada |

## Fonte e tamanho

Bricolage Grotesque nos títulos; Geist no texto e nos números. Em coluna de
número, os algarismos têm a mesma largura, para o milhar alinhar.

| Tamanho | Uso |
|:--|:--|
| 30 px | título da tela |
| 24 px | título de seção |
| 18 px | título de cartão |
| 16 px | texto e campo |
| 14 px | planilha |
| 12 px | legenda |

Campo de formulário tem 16 px: abaixo disso, o Safari do iPhone aproxima a
página ao tocar no campo. A exceção é a planilha de leituras, que é do
computador.

O canto arredondado tem 10 px.

## Densidade

O criador, no celular, vê cartões com espaço para o dedo. O operador e a
gravadora, no computador, veem tabela compacta, porque o fechamento chega a 150
linhas (RNF-26).

## Logotipo

Direção Alcance: um ponto laranja, a música, com dois anéis abertos, o eco. O
ponto é o único laranja do logotipo. Os arquivos estão em `marca/`, ao lado
deste documento: `logotipo` (símbolo e nome) e `simbolo` (sozinho), cada um em
`cor`, `branco` e `preto`. O nome é provisório até a decisão do nome público
(`instrucao.md`, seção 6).

## Recusado

| Recusado | Motivo |
|:--|:--|
| Modo claro | Cada cor teria dois valores, e cada tela, duas conferências. O interessado escolheu nascer só escuro |
| Texto branco no botão laranja | Contraste de 3,0 para 1, abaixo do mínimo de 4,5 |
| Laranja como cor de status | Competiria com o botão, e o laranja é vizinho do amarelo e do vermelho |
