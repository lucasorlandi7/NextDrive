-- Next Drive - testes das regras do banco.
-- Rode por último. Mostra uma tabela com OK ou FALHOU para cada teste e desfaz tudo no final.

BEGIN;

CREATE TEMP TABLE resultado (teste VARCHAR(80), resultado VARCHAR(10));
GRANT ALL ON resultado TO PUBLIC;

INSERT INTO cliente (nome, cpf, email, cnh_numero, cnh_validade)
VALUES ('Cliente Teste', '00000000001', 'teste@teste.com', '00000000001', current_date + 1000);
INSERT INTO cliente (nome, cpf, email, cnh_numero, cnh_validade)
VALUES ('Cliente CNH Vencida', '00000000002', 'vencida@teste.com', '00000000002', current_date - 10);
INSERT INTO veiculo (placa, modelo, ano, quilometragem, categoria_id)
VALUES ('TST0A00', 'Carro de Teste', 2024, 1000, (SELECT id FROM categoria WHERE nome = 'Econômico'));
INSERT INTO veiculo (placa, modelo, ano, quilometragem, status, categoria_id)
VALUES ('TST0B00', 'Carro em Manutenção', 2024, 1000, 'MANUTENCAO', (SELECT id FROM categoria WHERE nome = 'Econômico'));

DO $$
DECLARE
    v_cli   INTEGER := (SELECT id FROM cliente WHERE cpf = '00000000001');
    v_venc  INTEGER := (SELECT id FROM cliente WHERE cpf = '00000000002');
    v_carro INTEGER := (SELECT id FROM veiculo WHERE placa = 'TST0A00');
    v_manut INTEGER := (SELECT id FROM veiculo WHERE placa = 'TST0B00');
    v_cat   INTEGER := (SELECT id FROM categoria WHERE nome = 'Econômico');
    v_res   INTEGER;
    v_loc   INTEGER;
    v_pag   INTEGER;
    v_valor NUMERIC;
    v_qtd   INTEGER;
