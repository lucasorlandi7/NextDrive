-- =====================================================================
-- Next Drive - 11_testes_regras.sql : testes das regras de negócio
-- Executar (depois dos scripts 01 a 09):
--   psql -U postgres -d next_drive -f sql/11_testes_regras.sql
-- No DBeaver use "Executar script" (Alt+X) e abra a aba "Saída" para ler as
-- mensagens [OK] / [FALHA]. Tudo roda dentro de uma transação que é DESFEITA
-- no final (ROLLBACK), então os dados de exemplo não são alterados.
-- =====================================================================

BEGIN;

-- Auxiliares temporários (existem só nesta sessão)
CREATE FUNCTION pg_temp.teste(p_desc TEXT, p_ok BOOLEAN) RETURNS void
LANGUAGE plpgsql AS $fn$
BEGIN
    IF p_ok THEN RAISE NOTICE '[OK]    %', p_desc;
    ELSE         RAISE NOTICE '[FALHA] %', p_desc;
    END IF;
END;
$fn$;

CREATE FUNCTION pg_temp.espera_erro(p_desc TEXT, p_sql TEXT, p_trecho TEXT) RETURNS void
LANGUAGE plpgsql AS $fn$
BEGIN
    BEGIN
        EXECUTE p_sql;
        RAISE NOTICE '[FALHA] % (nenhum erro foi lançado)', p_desc;
    EXCEPTION WHEN OTHERS THEN
        IF position(p_trecho IN SQLERRM) > 0 THEN
            RAISE NOTICE '[OK]    % -> %', p_desc, SQLERRM;
        ELSE
            RAISE NOTICE '[FALHA] % -> erro inesperado: %', p_desc, SQLERRM;
        END IF;
    END;
END;
$fn$;

DO $t$
DECLARE
    v_res   INTEGER;
    v_loc   INTEGER;
    v_cli   INTEGER;
    v_val   NUMERIC;
    v_veic  veiculo%ROWTYPE;
    v_l     locacao%ROWTYPE;
