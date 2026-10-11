# ADR-0017 Imagens construídas na integração contínua, e deploy manual

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

A produção roda em Docker Compose numa VPS (ADR-0015), com duas imagens
próprias: a da api e a do frontend, que é o Caddy com o build do React. O
repositório é público no GitHub (ADR-0018), e o deploy precisa do ensaio e da
migração antes da api nova (ADR-0006).

## Decisão

A integração contínua, no GitHub Actions, roda as verificações da qualidade e
dos testes (ADR-0013) em todo envio. Na `main`, ela também constrói as duas
imagens, em dois estágios, e as envia ao registro de imagens do GitHub, com a
etiqueta do commit.

O deploy é manual: um script, rodado na VPS por quem mantém o sistema, que para
no primeiro erro:

1. baixa as imagens da etiqueta escolhida;
2. roda o ensaio sobre o dump mais recente (ADR-0006);
3. roda a migração;
4. sobe a versão nova;
5. confere que `GET /saude` responde.

## Consequências

A imagem que vai ao ar é a mesma que passou nos testes.

A VPS não compila nada, e não precisa de memória para o Maven nem para o Vite.

O primeiro deploy é montagem, feita à mão. O script começa pelo ensaio, e o
ensaio precisa de um dump, que só existe depois de o backup rodar uma vez.

## Alternativa recusada: construir as imagens na própria VPS

Dispensa o registro de imagens. Recusado porque compilar Spring e Vite pede
memória que uma VPS pequena pode não ter, e a imagem construída lá não é a que
passou nos testes.

## Alternativa recusada: deploy automático a cada merge

Recusado também no Tracking peso. Aqui pesa mais: um deploy sem ninguém
olhando, no meio de uma campanha, derruba o site justo quando os criadores
estão usando.
