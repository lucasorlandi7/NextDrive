-- =====================================================================
-- Next Drive - 03_indexes.sql : índices (RF01)
-- Executar:  psql -U postgres -d next_drive -f sql/03_indexes.sql
--
-- Já existem índices únicos criados automaticamente pelas constraints
-- UNIQUE do script 02 e cobrem as colunas mais consultadas do RF01:
--   uq_veiculo_placa     -> placa do veículo       (RN02)
--   uq_cliente_pf_cpf    -> CPF do cliente PF       (RN01)
--   uq_cliente_pj_cnpj   -> CNPJ do cliente PJ      (RN01)
--   uq_pessoa_email      -> e-mail (login, RF02)
-- Abaixo ficam os índices adicionais: dados das reservas, chaves
-- estrangeiras usadas em junções e índices parciais para os filtros
-- mais frequentes.
-- =====================================================================

-- Reservas: busca por período e loja de retirada (consulta de disponibilidade)
CREATE INDEX idx_reserva_loja_periodo
    ON reserva (loja_retirada_id, data_retirada_prevista, data_devolucao_prevista);

CREATE INDEX idx_reserva_cliente    ON reserva (cliente_id);
CREATE INDEX idx_reserva_categoria  ON reserva (categoria_id);

-- Só reservas ativas participam da disponibilidade
CREATE INDEX idx_reserva_ativas
    ON reserva (categoria_id, loja_retirada_id, data_retirada_prevista)
 WHERE status = 'ATIVA';

-- Veículos: filtro por loja, categoria e situação
CREATE INDEX idx_veiculo_disponibilidade
    ON veiculo (loja_atual_id, categoria_id, status);

-- Locações: conflito de períodos por veículo e histórico por condutor
CREATE INDEX idx_locacao_veiculo_periodo
    ON locacao (veiculo_id, data_retirada, data_devolucao_prevista);

CREATE INDEX idx_locacao_abertas
    ON locacao (veiculo_id)
 WHERE status = 'ABERTA';

CREATE INDEX idx_locacao_condutor   ON locacao (condutor_id);
CREATE INDEX idx_locacao_devolucao  ON locacao (data_devolucao_real);

-- Pagamentos, multas e manutenções
CREATE INDEX idx_pagamento_locacao  ON pagamento (locacao_id);
CREATE INDEX idx_pagamento_data     ON pagamento (data_pagamento);

CREATE INDEX idx_multa_locacao      ON multa (locacao_id);
CREATE INDEX idx_multa_pendentes    ON multa (locacao_id) WHERE status = 'PENDENTE';

CREATE INDEX idx_manutencao_veiculo ON manutencao (veiculo_id, data_entrada);

-- Funcionários por loja
CREATE INDEX idx_funcionario_loja   ON funcionario (loja_id);

-- Auditoria: consulta por tabela e período
CREATE INDEX idx_auditoria_tabela_data ON auditoria (tabela, data_hora);

-- Conferência dos índices das colunas do RF01
-- SELECT tablename, indexname FROM pg_indexes
--  WHERE schemaname = 'public' AND indexname IN
--        ('uq_veiculo_placa','uq_cliente_pf_cpf','uq_cliente_pj_cnpj','idx_reserva_loja_periodo');
