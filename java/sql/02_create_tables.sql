

DROP TABLE IF EXISTS auditoria_pagamento, pagamento, locacao, reserva, veiculo, cliente, categoria CASCADE;

CREATE TABLE categoria (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(60)   NOT NULL UNIQUE,
    valor_diaria  NUMERIC(10,2) NOT NULL CHECK (valor_diaria > 0)
);

CREATE TABLE cliente (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    cpf           VARCHAR(11)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    telefone      VARCHAR(20),
    cnh_numero    VARCHAR(11)  NOT NULL,
    cnh_validade  DATE         NOT NULL
);

CREATE TABLE veiculo (
    id             SERIAL PRIMARY KEY,
    placa          VARCHAR(8)  NOT NULL UNIQUE,
    modelo         VARCHAR(60) NOT NULL,
    ano            INTEGER     NOT NULL CHECK (ano BETWEEN 1990 AND 2100),
    quilometragem  INTEGER     NOT NULL DEFAULT 0 CHECK (quilometragem >= 0),
    status         VARCHAR(12) NOT NULL DEFAULT 'DISPONIVEL'
                   CHECK (status IN ('DISPONIVEL', 'LOCADO', 'MANUTENCAO')),
    categoria_id   INTEGER     NOT NULL REFERENCES categoria (id)
);

CREATE TABLE reserva (
    id                       SERIAL PRIMARY KEY,
    cliente_id               INTEGER     NOT NULL REFERENCES cliente (id),
    categoria_id             INTEGER     NOT NULL REFERENCES categoria (id),
    data_retirada_prevista   DATE        NOT NULL,
    data_devolucao_prevista  DATE        NOT NULL,
    status                   VARCHAR(10) NOT NULL DEFAULT 'ATIVA'
                             CHECK (status IN ('ATIVA', 'CANCELADA', 'EFETIVADA')),
    justificativa_cancelamento  VARCHAR(200),
    CONSTRAINT ck_reserva_datas CHECK (data_devolucao_prevista > data_retirada_prevista)
);

CREATE TABLE locacao (
    id                   SERIAL PRIMARY KEY,
    reserva_id           INTEGER     NOT NULL UNIQUE REFERENCES reserva (id),
    veiculo_id           INTEGER     NOT NULL REFERENCES veiculo (id),
    data_retirada        DATE        NOT NULL,
    data_devolucao_real  DATE,
    km_saida             INTEGER     NOT NULL,
    km_chegada           INTEGER,
    valor_final          NUMERIC(10,2),
    status               VARCHAR(10) NOT NULL DEFAULT 'ABERTA'
                         CHECK (status IN ('ABERTA', 'ENCERRADA', 'PAGA'))
);

CREATE TABLE pagamento (
    id              SERIAL PRIMARY KEY,
    locacao_id      INTEGER       NOT NULL REFERENCES locacao (id),
    tipo            VARCHAR(6)    NOT NULL CHECK (tipo IN ('CARTAO', 'PIX')),
    valor           NUMERIC(10,2) NOT NULL CHECK (valor > 0),
    data_pagamento  TIMESTAMP     NOT NULL DEFAULT now(),
    bandeira        VARCHAR(20),   -- só para cartão
    txid            VARCHAR(35),   -- só para Pix
    CONSTRAINT ck_pagamento_tipo CHECK (
        (tipo = 'CARTAO' AND bandeira IS NOT NULL) OR (tipo = 'PIX' AND txid IS NOT NULL))
);
