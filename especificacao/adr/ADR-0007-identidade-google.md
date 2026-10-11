# ADR-0007 Identidade pelo Google, em produção e sem senha

- **Data:** 10-10-2026
- **Situação:** aceito

## Contexto

O cadastro de criador é aberto (RN-03), e operadores e gravadora também entram
pela mesma porta. Não existe serviço de e-mail no desenho (RN-56).

Conferido em 10-10-2026 na documentação do Google: o app que pede só nome,
e-mail e perfil não precisa de lista de usuários de teste nem mesmo em modo de
teste ("your users do not need to be in the trusted user list"), sem aviso e
sem expirar a autorização em 7 dias; publicado em produção, fica disponível a
qualquer conta Google. A verificação pode ser exigida só para o nome e o logo
do app aparecerem na página de login do Google, ou para escopos sensíveis.

## Decisão

A autenticação é delegada ao Google, por OpenID Connect, pedindo só `openid`,
e-mail e perfil (RNF-22). O app é publicado em produção, sem verificação. Só o
e-mail que o Google devolve como verificado é aceito (RN-01). O sistema não
guarda senha.

## Consequências

Somem do escopo: guardar senha, recuperação de senha, verificação de e-mail e
a responsabilidade de ter senha de outras pessoas no banco.

Quem não tem conta Google fica de fora. Aceito: é raro, e a conta é gratuita.

A página de login do Google pode não mostrar o nome e o logo do Ecoa enquanto
não houver verificação de marca, que é opcional. As telas do sistema, inclusive
o botão de entrar, não são afetadas.

Indisponibilidade do Google impede login novo. Sessão aberta não cai.

## Alternativa recusada: login pelo TikTok

O Login Kit provaria a posse do @ no próprio login, sem código na bio. Recusado
porque é produto do mesmo programa da API do TikTok e passa pela mesma
aprovação (ADR-0001): até ela sair, só funciona para 10 contas do próprio
desenvolvedor.

## Alternativa recusada: e-mail e senha próprios

O trabalho de verdade não é a tela de login, é o que vem atrás dela:
recuperação de senha, que exige enviar e-mail, e guardar senha com hash.

## Alternativa recusada: link de acesso por e-mail

Dispensa senha, mas exige serviço de e-mail, domínio verificado e configuração
de DNS para cada login.

## Quando rever

Na Fase 2, com a API do TikTok aprovada: o Login Kit pode virar a forma de
conectar a conta do criador, sem substituir este login.
