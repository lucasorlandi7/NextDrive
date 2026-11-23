-- Next Drive (versão simples) - dados de exemplo

INSERT INTO categoria (nome, valor_diaria) VALUES
    ('Econômico', 120.00),
    ('SUV',       220.00),
    ('Luxo',      380.00);

INSERT INTO veiculo (placa, modelo, ano, quilometragem, categoria_id) VALUES
    ('ABC1D23', 'Fiat Mobi',        2023, 15200, 1),
    ('DEF4G56', 'Chevrolet Onix',   2024,  8300, 1),
    ('GHI7J89', 'Jeep Compass',     2023, 22100, 2),
    ('JKL0M12', 'Toyota Corolla X', 2024,  5400, 3),
    ('NOP3Q45', 'Renault Kwid',     2022, 41000, 1);

INSERT INTO cliente (nome, cpf, email, telefone, cnh_numero, cnh_validade) VALUES
    ('Ana Lima',        '11122233344', 'ana.lima@example.com',       '49999990001', '12345678901', '2030-05-20'),
    ('Carlos Pereira',  '55566677788', 'carlos.pereira@example.com', '49999990002', '10987654321', '2029-11-10'),
    ('Marcos Almeida',  '99988877766', 'marcos.almeida@example.com', '49999990003', '11223344556', '2031-02-15');

-- Reservas: duas viraram locação já devolvida, uma está com o carro na rua,
-- uma ainda vai acontecer e uma foi cancelada.
INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, data_devolucao_prevista, status, justificativa_cancelamento) VALUES
    (1, 1, '2026-11-02', '2026-11-05', 'EFETIVADA', NULL),
    (2, 2, '2026-11-10', '2026-11-14', 'EFETIVADA', NULL),
    (3, 3, '2026-11-20', '2026-11-25', 'EFETIVADA', NULL),
    (1, 2, '2026-12-01', '2026-12-03', 'ATIVA',     NULL),
    (2, 1, '2026-11-18', '2026-11-20', 'CANCELADA', 'Cliente desistiu da viagem');

-- Locações:
--   1) Ana, Fiat Mobi: 3 diárias de R$ 120,00 = R$ 360,00, já paga.
--   2) Carlos, Jeep Compass: devolveu 2 dias depois do previsto.
--      6 diárias de R$ 220,00 + 2 x 30% da diária = R$ 1.452,00; pagou só uma parte.
--   3) Marcos, Toyota Corolla: ainda está com o carro.
INSERT INTO locacao (reserva_id, veiculo_id, data_retirada, data_devolucao_real, km_saida, km_chegada, valor_final, status) VALUES
    (1, 1, '2026-11-02', '2026-11-05', 15200, 15650,  360.00, 'PAGA'),
    (2, 3, '2026-11-10', '2026-11-16', 22100, 23050, 1452.00, 'ENCERRADA'),
    (3, 4, '2026-11-20', NULL,          5400,  NULL,    NULL, 'ABERTA');

-- Situação atual dos carros depois dessas locações.
UPDATE veiculo SET quilometragem = 15650 WHERE id = 1;
UPDATE veiculo SET quilometragem = 23050 WHERE id = 3;
UPDATE veiculo SET status = 'LOCADO'     WHERE id = 4;
UPDATE veiculo SET status = 'MANUTENCAO' WHERE id = 5;

-- Pagamentos: um com cartão (quita a locação 1) e um Pix parcial da locação 2.
INSERT INTO pagamento (locacao_id, tipo, valor, data_pagamento, bandeira, txid) VALUES
    (1, 'CARTAO', 360.00, '2026-11-05 17:40', 'Visa', NULL),
    (2, 'PIX',    700.00, '2026-11-16 10:15', NULL,   'E1234567820261116101500001');
