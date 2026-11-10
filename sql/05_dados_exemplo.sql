-- =====================================================================
-- Next Drive - 05_dados_exemplo.sql : dados fictícios para testes
-- Executar:  psql -U postgres -d next_drive -f sql/05_dados_exemplo.sql
-- Todos os usuários de exemplo usam a senha  Senha@123  (guardada como hash).
-- Documentos (CPF/CNPJ/CNH) são fictícios. O estado final dos veículos e das
-- locações já é consistente com as regras dos triggers (script 08).
-- =====================================================================

-- Parâmetros de cálculo da devolução
INSERT INTO parametro_sistema (chave, valor, descricao) VALUES
    ('preco_litro_combustivel', 6.20, 'Preço do litro de combustível cobrado na diferença de tanque (R$)');

-- Lojas
INSERT INTO loja (id, nome, cidade, uf, endereco, telefone) VALUES
    (1, 'Next Drive Centro',  'São Miguel do Oeste', 'SC', 'Av. Getúlio Vargas, 1200', '(49) 3622-1000'),
    (2, 'Next Drive Chapecó', 'Chapecó',             'SC', 'Av. Fernando Machado, 800', '(49) 3322-2000'),
    (3, 'Next Drive Joaçaba', 'Joaçaba',             'SC', 'Rua XV de Novembro, 450',   '(49) 3522-3000');

-- Categorias
INSERT INTO categoria_veiculo (id, nome, descricao, valor_diaria, valor_hora_extra, taxa_devolucao_outra_loja, valor_cautela) VALUES
    (1, 'Econômico',     'Carros compactos para uso urbano',          120.00, 15.00,  80.00,  500.00),
    (2, 'Intermediário', 'Sedãs e hatches médios',                    170.00, 20.00, 100.00,  800.00),
    (3, 'SUV',           'Utilitários esportivos',                    260.00, 30.00, 150.00, 1500.00),
    (4, 'Executivo',     'Sedãs de luxo para viagens a trabalho',     380.00, 45.00, 200.00, 2500.00);

-- Veículos (estado em 09/11/2026)
INSERT INTO veiculo (id, placa, marca, modelo, ano, cor, categoria_id, loja_atual_id, quilometragem, capacidade_tanque_litros, status) VALUES
    ( 1, 'ABC1D23', 'Fiat',       'Mobi',              2024, 'Branco',  1, 1, 12480, 47, 'DISPONIVEL'),
    ( 2, 'BCD2E34', 'Volkswagen', 'Gol',               2023, 'Prata',   1, 2, 30210, 55, 'DISPONIVEL'),
    ( 3, 'CDE3F45', 'Chevrolet',  'Onix',              2025, 'Preto',   2, 1, 20640, 44, 'LOCADO'),
    ( 4, 'DEF4G56', 'Hyundai',    'HB20',              2024, 'Cinza',   2, 2, 18700, 50, 'DISPONIVEL'),
    ( 5, 'EFG5H67', 'Fiat',       'Cronos',            2023, 'Branco',  2, 3, 41000, 48, 'MANUTENCAO'),
    ( 6, 'FGH6I78', 'Jeep',       'Compass',           2025, 'Azul',    3, 1,  8900, 60, 'DISPONIVEL'),
    ( 7, 'GHI7J89', 'Toyota',     'Corolla Cross',     2025, 'Prata',   3, 2,  5000, 47, 'LOCADO'),
    ( 8, 'HIJ8K90', 'Honda',      'HR-V',              2024, 'Vermelho',3, 3, 22100, 40, 'DISPONIVEL'),
    ( 9, 'IJK9L01', 'Toyota',     'Corolla',           2025, 'Preto',   4, 1,  9800, 50, 'DISPONIVEL'),
    (10, 'JKL0M12', 'Renault',    'Kwid',              2024, 'Laranja', 1, 3, 14500, 38, 'DISPONIVEL');

