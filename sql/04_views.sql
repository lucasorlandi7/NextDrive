-- =====================================================================
-- Next Drive - 04_views.sql : visualizações (views) e função de apoio
-- Executar:  psql -U postgres -d next_drive -f sql/04_views.sql
-- Atende RF06 (disponibilidade), RF13 (histórico) e RF14 (relatórios).
-- =====================================================================

-- ---------------------------------------------------------------------
-- RF06: disponibilidade da frota por loja e categoria (situação atual)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_disponibilidade_frota AS
SELECT l.id                                                   AS loja_id,
       l.nome                                                 AS loja,
       c.id                                                   AS categoria_id,
       c.nome                                                 AS categoria,
       c.valor_diaria,
       COUNT(v.id)                                            AS total_veiculos,
       COUNT(v.id) FILTER (WHERE v.status = 'DISPONIVEL')     AS disponiveis,
       COUNT(v.id) FILTER (WHERE v.status = 'LOCADO')         AS locados,
       COUNT(v.id) FILTER (WHERE v.status = 'MANUTENCAO')     AS em_manutencao
  FROM loja l
  JOIN veiculo v            ON v.loja_atual_id = l.id
  JOIN categoria_veiculo c  ON c.id = v.categoria_id
 WHERE v.status <> 'INATIVO'
 GROUP BY l.id, l.nome, c.id, c.nome, c.valor_diaria;

COMMENT ON VIEW vw_disponibilidade_frota IS
    'RF06 - Veículos por loja e categoria, separados por situação. RN04/RN11: em manutenção não conta como disponível.';

-- ---------------------------------------------------------------------
-- RF06 (por período): veículos livres de locação e manutenção no intervalo
-- Uma view não recebe parâmetros; esta função complementa a view acima.
-- SECURITY DEFINER: lê locacao e manutencao em nome do chamador, que só precisa de EXECUTE
-- (o atendente e o site não têm SELECT em manutencao).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_veiculos_disponiveis(
    p_categoria_id INTEGER,
    p_loja_id      INTEGER,
    p_inicio       TIMESTAMP,
    p_fim          TIMESTAMP)
RETURNS TABLE (veiculo_id INTEGER, placa VARCHAR, marca VARCHAR, modelo VARCHAR, valor_diaria NUMERIC)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp AS $$
    SELECT v.id, v.placa, v.marca, v.modelo, c.valor_diaria
      FROM veiculo v
      JOIN categoria_veiculo c ON c.id = v.categoria_id
     WHERE v.categoria_id  = p_categoria_id
       AND v.loja_atual_id = p_loja_id
       AND v.status IN ('DISPONIVEL','LOCADO')            -- exclui MANUTENCAO e INATIVO
       AND NOT EXISTS (                                   -- sem locação aberta no período (RN03)
             SELECT 1 FROM locacao lo
              WHERE lo.veiculo_id = v.id
                AND lo.status = 'ABERTA'
                AND lo.data_retirada < p_fim
                AND p_inicio < lo.data_devolucao_prevista)
       AND NOT EXISTS (                                   -- sem manutenção no período (RN11)
             SELECT 1 FROM manutencao m
              WHERE m.veiculo_id = v.id
                AND m.status = 'EM_ANDAMENTO'
                AND m.data_entrada < p_fim::date)
     ORDER BY v.placa;
$$;

-- ---------------------------------------------------------------------
-- RF14: reservas com dados do cliente, categoria e lojas
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_reservas_detalhadas AS
SELECT r.id                       AS reserva_id,
       p.nome                     AS cliente,
       cl.tipo_cliente,
       COALESCE(pf.cpf, pj.cnpj)  AS documento,
       cat.nome                   AS categoria,
       lr.nome                    AS loja_retirada,
       ld.nome                    AS loja_devolucao,
       r.data_retirada_prevista,
       r.data_devolucao_prevista,
       r.valor_estimado,
       r.canal,
       f.nome                     AS atendente,
       r.status
  FROM reserva r
  JOIN cliente cl            ON cl.pessoa_id = r.cliente_id
  JOIN pessoa p              ON p.id = cl.pessoa_id
  LEFT JOIN cliente_pf pf    ON pf.pessoa_id = cl.pessoa_id
  LEFT JOIN cliente_pj pj    ON pj.pessoa_id = cl.pessoa_id
  JOIN categoria_veiculo cat ON cat.id = r.categoria_id
  JOIN loja lr               ON lr.id = r.loja_retirada_id
  JOIN loja ld               ON ld.id = r.loja_devolucao_id
  LEFT JOIN pessoa f         ON f.id = r.funcionario_id;

