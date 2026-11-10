-- =====================================================================
-- Next Drive - 06_consultas_relatorios.sql : SELECTs dos relatórios
-- Cada consulta usa junções (JOIN) e/ou as views criadas no script 04 (RF14).
-- Executar:  psql -U postgres -d next_drive -f sql/06_consultas_relatorios.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- R01. Disponibilidade atual da frota por loja e categoria (RF06)
-- ---------------------------------------------------------------------
SELECT loja, categoria, valor_diaria, total_veiculos, disponiveis, locados, em_manutencao
  FROM vw_disponibilidade_frota
 ORDER BY loja, categoria;

-- ---------------------------------------------------------------------
-- R02. Veículos livres para um período (categoria SUV, loja Centro) (RF06)
-- ---------------------------------------------------------------------
SELECT *
  FROM fn_veiculos_disponiveis(3, 1, TIMESTAMP '2026-11-15 09:00', TIMESTAMP '2026-11-18 09:00');

-- ---------------------------------------------------------------------
-- R03. Reservas ativas (futuras), com cliente, categoria e lojas
-- ---------------------------------------------------------------------
SELECT reserva_id, cliente, tipo_cliente, categoria, loja_retirada, loja_devolucao,
       data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal
  FROM vw_reservas_detalhadas
 WHERE status = 'ATIVA'
 ORDER BY data_retirada_prevista;

-- ---------------------------------------------------------------------
-- R04. Locações em aberto e quantos dias faltam para a devolução prevista
--      (valor negativo = devolução atrasada)
-- ---------------------------------------------------------------------
SELECT locacao_id, cliente, condutor, placa, veiculo, loja_retirada,
       data_retirada, data_devolucao_prevista,
       ROUND(EXTRACT(EPOCH FROM (data_devolucao_prevista - CURRENT_TIMESTAMP)) / 86400.0, 1) AS dias_para_devolver
  FROM vw_locacoes_detalhadas
 WHERE status = 'ABERTA'
 ORDER BY data_devolucao_prevista;

-- ---------------------------------------------------------------------
-- R05. Faturamento mensal por forma de pagamento (RF14)
-- ---------------------------------------------------------------------
SELECT mes, qtd_pagamentos, total_recebido,
       COALESCE(total_credito, 0) AS credito,
       COALESCE(total_debito,  0) AS debito,
       COALESCE(total_pix,     0) AS pix
  FROM vw_faturamento_mensal
 ORDER BY mes;

-- ---------------------------------------------------------------------
-- R06. Locações encerradas com saldo devedor
-- ---------------------------------------------------------------------
SELECT locacao_id, cliente, valor_final, total_pago, saldo_devedor, status
  FROM vw_saldo_locacao
 WHERE saldo_devedor > 0
 ORDER BY saldo_devedor DESC;

-- ---------------------------------------------------------------------
-- R07. Histórico de locações de um cliente (RF13) - exemplo: cliente 6
-- ---------------------------------------------------------------------
SELECT locacao_id, placa, veiculo, data_retirada, data_devolucao_real, valor_final, status
  FROM vw_historico_cliente
 WHERE cliente_id = 6
 ORDER BY data_retirada DESC;

-- ---------------------------------------------------------------------
-- R08. Ranking de utilização da frota (locações, km rodados e receita)
-- ---------------------------------------------------------------------
SELECT placa, veiculo, categoria, loja_atual, qtd_locacoes, km_rodados, receita_total
  FROM vw_utilizacao_frota
 ORDER BY receita_total DESC, qtd_locacoes DESC;

-- ---------------------------------------------------------------------
-- R09. Receita por categoria de veículo (junção direta + agrupamento)
-- ---------------------------------------------------------------------
SELECT cat.nome                      AS categoria,
       COUNT(lo.id)                  AS locacoes,
       ROUND(AVG(lo.valor_final), 2) AS ticket_medio,
       SUM(lo.valor_final)           AS receita_total
  FROM locacao lo
  JOIN veiculo v             ON v.id = lo.veiculo_id
  JOIN categoria_veiculo cat ON cat.id = v.categoria_id
 WHERE lo.status <> 'ABERTA'
 GROUP BY cat.nome
 ORDER BY receita_total DESC;

-- ---------------------------------------------------------------------
-- R10. Multas pendentes de pagamento (RF24)
-- ---------------------------------------------------------------------
SELECT multa_id, cliente, placa, descricao, data_infracao, valor, cautela_status
  FROM vw_multas_pendentes
 ORDER BY data_infracao;

-- ---------------------------------------------------------------------
-- R11. Manutenções: veículo, tipo, responsável e custo
-- ---------------------------------------------------------------------
SELECT m.id AS manutencao_id, v.placa, v.marca || ' ' || v.modelo AS veiculo,
       m.tipo, m.descricao, m.data_entrada, m.data_saida,
       COALESCE(m.custo, 0) AS custo, m.status, p.nome AS registrado_por
  FROM manutencao m
  JOIN veiculo v    ON v.id = m.veiculo_id
  JOIN pessoa p     ON p.id = m.funcionario_id
 ORDER BY m.data_entrada DESC;

-- ---------------------------------------------------------------------
-- R12. Desempenho por loja de retirada (locações e receita)
-- ---------------------------------------------------------------------
SELECT l.nome                         AS loja,
       COUNT(lo.id)                   AS locacoes,
       COALESCE(SUM(lo.valor_final), 0) AS receita
  FROM loja l
  LEFT JOIN locacao lo ON lo.loja_retirada_id = l.id AND lo.status <> 'ABERTA'
 GROUP BY l.nome
 ORDER BY receita DESC;

-- ---------------------------------------------------------------------
-- R13. Trilha de auditoria de pagamentos e preços (RF17) - preenchida pelos triggers
-- ---------------------------------------------------------------------
SELECT id, tabela, operacao, registro_id, usuario_db, data_hora
  FROM auditoria
 ORDER BY data_hora DESC, id DESC
 LIMIT 50;