-- Pessoas: funcionários (1,2,3,10) e clientes (4 a 9)
INSERT INTO pessoa (id, nome, email, senha_hash, telefone) VALUES
    ( 1, 'Marcos Almeida',          'marcos.almeida@nextdrive.example',   crypt('Senha@123', gen_salt('bf')), '(49) 99111-0001'),
    ( 2, 'Juliana Prado',           'juliana.prado@nextdrive.example',    crypt('Senha@123', gen_salt('bf')), '(49) 99111-0002'),
    ( 3, 'Rafael Souza',            'rafael.souza@nextdrive.example',     crypt('Senha@123', gen_salt('bf')), '(49) 99111-0003'),
    (10, 'Luciana Ferreira',        'luciana.ferreira@nextdrive.example', crypt('Senha@123', gen_salt('bf')), '(49) 99111-0010'),
    ( 4, 'Ana Beatriz Lima',        'ana.lima@example.com',               crypt('Senha@123', gen_salt('bf')), '(49) 98801-0004'),
    ( 5, 'Carlos Eduardo Pereira',  'carlos.pereira@example.com',         crypt('Senha@123', gen_salt('bf')), '(49) 98801-0005'),
    ( 6, 'Fernanda Rocha',          'fernanda.rocha@example.com',         crypt('Senha@123', gen_salt('bf')), '(49) 98801-0006'),
    ( 7, 'Pedro Henrique Dias',     'pedro.dias@example.com',             crypt('Senha@123', gen_salt('bf')), '(49) 98801-0007'),
    ( 8, 'João Vitor Martins',      'joao.martins@example.com',           crypt('Senha@123', gen_salt('bf')), '(49) 98801-0008'),
    ( 9, 'Transportes Oeste',       'contato@transportesoeste.example',   crypt('Senha@123', gen_salt('bf')), '(49) 3622-9090');

INSERT INTO funcionario (pessoa_id, cpf, cargo, loja_id, data_admissao) VALUES
    ( 1, '90000000001', 'GERENTE',   1, '2022-03-01'),
    ( 2, '90000000002', 'ATENDENTE', 1, '2023-05-15'),
    ( 3, '90000000003', 'ATENDENTE', 2, '2024-01-10'),
    (10, '90000000010', 'ATENDENTE', 3, '2025-02-03');

INSERT INTO cliente (pessoa_id, tipo_cliente) VALUES
    (4,'PF'), (5,'PF'), (6,'PF'), (7,'PF'), (8,'PF'), (9,'PJ');

INSERT INTO cliente_pf (pessoa_id, cpf, data_nascimento, cnh_numero, cnh_data_emissao, cnh_validade) VALUES
    (4, '10000000004', '1992-03-14', '10000000004', '2012-05-20', '2030-05-20'),
    (5, '10000000005', '1985-07-02', '10000000005', '2005-09-10', '2029-09-10'),
    (6, '10000000006', '1998-11-25', '10000000006', '2018-02-01', '2028-02-01'),
    (7, '10000000007', '2003-08-09', '10000000007', '2022-01-15', '2031-01-15'),
    (8, '10000000008', '2006-12-01', '10000000008', '2025-06-10', '2035-06-10');   -- menor de 21 anos e CNH com menos de 2 anos (testa RF16)

INSERT INTO cliente_pj (pessoa_id, cnpj, razao_social, condutor_id) VALUES
    (9, '12345678000190', 'Transportes Oeste Ltda', 5);

