-- Next Drive - views (consultas salvas).

CREATE OR REPLACE VIEW vw_veiculos_disponiveis AS
SELECT v.id, v.placa, v.modelo, v.ano, c.nome AS categoria, c.valor_diaria
FROM veiculo v
JOIN categoria c ON c.id = v.categoria_id
WHERE v.status = 'DISPONIVEL';

CREATE OR REPLACE VIEW vw_locacoes_abertas AS
SELECT l.id AS locacao_id, cl.nome AS cliente, v.placa, v.modelo,
       l.data_retirada, r.data_devolucao_prevista
FROM locacao l
JOIN reserva r  ON r.id = l.reserva_id
JOIN cliente cl ON cl.id = r.cliente_id
JOIN veiculo v  ON v.id = l.veiculo_id
WHERE l.status = 'ABERTA';

CREATE OR REPLACE VIEW vw_faturamento_categoria AS
SELECT c.nome AS categoria, COUNT(p.id) AS pagamentos, COALESCE(SUM(p.valor), 0) AS total
FROM categoria c
LEFT JOIN veiculo v   ON v.categoria_id = c.id
LEFT JOIN locacao l   ON l.veiculo_id = v.id
LEFT JOIN pagamento p ON p.locacao_id = l.id
GROUP BY c.nome;
