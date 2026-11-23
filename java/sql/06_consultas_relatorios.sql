-- Next Drive - consultas para relatórios.

SELECT status, COUNT(*) AS quantidade FROM veiculo GROUP BY status ORDER BY status;

SELECT * FROM vw_veiculos_disponiveis ORDER BY categoria, modelo;

SELECT * FROM vw_locacoes_abertas ORDER BY data_retirada;

SELECT * FROM vw_faturamento_categoria ORDER BY total DESC;

SELECT cl.nome, COUNT(l.id) AS locacoes, COALESCE(SUM(l.valor_final), 0) AS total_gasto
FROM cliente cl
LEFT JOIN reserva r ON r.cliente_id = cl.id
LEFT JOIN locacao l ON l.reserva_id = r.id
GROUP BY cl.nome
ORDER BY total_gasto DESC;

SELECT tipo, COUNT(*) AS pagamentos, SUM(valor) AS total FROM pagamento GROUP BY tipo;