-- Reservas
INSERT INTO reserva (id, cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id, data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, funcionario_id, status, data_criacao, data_cancelamento, justificativa_cancelamento) VALUES
    (1, 4, 1, 1, 1, '2026-09-05 09:00', '2026-09-08 09:00',  360.00, 'SITE',   NULL, 'EFETIVADA', '2026-09-01 10:00', NULL, NULL),
    (2, 5, 3, 2, 1, '2026-09-18 10:00', '2026-09-21 10:00',  780.00, 'BALCAO', 3,    'EFETIVADA', '2026-09-18 09:30', NULL, NULL),
    (3, 6, 2, 1, 1, '2026-10-02 08:00', '2026-10-06 08:00',  680.00, 'SITE',   NULL, 'EFETIVADA', '2026-09-28 20:15', NULL, NULL),
    (4, 9, 2, 1, 1, '2026-11-06 08:00', '2026-11-12 08:00', 1020.00, 'BALCAO', 2,    'EFETIVADA', '2026-11-05 16:40', NULL, NULL),
    (5, 7, 3, 2, 2, '2026-11-07 14:00', '2026-11-11 14:00', 1040.00, 'SITE',   NULL, 'EFETIVADA', '2026-11-04 11:20', NULL, NULL),
    (6, 4, 4, 1, 1, '2026-11-20 09:00', '2026-11-23 09:00', 1140.00, 'SITE',   NULL, 'ATIVA',     '2026-11-08 19:05', NULL, NULL),
    (7, 6, 1, 2, 2, '2026-10-15 09:00', '2026-10-18 09:00',  360.00, 'SITE',   NULL, 'CANCELADA', '2026-10-08 08:00', '2026-10-10 15:30', 'Mudança de planos de viagem'),
    (8, 7, 2, 1, 1, '2026-11-28 10:00', '2026-12-01 10:00',  510.00, 'BALCAO', 2,    'ATIVA',     '2026-11-09 09:15', NULL, NULL),
    (9, 6, 1, 3, 3, '2026-10-20 10:00', '2026-10-23 10:00',  360.00, 'BALCAO', 10,   'EFETIVADA', '2026-10-19 17:00', NULL, NULL);

-- Locações (quatro encerradas/pagas e duas abertas)
INSERT INTO locacao (id, reserva_id, veiculo_id, condutor_id, funcionario_retirada_id, funcionario_devolucao_id,
                     loja_retirada_id, loja_devolucao_id, data_retirada, data_devolucao_prevista, data_devolucao_real,
                     km_saida, km_chegada, combustivel_saida, combustivel_chegada, cautela_valor, cautela_status,
                     valor_diaria_contratada, valor_diarias, valor_horas_extras, valor_combustivel,
                     taxa_devolucao_outra_loja, desconto, valor_final, status) VALUES
    (1, 1,  1, 4, 2, 2, 1, 1, '2026-09-05 09:10', '2026-09-08 09:00', '2026-09-08 08:50',
        12000, 12480,  90,  75,  500.00, 'LIBERADA', 120.00,  360.00,   0.00,  43.71,   0.00,  0.00,  403.71, 'PAGA'),
    (2, 2,  6, 5, 3, 2, 2, 1, '2026-09-18 10:00', '2026-09-21 10:00', '2026-09-21 13:30',
         8000,  8900,  80,  60, 1500.00, 'LIBERADA', 260.00,  780.00, 120.00,  74.40, 150.00, 50.00, 1074.40, 'PAGA'),
    (3, 3,  3, 6, 2, 2, 1, 1, '2026-10-02 08:05', '2026-10-06 08:00', '2026-10-06 07:50',
        20000, 20640, 100,  50,  800.00, 'LIBERADA', 170.00,  680.00,   0.00, 136.40,   0.00,  0.00,  816.40, 'PAGA'),
    (6, 9, 10, 6, 10, 10, 3, 3, '2026-10-20 10:00', '2026-10-23 10:00', '2026-10-23 09:40',
        14000, 14500, 100,  80,  500.00, 'RETIDA',   120.00,  360.00,   0.00,  47.12,   0.00,  0.00,  407.12, 'PAGA');

