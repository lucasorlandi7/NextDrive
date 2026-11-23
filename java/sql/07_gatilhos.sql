-- Next Drive - gatilhos (triggers).

DROP TABLE IF EXISTS auditoria_pagamento;

CREATE TABLE auditoria_pagamento (
    id            SERIAL PRIMARY KEY,
    pagamento_id  INTEGER       NOT NULL,
    operacao      VARCHAR(6)    NOT NULL,
    valor_antigo  NUMERIC(10,2),
    valor_novo    NUMERIC(10,2),
    usuario       VARCHAR(60)   NOT NULL DEFAULT current_user,
    data_hora     TIMESTAMP     NOT NULL DEFAULT now()
);

-- 1. Não deixa alugar um carro que não está disponível ou que já tem locação aberta.
CREATE OR REPLACE FUNCTION fn_validar_locacao() RETURNS TRIGGER AS $$
DECLARE
    v_status VARCHAR(12);
BEGIN
    SELECT status INTO v_status FROM veiculo WHERE id = NEW.veiculo_id;
    IF v_status IS DISTINCT FROM 'DISPONIVEL' THEN
        RAISE EXCEPTION 'O veículo % não está disponível (situação: %).', NEW.veiculo_id, v_status;
    END IF;
    IF EXISTS (SELECT 1 FROM locacao WHERE veiculo_id = NEW.veiculo_id AND status = 'ABERTA') THEN
        RAISE EXCEPTION 'O veículo % já tem uma locação aberta.', NEW.veiculo_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_locacao ON locacao;
CREATE TRIGGER trg_validar_locacao
    BEFORE INSERT ON locacao
    FOR EACH ROW EXECUTE FUNCTION fn_validar_locacao();

-- 2. Quando a locação é encerrada, o carro volta a ficar disponível e recebe a quilometragem nova.
CREATE OR REPLACE FUNCTION fn_liberar_veiculo() RETURNS TRIGGER AS $$
BEGIN
    UPDATE veiculo
       SET status = 'DISPONIVEL',
           quilometragem = COALESCE(NEW.km_chegada, quilometragem)
     WHERE id = NEW.veiculo_id AND status = 'LOCADO';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_liberar_veiculo ON locacao;
CREATE TRIGGER trg_liberar_veiculo
    AFTER UPDATE OF status ON locacao
    FOR EACH ROW
    WHEN (OLD.status = 'ABERTA' AND NEW.status IN ('ENCERRADA', 'PAGA'))
    EXECUTE FUNCTION fn_liberar_veiculo();

-- 3. Não deixa fazer reserva para cliente com CNH que vence antes da devolução.
CREATE OR REPLACE FUNCTION fn_validar_cnh() RETURNS TRIGGER AS $$
DECLARE
    v_validade DATE;
BEGIN
    SELECT cnh_validade INTO v_validade FROM cliente WHERE id = NEW.cliente_id;
    IF v_validade < NEW.data_devolucao_prevista THEN
        RAISE EXCEPTION 'A CNH do cliente vence em %, antes da devolução.', v_validade;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_cnh ON reserva;
CREATE TRIGGER trg_validar_cnh
    BEFORE INSERT ON reserva
    FOR EACH ROW EXECUTE FUNCTION fn_validar_cnh();

-- 4. Auditoria: guarda toda inclusão, alteração e exclusão de pagamentos.
CREATE OR REPLACE FUNCTION fn_auditar_pagamento() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_pagamento (pagamento_id, operacao, valor_novo)
        VALUES (NEW.id, TG_OP, NEW.valor);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO auditoria_pagamento (pagamento_id, operacao, valor_antigo, valor_novo)
        VALUES (NEW.id, TG_OP, OLD.valor, NEW.valor);
        RETURN NEW;
    END IF;
    INSERT INTO auditoria_pagamento (pagamento_id, operacao, valor_antigo)
    VALUES (OLD.id, TG_OP, OLD.valor);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

DROP TRIGGER IF EXISTS trg_auditar_pagamento ON pagamento;
CREATE TRIGGER trg_auditar_pagamento
    AFTER INSERT OR UPDATE OR DELETE ON pagamento
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_pagamento();
