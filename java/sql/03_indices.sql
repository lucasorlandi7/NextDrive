-- Next Drive - índices.
-- A placa do veículo e o CPF do cliente já têm índice criado automaticamente pela restrição UNIQUE.
-- Aqui ficam os índices extras, para as buscas mais comuns do sistema.

CREATE INDEX IF NOT EXISTS idx_cliente_nome         ON cliente (nome);
CREATE INDEX IF NOT EXISTS idx_veiculo_status       ON veiculo (status);
CREATE INDEX IF NOT EXISTS idx_reserva_datas        ON reserva (data_retirada_prevista, data_devolucao_prevista);
CREATE INDEX IF NOT EXISTS idx_locacao_veiculo      ON locacao (veiculo_id, status);
CREATE INDEX IF NOT EXISTS idx_pagamento_locacao    ON pagamento (locacao_id);
