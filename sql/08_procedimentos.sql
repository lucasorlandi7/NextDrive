-- Next Drive - função e procedures.

-- Calcula o valor final: diárias (mínimo 1) mais 30% da diária por dia de atraso.
CREATE OR REPLACE FUNCTION fn_valor_final(p_locacao_id INTEGER, p_devolucao DATE) RETURNS NUMERIC AS $$
DECLARE
    v_diaria    NUMERIC;
    v_retirada  DATE;
    v_prevista  DATE;
    v_dias      INTEGER;
    v_atraso    INTEGER;
BEGIN
    SELECT c.valor_diaria, l.data_retirada, r.data_devolucao_prevista
      INTO v_diaria, v_retirada, v_prevista
      FROM locacao l
      JOIN reserva r   ON r.id = l.reserva_id
      JOIN veiculo v   ON v.id = l.veiculo_id
      JOIN categoria c ON c.id = v.categoria_id
     WHERE l.id = p_locacao_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Locação % não encontrada.', p_locacao_id;
    END IF;
    v_dias   := GREATEST(1, p_devolucao - v_retirada);
    v_atraso := GREATEST(0, p_devolucao - v_prevista);
    RETURN ROUND(v_diaria * v_dias + v_diaria * 0.30 * v_atraso, 2);
END;
$$ LANGUAGE plpgsql;

-- Registra a devolução: guarda quilometragem e data, calcula o valor e encerra a locação.
CREATE OR REPLACE PROCEDURE sp_devolver_veiculo(p_locacao_id INTEGER, p_km INTEGER, p_devolucao DATE)
AS $$
DECLARE
    v_loc locacao%ROWTYPE;
BEGIN
    SELECT * INTO v_loc FROM locacao WHERE id = p_locacao_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Locação % não encontrada.', p_locacao_id;
    END IF;
    IF v_loc.status <> 'ABERTA' THEN
        RAISE EXCEPTION 'A locação % já foi encerrada.', p_locacao_id;
    END IF;
    IF p_km < v_loc.km_saida THEN
        RAISE EXCEPTION 'A quilometragem não pode ser menor que a da saída (%).', v_loc.km_saida;
    END IF;
    IF p_devolucao < v_loc.data_retirada THEN
        RAISE EXCEPTION 'A devolução não pode ser antes da retirada.';
    END IF;
    UPDATE locacao
       SET data_devolucao_real = p_devolucao,
           km_chegada = p_km,
           valor_final = fn_valor_final(p_locacao_id, p_devolucao),
           status = 'ENCERRADA'
     WHERE id = p_locacao_id;
END;
$$ LANGUAGE plpgsql;

-- Cancela uma reserva ativa, guardando a justificativa.
CREATE OR REPLACE PROCEDURE sp_cancelar_reserva(p_reserva_id INTEGER, p_justificativa VARCHAR)
AS $$
DECLARE
    v_status VARCHAR(10);
BEGIN
    SELECT status INTO v_status FROM reserva WHERE id = p_reserva_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Reserva % não encontrada.', p_reserva_id;
    END IF;
    IF v_status <> 'ATIVA' THEN
        RAISE EXCEPTION 'Só é possível cancelar reservas ativas (situação atual: %).', v_status;
    END IF;
    IF p_justificativa IS NULL OR length(trim(p_justificativa)) = 0 THEN
        RAISE EXCEPTION 'Informe a justificativa do cancelamento.';
    END IF;
    UPDATE reserva SET status = 'CANCELADA', justificativa_cancelamento = p_justificativa WHERE id = p_reserva_id;
END;
$$ LANGUAGE plpgsql;
