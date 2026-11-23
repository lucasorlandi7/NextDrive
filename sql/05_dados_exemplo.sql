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