-- ---------------------------------------------------------------------
-- RF14: locações completas
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_locacoes_detalhadas AS
SELECT lo.id                      AS locacao_id,
       lo.reserva_id,
       pc.nome                    AS cliente,
       pcond.nome                 AS condutor,
       v.placa,
       v.marca || ' ' || v.modelo AS veiculo,
       cat.nome                   AS categoria,
       lr.nome                    AS loja_retirada,
       ld.nome                    AS loja_devolucao,
       lo.data_retirada,
       lo.data_devolucao_prevista,
       lo.data_devolucao_real,
       lo.km_chegada - lo.km_saida AS km_rodados,
       lo.valor_diarias,
       lo.valor_horas_extras,
       lo.valor_combustivel,
       lo.taxa_devolucao_outra_loja,
       lo.desconto,
       lo.valor_final,
       lo.cautela_valor,
       lo.cautela_status,
       lo.status
  FROM locacao lo
  JOIN reserva r             ON r.id = lo.reserva_id
  JOIN pessoa pc             ON pc.id = r.cliente_id
  JOIN pessoa pcond          ON pcond.id = lo.condutor_id
  JOIN veiculo v             ON v.id = lo.veiculo_id
  JOIN categoria_veiculo cat ON cat.id = v.categoria_id
  JOIN loja lr               ON lr.id = lo.loja_retirada_id
  LEFT JOIN loja ld          ON ld.id = lo.loja_devolucao_id;

-- ---------------------------------------------------------------------
-- RF14: saldo financeiro de cada locação encerrada (valor - pago)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_saldo_locacao AS
SELECT lo.id                                   AS locacao_id,
       pc.nome                                 AS cliente,
       lo.valor_final,
       COALESCE(SUM(pg.valor), 0)              AS total_pago,
       lo.valor_final - COALESCE(SUM(pg.valor), 0) AS saldo_devedor,
       lo.status
  FROM locacao lo
  JOIN reserva r        ON r.id = lo.reserva_id
  JOIN pessoa pc        ON pc.id = r.cliente_id
  LEFT JOIN pagamento pg ON pg.locacao_id = lo.id
 WHERE lo.status <> 'ABERTA'
 GROUP BY lo.id, pc.nome, lo.valor_final, lo.status;

-- ---------------------------------------------------------------------
-- RF14: faturamento mensal por forma de pagamento
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_faturamento_mensal AS
SELECT to_char(date_trunc('month', pg.data_pagamento), 'YYYY-MM')       AS mes,
       COUNT(*)                                                         AS qtd_pagamentos,
       SUM(pg.valor)                                                    AS total_recebido,
       SUM(pg.valor) FILTER (WHERE pg.forma = 'CARTAO_CREDITO')         AS total_credito,
       SUM(pg.valor) FILTER (WHERE pg.forma = 'CARTAO_DEBITO')          AS total_debito,
       SUM(pg.valor) FILTER (WHERE pg.forma = 'PIX')                    AS total_pix
  FROM pagamento pg
 GROUP BY date_trunc('month', pg.data_pagamento);

-- ---------------------------------------------------------------------
-- RF13: histórico de locações dos clientes
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_historico_cliente AS
SELECT r.cliente_id,
       pc.nome                    AS cliente,
       lo.id                      AS locacao_id,
       v.placa,
       v.marca || ' ' || v.modelo AS veiculo,
       lo.data_retirada,
       lo.data_devolucao_real,
       lo.valor_final,
       lo.status
  FROM locacao lo
  JOIN reserva r ON r.id = lo.reserva_id
  JOIN pessoa pc ON pc.id = r.cliente_id
  JOIN veiculo v ON v.id = lo.veiculo_id;

-- ---------------------------------------------------------------------
-- RF14: utilização da frota (locações, km rodados e receita por veículo)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_utilizacao_frota AS
SELECT v.id                                   AS veiculo_id,
       v.placa,
       v.marca || ' ' || v.modelo             AS veiculo,
       cat.nome                               AS categoria,
       l.nome                                 AS loja_atual,
       v.status,
       COUNT(lo.id)                           AS qtd_locacoes,
       COALESCE(SUM(lo.km_chegada - lo.km_saida), 0) AS km_rodados,
       COALESCE(SUM(lo.valor_final), 0)       AS receita_total
  FROM veiculo v
  JOIN categoria_veiculo cat ON cat.id = v.categoria_id
  JOIN loja l                ON l.id = v.loja_atual_id
  LEFT JOIN locacao lo       ON lo.veiculo_id = v.id AND lo.status <> 'ABERTA'
 GROUP BY v.id, v.placa, v.marca, v.modelo, cat.nome, l.nome, v.status;

-- ---------------------------------------------------------------------
-- RF24/RF14: multas pendentes de pagamento
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_multas_pendentes AS
SELECT m.id                       AS multa_id,
       pc.nome                    AS cliente,
       v.placa,
       m.descricao,
       m.data_infracao,
       m.valor,
       lo.id                      AS locacao_id,
       lo.cautela_status
  FROM multa m
  JOIN locacao lo ON lo.id = m.locacao_id
  JOIN reserva r  ON r.id = lo.reserva_id
  JOIN pessoa pc  ON pc.id = r.cliente_id
  JOIN veiculo v  ON v.id = lo.veiculo_id
 WHERE m.status = 'PENDENTE';