BEGIN
    RAISE NOTICE '--- RF16 / RN05 / RF04: validação do condutor ---';

    -- cliente com 19 anos (João Vitor, id 8)
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, funcionario_id)
    VALUES (8, 1, 1, 1, '2026-12-10 09:00', '2026-12-12 09:00', 240, 'BALCAO', 2) RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RF16: condutor com menos de 21 anos é recusado',
        format('CALL sp_abrir_locacao(%s, 1, 8, 2, 12480, 100::smallint, TIMESTAMP ''2026-12-10 09:00'')', v_res),
        'RF16: condutor com menos de 21');

    -- cliente de 31 anos com CNH emitida há menos de 2 anos
    INSERT INTO pessoa (nome, email, senha_hash) VALUES ('Teste CNH Recente', 'teste.cnh1@example.com', crypt('x', gen_salt('bf'))) RETURNING id INTO v_cli;
    INSERT INTO cliente (pessoa_id, tipo_cliente) VALUES (v_cli, 'PF');
    INSERT INTO cliente_pf (pessoa_id, cpf, data_nascimento, cnh_numero, cnh_data_emissao, cnh_validade)
    VALUES (v_cli, '20000000001', '1995-01-01', '20000000001', '2026-01-10', '2036-01-10');
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, funcionario_id)
    VALUES (v_cli, 1, 1, 1, '2026-12-10 09:00', '2026-12-12 09:00', 240, 'BALCAO', 2) RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RF16: CNH emitida há menos de 2 anos é recusada',
        format('CALL sp_abrir_locacao(%s, 1, %s, 2, 12480, 100::smallint, TIMESTAMP ''2026-12-10 09:00'')', v_res, v_cli),
        'RF16: CNH emitida há menos de 2 anos');

    -- cliente com CNH vencida
    INSERT INTO pessoa (nome, email, senha_hash) VALUES ('Teste CNH Vencida', 'teste.cnh2@example.com', crypt('x', gen_salt('bf'))) RETURNING id INTO v_cli;
    INSERT INTO cliente (pessoa_id, tipo_cliente) VALUES (v_cli, 'PF');
    INSERT INTO cliente_pf (pessoa_id, cpf, data_nascimento, cnh_numero, cnh_data_emissao, cnh_validade)
    VALUES (v_cli, '20000000002', '1990-01-01', '20000000002', '2010-01-01', '2026-06-01');
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, funcionario_id)
    VALUES (v_cli, 1, 1, 1, '2026-12-10 09:00', '2026-12-12 09:00', 240, 'BALCAO', 2) RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RN05: CNH vencida é recusada',
        format('CALL sp_abrir_locacao(%s, 1, %s, 2, 12480, 100::smallint, TIMESTAMP ''2026-12-10 09:00'')', v_res, v_cli),
        'RN05: CNH vencida');

    -- cliente PF com condutor diferente do cliente
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal)
    VALUES (4, 1, 1, 1, '2026-12-10 09:00', '2026-12-12 09:00', 240, 'SITE') RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RF04: PF não pode indicar outro condutor',
        format('CALL sp_abrir_locacao(%s, 1, 5, 2, 12480, 100::smallint, TIMESTAMP ''2026-12-10 09:00'')', v_res),
        'RF04: para cliente pessoa física');

    RAISE NOTICE '--- RF15 / RN03 / RN04: conflito e disponibilidade do veículo ---';

    -- veículo 3 está locado (locação 4 aberta de 06/11 a 12/11)
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal)
    VALUES (4, 2, 1, 1, '2026-11-10 09:00', '2026-11-11 09:00', 170, 'SITE') RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RN03: veículo com locação em período conflitante é recusado',
        format('CALL sp_abrir_locacao(%s, 3, 4, 2, 20640, 100::smallint, TIMESTAMP ''2026-11-10 09:00'')', v_res),
        'RN03');

    -- veículo 5 está em manutenção
    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal)
    VALUES (4, 2, 3, 3, '2026-11-10 09:00', '2026-11-11 09:00', 170, 'SITE') RETURNING id INTO v_res;
    PERFORM pg_temp.espera_erro('RN04: veículo em manutenção não pode ser locado',
        format('CALL sp_abrir_locacao(%s, 5, 4, 10, 41000, 100::smallint, TIMESTAMP ''2026-11-10 09:00'')', v_res),
        'RN04');

    RAISE NOTICE '--- RF09 / RF10 / RF11: retirada, devolução e pagamento ---';

    INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id,
                         data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal)
    VALUES (4, 1, 1, 1, '2026-12-01 09:00', '2026-12-04 09:00', 360, 'SITE') RETURNING id INTO v_res;

    CALL sp_abrir_locacao(v_res, 1, 4, 2, 12480, 100::smallint, TIMESTAMP '2026-12-01 09:00');
    SELECT id INTO v_loc FROM locacao WHERE reserva_id = v_res;
    SELECT * INTO v_veic FROM veiculo WHERE id = 1;
    PERFORM pg_temp.teste('Retirada: veículo passa a LOCADO (RF15)', v_veic.status = 'LOCADO');
    PERFORM pg_temp.teste('Retirada: reserva passa a EFETIVADA',
        (SELECT status FROM reserva WHERE id = v_res) = 'EFETIVADA');
    PERFORM pg_temp.teste('Retirada: diária e cautela copiadas da categoria',
        (SELECT valor_diaria_contratada = 120 AND cautela_valor = 500 FROM locacao WHERE id = v_loc));

    -- devolve 3 h atrasado, em outra loja, tanque 100% -> 80%, desconto de 10
    -- esperado: 3 x 120 + 3 h x 15 + 9,4 L x 6,20 + 80 - 10 = 533,28
    CALL sp_registrar_devolucao(v_loc, 12980, 80::smallint, 2, 3, 10, TIMESTAMP '2026-12-04 12:00');
    SELECT * INTO v_l FROM locacao WHERE id = v_loc;
    PERFORM pg_temp.teste('Devolução: diárias = 360,00', v_l.valor_diarias = 360);
    PERFORM pg_temp.teste('Devolução: horas extras = 45,00 (RN08)', v_l.valor_horas_extras = 45);
    PERFORM pg_temp.teste('Devolução: combustível = 58,28', v_l.valor_combustivel = 58.28);
    PERFORM pg_temp.teste('Devolução: taxa de outra loja = 80,00', v_l.taxa_devolucao_outra_loja = 80);
    PERFORM pg_temp.teste('Devolução: valor final = 533,28 (RN07)', v_l.valor_final = 533.28);
    SELECT * INTO v_veic FROM veiculo WHERE id = 1;
    PERFORM pg_temp.teste('Devolução: veículo DISPONIVEL, na loja 2, com 12980 km',
        v_veic.status = 'DISPONIVEL' AND v_veic.loja_atual_id = 2 AND v_veic.quilometragem = 12980);

    -- RN09: locação encerrada não pode ter dados alterados
    PERFORM pg_temp.espera_erro('RN09: locação encerrada não aceita alteração de km',
        format('UPDATE locacao SET km_saida = 0 WHERE id = %s', v_loc), 'RN09');

    -- pagamentos
    CALL sp_registrar_pagamento(v_loc, 300, 'PIX', 3, p_txid => 'TESTE-PIX-0001');
    PERFORM pg_temp.teste('Pagamento parcial: locação continua ENCERRADA',
        (SELECT status FROM locacao WHERE id = v_loc) = 'ENCERRADA');
    PERFORM pg_temp.espera_erro('Pagamento acima do saldo é recusado',
        format('CALL sp_registrar_pagamento(%s, 300, ''PIX'', 3, p_txid => ''TESTE-PIX-0002'')', v_loc),
        'ultrapassa');
    PERFORM pg_temp.espera_erro('RN12: forma de pagamento inválida é recusada',
        format('CALL sp_registrar_pagamento(%s, 10, ''BOLETO'', 3)', v_loc), 'RN12');
    CALL sp_registrar_pagamento(v_loc, 233.28, 'CARTAO_CREDITO', 3, 'Visa', '1234', 2::smallint);
    SELECT * INTO v_l FROM locacao WHERE id = v_loc;
    PERFORM pg_temp.teste('Quitação: locação PAGA e cautela LIBERADA', v_l.status = 'PAGA' AND v_l.cautela_status = 'LIBERADA');
    PERFORM pg_temp.teste('RF17: 2 pagamentos auditados',
        (SELECT COUNT(*) FROM auditoria WHERE tabela = 'pagamento' AND operacao = 'I') = 2);
    PERFORM pg_temp.espera_erro('RN09: locação paga não pode ser excluída',
        format('DELETE FROM locacao WHERE id = %s', v_loc), 'RN09');

    RAISE NOTICE '--- RF08: cancelamento de reserva ---';
    PERFORM pg_temp.espera_erro('RF08: cancelamento sem justificativa é recusado',
        'CALL sp_cancelar_reserva(8, ''   '')', 'RF08');
    CALL sp_cancelar_reserva(8, 'Cliente desistiu da viagem');
    PERFORM pg_temp.teste('RF08: reserva 8 cancelada com justificativa',
        (SELECT status = 'CANCELADA' AND justificativa_cancelamento = 'Cliente desistiu da viagem'
           FROM reserva WHERE id = 8));
    PERFORM pg_temp.espera_erro('RF08: reserva já cancelada não cancela de novo',
        'CALL sp_cancelar_reserva(8, ''Outra tentativa'')', 'ativas');

    RAISE NOTICE '--- RF17: auditoria de preços ---';
    UPDATE categoria_veiculo SET valor_diaria = 125 WHERE id = 1;
    PERFORM pg_temp.teste('RF17: alteração de preço auditada com valor antigo e novo',
        (SELECT (dados_antigos->>'valor_diaria')::NUMERIC = 120 AND (dados_novos->>'valor_diaria')::NUMERIC = 125
           FROM auditoria WHERE tabela = 'categoria_veiculo' AND operacao = 'U' LIMIT 1));

    RAISE NOTICE '--- RN04 / RN11: manutenção ---';
    PERFORM pg_temp.espera_erro('RN04: veículo locado não entra em manutenção',
        'INSERT INTO manutencao (veiculo_id, funcionario_id, tipo, descricao, data_entrada) VALUES (3, 1, ''CORRETIVA'', ''Teste'', ''2026-11-09'')',
        'RN04');
    INSERT INTO manutencao (veiculo_id, funcionario_id, tipo, descricao, data_entrada)
    VALUES (8, 1, 'PREVENTIVA', 'Revisão', '2026-11-09') RETURNING id INTO v_val;
    PERFORM pg_temp.teste('Manutenção aberta: veículo 8 fica em MANUTENCAO',
        (SELECT status FROM veiculo WHERE id = 8) = 'MANUTENCAO');
    UPDATE manutencao SET status = 'CONCLUIDA', data_saida = '2026-11-10', custo = 300 WHERE id = v_val;
    PERFORM pg_temp.teste('Manutenção concluída: veículo 8 volta a DISPONIVEL',
        (SELECT status FROM veiculo WHERE id = 8) = 'DISPONIVEL');

    RAISE NOTICE '--- Multas e cautela ---';
    PERFORM pg_temp.teste('Locação 6 paga com multa pendente: cautela RETIDA',
        (SELECT cautela_status FROM locacao WHERE id = 6) = 'RETIDA');
    UPDATE multa SET status = 'PAGA' WHERE id = 2;
    PERFORM pg_temp.teste('Última multa paga: cautela da locação 6 LIBERADA',
        (SELECT cautela_status FROM locacao WHERE id = 6) = 'LIBERADA');

    RAISE NOTICE '--- RF02: login ---';
    PERFORM pg_temp.teste('Login de atendente com senha correta',
        (SELECT perfil FROM fn_autenticar('juliana.prado@nextdrive.example', 'Senha@123')) = 'ATENDENTE');
    PERFORM pg_temp.teste('Login de cliente com senha correta',
        (SELECT perfil FROM fn_autenticar('ana.lima@example.com', 'Senha@123')) = 'CLIENTE');
    PERFORM pg_temp.teste('Login com senha errada não retorna usuário',
        NOT EXISTS (SELECT 1 FROM fn_autenticar('juliana.prado@nextdrive.example', 'errada')));
END
$t$;

ROLLBACK;   -- desfaz todos os dados de teste
