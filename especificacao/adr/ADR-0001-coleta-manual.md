# ADR-0001 Coleta manual de views no MVP

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O resultado da campanha depende das views de cada vídeo (RN-39, RN-45), e elas
precisam chegar ao sistema.

O TikTok tem API oficial, a Display API. Conferido em 10-10-2026 na
documentação dele:

- a leitura (`POST /v2/video/query/`, escopo `video.list`) alcança só vídeos
  públicos da conta que autorizou pelo Login Kit; sem conta conectada não há
  número, e o oEmbed público não traz contagem;
- app não aprovado funciona só no sandbox, com até 10 contas que sejam do
  próprio desenvolvedor;
- a revisão do app exige site público completo, termos e política de
  privacidade visíveis, URLs verificadas e vídeo do fluxo inteiro gravado no
  sandbox, recusa app em desenvolvimento e não informa prazo;
- a Display API se descreve como forma de exibir o perfil e os vídeos do
  criador, e medir para premiar não aparece;
- os Termos do Desenvolvedor, modificados em 26-12-2025, proíbem, sem
  consentimento escrito do TikTok, uso comercial que facilite anúncio
  (III.3(c)), montar registro sobre pessoa ou conteúdo (III.3(h)), repassar
  dado a terceiro (II.1 e III.3(b)) e replicar serviço do TikTok (III.3(p)).

O piloto tem até 50 criadores (RNF-01).

## Decisão

No MVP, o operador lê as views na página pública de cada vídeo e as digita no
sistema (RN-39). O sistema não conversa com o TikTok.

## Consequências

Os Termos do Desenvolvedor do TikTok não se aplicam, porque o sistema não usa
os serviços de desenvolvedor. Uma pessoa olhando um número público é uso comum
da rede.

O lançamento não depende de aprovação do TikTok, nem de prazo que ninguém
controla.

O login também não pode ser pelo TikTok, que passa pelo mesmo portão
(ADR-0007).

Custos aceitos:

- o trabalho do operador cresce com o número de vídeos e é o que limita o
  piloto (RNF-01, RNF-26);
- cada leitura vale a data em que foi feita, e a view de uma data passada não
  se recupera (RNF-05);
- só views: digitar curtidas e comentários triplicaria o trabalho, e o prêmio é
  por view.

## Alternativa recusada: API do TikTok desde o início

Foi a primeira escolha: tira a digitação e traz curtidas, comentários e
compartilhamentos sem custo. Perdeu porque põe no caminho do lançamento uma
aprovação sem prazo, que exige o sistema pronto, para um uso que encosta nas
quatro cláusulas dos termos citadas no contexto.

## Alternativa recusada: API como fonte principal, com digitação de reserva

Mantinha a API e tirava o prazo do caminho, com o operador digitando enquanto a
aprovação não saísse. Perdeu porque o login do criador precisaria de outro
caminho antes da aprovação, e o sistema sustentaria duas fontes de leitura sem
saber se a principal seria aprovada.

## Alternativa recusada: scraping da página pública

Automatiza sem aprovação. Recusado por ser frágil (login, captcha, HTML que
muda) e proibido pelos termos de uso das redes (`visao/pesquisa-das-redes.md`,
parte C).

## Quando rever

Quando o piloto passar do teto da RNF-01, ou quando a leitura jurídica dos
termos for favorável e o app for aprovado pelo TikTok. É a Fase 2 da visão.