INSERT INTO locacao (id, reserva_id, veiculo_id, condutor_id, funcionario_retirada_id, loja_retirada_id,
                     data_retirada, data_devolucao_prevista, km_saida, combustivel_saida,
                     cautela_valor, valor_diaria_contratada, status) VALUES
    (4, 4, 3, 5, 2, 1, '2026-11-06 08:20', '2026-11-12 08:00', 20640, 100,  800.00, 170.00, 'ABERTA'),
    (5, 5, 7, 7, 3, 2, '2026-11-07 14:15', '2026-11-11 14:00',  5000, 100, 1500.00, 260.00, 'ABERTA');

-- Pagamentos (superclasse + subclasses)
INSERT INTO pagamento (id, locacao_id, valor, forma, data_pagamento, funcionario_id) VALUES
    (1, 1,  403.71, 'CARTAO_DEBITO',  '2026-09-08 09:15', 2),
    (2, 2, 1074.40, 'CARTAO_CREDITO', '2026-09-21 14:00', 2),
    (3, 3,  400.00, 'PIX',            '2026-10-06 08:10', 2),
    (4, 3,  416.40, 'CARTAO_CREDITO', '2026-10-06 08:12', 2),
    (5, 6,  407.12, 'PIX',            '2026-10-23 09:55', 10);

INSERT INTO pagamento_cartao (pagamento_id, forma, bandeira, final_cartao, parcelas) VALUES
    (1, 'CARTAO_DEBITO',  'Visa',       '4321', 1),
    (2, 'CARTAO_CREDITO', 'Mastercard', '8765', 3),
    (4, 'CARTAO_CREDITO', 'Elo',        '1111', 2);

INSERT INTO pagamento_pix (pagamento_id, txid, chave_pix) VALUES
    (3, 'E0000000020261006081000001', 'pagamentos@nextdrive.example'),
    (5, 'E0000000020261023095500002', 'pagamentos@nextdrive.example');

-- Multas
INSERT INTO multa (id, locacao_id, descricao, valor, data_infracao, data_registro, funcionario_id, status) VALUES
    (1, 2, 'Excesso de velocidade (até 20%)', 195.23, '2026-09-19 14:20', '2026-10-02 09:00', 2,  'PAGA'),
    (2, 6, 'Estacionamento em local proibido', 130.16, '2026-10-21 16:05', '2026-11-03 10:00', 10, 'PENDENTE');

-- Manutenções
INSERT INTO manutencao (id, veiculo_id, funcionario_id, tipo, descricao, data_entrada, data_saida, custo, status) VALUES
    (1, 9, 1, 'PREVENTIVA', 'Revisão dos 10.000 km',          '2026-09-10', '2026-09-11', 450.00, 'CONCLUIDA'),
    (2, 6, 1, 'PREVENTIVA', 'Troca de óleo e pastilhas',      '2026-10-12', '2026-10-13', 650.00, 'CONCLUIDA'),
    (3, 5, 1, 'CORRETIVA',  'Troca de embreagem',             '2026-11-03', NULL,          NULL,   'EM_ANDAMENTO');

-- Ajusta as sequências das colunas IDENTITY após inserir ids explícitos
SELECT setval(pg_get_serial_sequence('loja','id'),              (SELECT MAX(id) FROM loja));
SELECT setval(pg_get_serial_sequence('categoria_veiculo','id'), (SELECT MAX(id) FROM categoria_veiculo));
SELECT setval(pg_get_serial_sequence('veiculo','id'),           (SELECT MAX(id) FROM veiculo));
SELECT setval(pg_get_serial_sequence('pessoa','id'),            (SELECT MAX(id) FROM pessoa));
SELECT setval(pg_get_serial_sequence('reserva','id'),           (SELECT MAX(id) FROM reserva));
SELECT setval(pg_get_serial_sequence('locacao','id'),           (SELECT MAX(id) FROM locacao));
SELECT setval(pg_get_serial_sequence('pagamento','id'),         (SELECT MAX(id) FROM pagamento));
SELECT setval(pg_get_serial_sequence('multa','id'),             (SELECT MAX(id) FROM multa));
SELECT setval(pg_get_serial_sequence('manutencao','id'),        (SELECT MAX(id) FROM manutencao));