BEGIN
    INSERT INTO resultado
    SELECT 'Índices extras foram criados', CASE WHEN COUNT(*) >= 5 THEN 'OK' ELSE 'FALHOU' END
    FROM pg_indexes WHERE schemaname = 'public' AND indexname LIKE 'idx\_%';

    INSERT INTO resultado
    SELECT 'View mostra só carros disponíveis',
           CASE WHEN (SELECT COUNT(*) FROM vw_veiculos_disponiveis) = (SELECT COUNT(*) FROM veiculo WHERE status = 'DISPONIVEL')
                 AND NOT EXISTS (SELECT 1 FROM vw_veiculos_disponiveis WHERE placa = 'TST0B00')
                THEN 'OK' ELSE 'FALHOU' END;

    BEGIN
        INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, data_devolucao_prevista)
        VALUES (v_venc, v_cat, current_date, current_date + 3);
        INSERT INTO resultado VALUES ('Bloqueia reserva com CNH vencida', 'FALHOU');
    EXCEPTION WHEN raise_exception THEN
        INSERT INTO resultado VALUES ('Bloqueia reserva com CNH vencida', 'OK');
    END;

    INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, data_devolucao_prevista, status)
    VALUES (v_cli, v_cat, current_date, current_date + 3, 'EFETIVADA') RETURNING id INTO v_res;

    BEGIN
        INSERT INTO locacao (reserva_id, veiculo_id, data_retirada, km_saida) VALUES (v_res, v_manut, current_date, 1000);
        INSERT INTO resultado VALUES ('Bloqueia alugar carro em manutenção', 'FALHOU');
    EXCEPTION WHEN raise_exception THEN
        INSERT INTO resultado VALUES ('Bloqueia alugar carro em manutenção', 'OK');
    END;

    INSERT INTO locacao (reserva_id, veiculo_id, data_retirada, km_saida) VALUES (v_res, v_carro, current_date, 1000) RETURNING id INTO v_loc;
    UPDATE veiculo SET status = 'LOCADO' WHERE id = v_carro;

    BEGIN
        INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, data_devolucao_prevista, status)
        VALUES (v_cli, v_cat, current_date, current_date + 2, 'EFETIVADA') RETURNING id INTO v_res;
        INSERT INTO locacao (reserva_id, veiculo_id, data_retirada, km_saida) VALUES (v_res, v_carro, current_date, 1000);
        INSERT INTO resultado VALUES ('Bloqueia alugar carro que já está alugado', 'FALHOU');
    EXCEPTION WHEN raise_exception THEN
        INSERT INTO resultado VALUES ('Bloqueia alugar carro que já está alugado', 'OK');
    END;

    BEGIN
        CALL sp_devolver_veiculo(v_loc, 500, current_date + 5);
        INSERT INTO resultado VALUES ('Bloqueia devolução com quilometragem menor', 'FALHOU');
    EXCEPTION WHEN raise_exception THEN
        INSERT INTO resultado VALUES ('Bloqueia devolução com quilometragem menor', 'OK');
    END;

    CALL sp_devolver_veiculo(v_loc, 1200, current_date + 5);
    SELECT valor_final INTO v_valor FROM locacao WHERE id = v_loc;
    INSERT INTO resultado VALUES ('Devolução calcula R$ 672,00 (5 dias, 2 de atraso)', CASE WHEN v_valor = 672.00 THEN 'OK' ELSE 'FALHOU' END);

    INSERT INTO resultado
    SELECT 'Carro volta a ficar disponível e recebe a quilometragem',
           CASE WHEN status = 'DISPONIVEL' AND quilometragem = 1200 THEN 'OK' ELSE 'FALHOU' END
    FROM veiculo WHERE id = v_carro;

    INSERT INTO pagamento (locacao_id, tipo, valor, bandeira) VALUES (v_loc, 'CARTAO', 672.00, 'Visa') RETURNING id INTO v_pag;
    UPDATE pagamento SET valor = 670.00 WHERE id = v_pag;
    SELECT COUNT(*) INTO v_qtd FROM auditoria_pagamento WHERE pagamento_id = v_pag;
    INSERT INTO resultado VALUES ('Auditoria registra inclusão e alteração do pagamento', CASE WHEN v_qtd = 2 THEN 'OK' ELSE 'FALHOU' END);

    INSERT INTO reserva (cliente_id, categoria_id, data_retirada_prevista, data_devolucao_prevista)
    VALUES (v_cli, v_cat, current_date + 10, current_date + 12) RETURNING id INTO v_res;
    CALL sp_cancelar_reserva(v_res, 'Cliente desistiu');
    INSERT INTO resultado
    SELECT 'Cancela reserva ativa com justificativa',
           CASE WHEN status = 'CANCELADA' AND justificativa_cancelamento = 'Cliente desistiu' THEN 'OK' ELSE 'FALHOU' END
    FROM reserva WHERE id = v_res;

    BEGIN
        CALL sp_cancelar_reserva(v_res, 'De novo');
        INSERT INTO resultado VALUES ('Não cancela reserva que já está cancelada', 'FALHOU');
    EXCEPTION WHEN raise_exception THEN
        INSERT INTO resultado VALUES ('Não cancela reserva que já está cancelada', 'OK');
    END;

    SET LOCAL ROLE atendente_nextdrive;
    BEGIN
        DELETE FROM cliente WHERE id = v_cli;
        INSERT INTO resultado VALUES ('Atendente não pode excluir cliente', 'FALHOU');
    EXCEPTION WHEN insufficient_privilege THEN
        INSERT INTO resultado VALUES ('Atendente não pode excluir cliente', 'OK');
    END;
    BEGIN
        INSERT INTO pagamento (locacao_id, tipo, valor, txid) VALUES (v_loc, 'PIX', 1.00, 'TX-TESTE');
        INSERT INTO resultado VALUES ('Atendente registra pagamento (auditoria funciona para ele)', 'OK');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO resultado VALUES ('Atendente registra pagamento (auditoria funciona para ele)', 'FALHOU');
    END;
    RESET ROLE;
END
$$;

SELECT * FROM resultado;

ROLLBACK;
