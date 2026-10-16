# Auditoria de coerência

- **Data:** 16-10-2026
- **Modo:** desde `74a7118`
- **Auditado:** `74a7118..5bea3f1`, a identidade visual
- **Correções:** o commit deste relatório
- **Estado:** dois achados confirmados, decididos e aplicados

Dois agentes, um auditor e um cético, sobre os 10 itens de texto alterados; os
6 SVG ficaram de fora. O auditor achou 4 defeitos, e o cético confirmou 2. O
script registrou os quatro como "sem veredito", porque casa o veredito pelo
título exato e o cético reescreveu os títulos; os vereditos foram lidos no
registro dos agentes. Cada achado foi conferido, com a conta de contraste
refeita.

## Confirmados

| Problema | Decisão |
|:--|:--|
| O vermelho é cor de ação, mas o texto sobre ele não está definido. Pela convenção, cai no texto claro: 2,46 para 1, abaixo de 4,5, a mesma falha recusada para o laranja. Verde e amarelo falhariam se o selo fosse preenchido | Texto sobre o vermelho em `#141210` (6,8), no token `--destructive-foreground`; o selo é texto colorido com contorno, sem preenchimento |
| A borda de campo divide o valor com a divisória e dá 1,52 contra o fundo, abaixo do mínimo de 3 para borda de componente | Borda de campo própria, `#78716C` (3,9 contra o fundo), no token `--input`; a divisória fica em `--border` e não tem mínimo |

## Derrubados pelo cético

- O gráfico sem cor própria na paleta. Lacuna, resolvida pela T-25 dentro da
  paleta. Hipótese não verificada: `--chart-1` a `--chart-5` do shadcn/ui
  fora da paleta, para conferir na T-26.
- O aviso da RN-65 no amarelo, que diz "tem volta", sendo permanente. Rótulo
  impreciso, decidido de forma explícita, sem efeito no que se constrói.
