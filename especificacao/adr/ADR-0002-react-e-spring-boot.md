# ADR-0002 Frontend React separado, consumindo API em Spring Boot

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

Três atores têm tela (AT-01, AT-02, AT-03), e quase tudo é formulário e lista,
com gráficos (RN-49), login pelo Google (ADR-0007) e exportação em CSV
(RN-62). A tela do operador é cadastro e edição, e é metade do sistema.

A interface é carro-chefe do produto: o criador e a gravadora julgam a
plataforma pela tela.

## Decisão

Frontend em React com Vite e TypeScript, separado, consumindo uma API em Spring
Boot com Java e Maven. A camada web é o Spring MVC.

O contrato de API é gerado a partir do código que roda, pelo springdoc-openapi,
ligado só em desenvolvimento, e dele saem os tipos do frontend (ADR-0010). As
versões estão nas convenções de cada lado.

## Consequências

Dois projetos no mesmo repositório (ADR-0003), um contrato de API
(`endpoints.md`) e dois builds.

As telas do operador são escritas à mão, com os componentes do shadcn/ui
(ADR-0010).

O frontend é estático, e o Caddy entrega o build e repassa `/api` para o Spring
(ADR-0015). Navegador e api ficam na mesma origem.

É a mesma stack do Tracking peso, o que reaproveita convenção, armadilha já
conhecida e aprendizado.

## Alternativa recusada: Django com páginas montadas no servidor

O admin do Django entregaria quase pronta a tela do operador, sem contrato de
API nem frontend separado, e em Python. Recusado porque a interface é
carro-chefe, e React com shadcn/ui dá mais controle de layout do que páginas
montadas no servidor.

## Alternativa recusada: Spring Boot com páginas montadas no servidor

Um projeto só, sem contrato de API. Recusado pelo mesmo motivo de interface, e
porque não traria a tela de administração pronta, que era a vantagem do Django.
