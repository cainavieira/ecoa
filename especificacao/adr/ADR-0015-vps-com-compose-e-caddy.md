# ADR-0015 Produção numa VPS com Docker Compose e Caddy

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O login pelo Google exige endereço público com HTTPS. Criadores e gravadora
dependem do site durante a campanha, mas não há meta de disponibilidade
(RNF-03). O sistema tem três peças que ficam no ar: o frontend estático, a api
e o banco. O custo sai do bolso dos sócios (RNF-02).

## Decisão

Produção numa VPS, com Docker Compose rodando três serviços:

- o **Caddy**, na frente, que obtém e renova o certificado HTTPS sozinho,
  entrega o build do React, devolve `index.html` para todo caminho que não é
  arquivo, e repassa `/api` para a api;
- a **api**, em Spring Boot, com uma réplica;
- o **Postgres**, só na rede interna do Compose, sem porta publicada.

Banco e api ficam na mesma máquina. O desenvolvimento acontece todo na máquina
de quem desenvolve, com o Postgres em Docker, e a VPS só recebe o MVP pronto
(RNF-27). Não há homologação.

A VPS aceita SSH só com chave, abre só as portas 22, 80 e 443, e instala
atualizações de segurança sozinha (RNF-19).

## Consequências

Navegador, frontend e api ficam na mesma origem. Isso apaga a configuração de
origem cruzada, deixa o cookie funcionar sem marca especial (ADR-0008) e faz
existir um único endereço de retorno no Google.

O banco não é alcançável de fora (RNF-20): nenhum caminho chega a ele sem passar
pela api.

Deveres aceitos por quem mantém o sistema: as atualizações e o acesso acima, o
backup (ADR-0016) e o monitor externo (RNF-04). A primeira montagem leva um ou
dois dias para quem nunca fez; depois, minutos por mês.

## Alternativa recusada: plataforma gerenciada

Menos peças para manter, com HTTPS e banco gerenciados e backup do banco
incluído. Recusada pelo custo mensal maior, com os preços ainda não
conferidos (RNF-02).

## Alternativa recusada: servidor em casa, como no Tracking peso

Custa só energia. Recusado porque cai junto com a energia e a conexão da casa, e
aqui há criadores e uma gravadora usando o site durante a campanha.

## Alternativa recusada: Kubernetes com k3s

É o desenho do Tracking peso. Recusado porque é um orquestrador para três
serviços com uma réplica cada, e o Compose faz o mesmo com menos peças.

## Alternativa recusada: nginx com certbot

Duas peças para o que o Caddy faz sozinho: entregar o estático, repassar
`/api` e cuidar do certificado.
