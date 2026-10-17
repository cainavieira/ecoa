-- As dez tabelas do MVP, com as restrições que o banco consegue impor
-- (instrucao.md, seção 5). As que atravessam tabelas ou dependem do dia
-- ficam na aplicação.

create table conta (
    id_conta int generated always as identity primary key,
    email text not null unique check (email = lower(email)),
    nome text,
    funcao text not null
        check (funcao in ('criador', 'operador', 'gravadora')),
    criado_em timestamptz not null
);

create table criador (
    id_criador int primary key references conta (id_conta) on delete cascade,
    usuario_tiktok text,
    codigo_verificacao text not null unique,
    status text not null
        check (status in ('pendente', 'recusada', 'aprovada', 'bloqueada')),
    motivo_recusa text,
    motivo_bloqueio text,
    motivo_desbloqueio text,
    aceite_termos_em timestamptz not null,
    constraint criador_usuario_tiktok_exigido_check
        check (status = 'pendente' or usuario_tiktok is not null),
    constraint criador_usuario_tiktok_normalizado_check
        check (usuario_tiktok = lower(usuario_tiktok)
            and usuario_tiktok !~ '^@|\s' and usuario_tiktok <> ''),
    constraint criador_motivo_recusa_check
        check (status <> 'recusada' or motivo_recusa is not null),
    constraint criador_motivo_bloqueio_check
        check (status <> 'bloqueada' or motivo_bloqueio is not null)
);

-- RN-11: pendentes e recusadas podem repetir o @
create unique index criador_usuario_tiktok_key
    on criador (usuario_tiktok)
    where status in ('aprovada', 'bloqueada');

create table token_renovacao (
    id_token_renovacao int generated always as identity primary key,
    xid_conta int not null references conta (id_conta) on delete cascade,
    hash text not null unique,
    criado_em timestamptz not null,
    expira_em timestamptz not null
);

create table campanha (
    id_campanha int generated always as identity primary key,
    musica text not null,
    artista text not null,
    regulamento text not null,
    data_inicio date not null,
    data_fim date not null,
    data_fechamento date not null,
    encerrada_em timestamptz,
    criado_em timestamptz not null,
    constraint campanha_datas_check
        check (data_inicio <= data_fim and data_fim <= data_fechamento)
);

create table link_referencia (
    id_link_referencia int generated always as identity primary key,
    xid_campanha int not null
        references campanha (id_campanha) on delete cascade,
    url text not null,
    descricao text
);

-- Sem cascata da campanha: com participante, ela não se apaga (RN-30).
-- O criador apagado vira nulo, o "participante removido" (RN-59).
create table participacao (
    id_participacao int generated always as identity primary key,
    xid_campanha int not null references campanha (id_campanha),
    xid_criador int references criador (id_criador) on delete set null,
    aceite_em timestamptz not null,
    posicao_final int,
    total_final bigint,
    unique (xid_campanha, xid_criador)
);

create table video (
    id_video int generated always as identity primary key,
    xid_participacao int not null
        references participacao (id_participacao) on delete cascade,
    link text,
    status text not null
        check (status in
            ('em_analise', 'aprovado', 'recusado', 'desqualificado')),
    data_publicacao date,
    motivo text,
    enviado_em timestamptz not null,
    constraint video_data_publicacao_check
        check (status not in ('aprovado', 'desqualificado')
            or data_publicacao is not null),
    constraint video_motivo_check
        check (status not in ('recusado', 'desqualificado')
            or motivo is not null)
);

-- RN-35: depois de recusado, o link pode voltar
create unique index video_link_key
    on video (link)
    where status <> 'recusado';

create table leitura (
    xid_video int not null references video (id_video) on delete cascade,
    data date not null,
    views bigint not null check (views >= 0),
    registrada_em timestamptz not null,
    primary key (xid_video, data)
);

create table ganhador (
    id_ganhador int generated always as identity primary key,
    xid_participacao int not null
        references participacao (id_participacao) on delete cascade,
    premio text not null,
    entregue_em date,
    registrado_em timestamptz not null
);

-- RN-65: sem chave estrangeira, porque a conta já não existe
create table conta_bloqueada_apagada (
    usuario_tiktok text primary key
        check (usuario_tiktok = lower(usuario_tiktok)
            and usuario_tiktok !~ '^@|\s' and usuario_tiktok <> ''),
    apagada_em timestamptz not null
);
