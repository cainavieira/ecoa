# ADR-0011 Frontend valida forma, backend valida regra

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O sistema tem regras de valor escritas: link do TikTok (RN-34), datas da
campanha em ordem e fechamento futuro (RN-24, RN-26), publicação dentro do
prazo (RN-36), views zero ou maior (RN-39), data de leitura não futura (RN-40).

O backend precisa validar de qualquer forma, porque o navegador pode ser
contornado. A pergunta é se o frontend repete essas regras para dar resposta
imediata.

## Decisão

Divisão por natureza, não por conveniência.

- **O frontend valida forma**: campo obrigatório, é número inteiro, é data, tem
  cara de endereço. É sobre digitação e precisa ser instantâneo.
- **O backend valida regra de negócio**: domínio do link, ordem das datas,
  prazo, faixa. O frontend exibe a recusa que ele devolve.

## Consequências

Regra de negócio tem uma casa só. Nenhuma regra aparece escrita em dois
lugares, e não existe o dia em que alguém muda uma e esquece a outra.

A pessoa descobre que o link não é do TikTok depois de uma ida ao servidor. Na
prática isso é imperceptível, e o custo é aceito.

## Alternativa recusada: duplicar as regras nos dois lados

Daria resposta imediata para tudo. Recusado porque a duplicação entre o Bean
Validation e o esquema do frontend é garantia de divergência futura, e regra
divergente aparece para a pessoa como recusa inexplicável.
