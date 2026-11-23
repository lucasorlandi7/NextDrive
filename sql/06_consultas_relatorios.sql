-- Next Drive - consultas dos relatórios.
-- Todas usam junções (JOIN) ou views. Rode depois dos dados de exemplo.

-- R01 - Situação da frota por categoria
SELECT c.nome AS categoria, v.status, COUNT(*) AS quantidade
FROM veiculo v
JOIN categoria c ON c.id = v.categoria_id
GROUP BY c.nome, v.status
ORDER BY c.nome, v.status;

-- R02 - Carros disponíveis para alugar (view vw_veiculos_disponiveis)
SELECT placa, modelo, ano, categoria, valor_diaria
FROM vw_veiculos_disponiveis
ORDER BY categoria, modelo;

-- R03 - Carros na rua: locações abertas (view vw_locacoes_abertas)
SELECT locacao_id, cliente, placa, modelo, data_retirada, data_devolucao_prevista
FROM vw_locacoes_abertas
ORDER BY data_retirada;

-- R04 - Faturamento por categoria (view vw_faturamento_categoria)
SELECT categoria, pagamentos, total
FROM vw_faturamento_categoria
ORDER BY total DESC;

-- R05 - Locações e total gasto por cliente
SELECT cl.nome AS cliente, COUNT(l.id) AS locacoes, COALESCE(SUM(l.valor_final), 0) AS total_gasto
FROM cliente cl
LEFT JOIN reserva r ON r.cliente_id = cl.id
LEFT JOIN locacao l ON l.reserva_id = r.id
GROUP BY cl.nome
ORDER BY total_gasto DESC;

-- R06 - Recebimentos por forma de pagamento e categoria
SELECT p.tipo AS forma, c.nome AS categoria, COUNT(*) AS pagamentos, SUM(p.valor) AS total
FROM pagamento p
JOIN locacao l   ON l.id = p.locacao_id
JOIN veiculo v   ON v.id = l.veiculo_id
JOIN categoria c ON c.id = v.categoria_id
GROUP BY p.tipo, c.nome
ORDER BY p.tipo, c.nome;

-- R07 - Saldo devedor das locações já devolvidas
SELECT l.id AS locacao, cl.nome AS cliente, l.valor_final,
       COALESCE(SUM(p.valor), 0) AS pago,
       l.valor_final - COALESCE(SUM(p.valor), 0) AS saldo
FROM locacao l
JOIN reserva r      ON r.id = l.reserva_id
JOIN cliente cl     ON cl.id = r.cliente_id
LEFT JOIN pagamento p ON p.locacao_id = l.id
WHERE l.status IN ('ENCERRADA', 'PAGA')
GROUP BY l.id, cl.nome, l.valor_final
ORDER BY saldo DESC;
