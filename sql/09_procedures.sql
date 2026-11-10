-- =====================================================================
-- Next Drive - 09_procedures.sql : stored procedures e função de login
-- Executar:  psql -U postgres -d next_drive -f sql/09_procedures.sql
-- Requer PostgreSQL 11 ou superior (CREATE PROCEDURE / CALL).
--
-- Procedures:
--   sp_cancelar_reserva      RF08  cancela reserva com justificativa
--   sp_abrir_locacao         RF09  registra a retirada (abre a locação)
--   sp_registrar_devolucao   RF10  registra a devolução e calcula o valor (RN06, RN07, RN08)
--   sp_registrar_pagamento   RF11  registra pagamento e quita a locação (RN12)
-- Função:
--   fn_autenticar            RF02  login por e-mail e senha (bcrypt)
-- =====================================================================

-- ---------------------------------------------------------------------
-- RF08: cancelamento de reserva com justificativa
--   CALL sp_cancelar_reserva(8, 'Cliente desistiu da viagem');
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_cancelar_reserva(
    p_reserva_id    INTEGER,
    p_justificativa TEXT)
LANGUAGE plpgsql AS $$
DECLARE
    v_status reserva.status%TYPE;
BEGIN
    IF p_justificativa IS NULL OR btrim(p_justificativa) = '' THEN
        RAISE EXCEPTION 'RF08: a justificativa do cancelamento é obrigatória';
    END IF;

    SELECT status INTO v_status FROM reserva WHERE id = p_reserva_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Reserva % não encontrada', p_reserva_id;
    END IF;
    IF v_status <> 'ATIVA' THEN
        RAISE EXCEPTION 'Somente reservas ativas podem ser canceladas (situação atual: %)', v_status;
    END IF;

    UPDATE reserva
       SET status                     = 'CANCELADA',
           data_cancelamento          = LOCALTIMESTAMP,
           justificativa_cancelamento = btrim(p_justificativa)
     WHERE id = p_reserva_id;
END;
$$;

-- ---------------------------------------------------------------------
-- RF09: retirada do veículo (abertura da locação)
--   CALL sp_abrir_locacao(6, 9, 4, 2, 9800, 100::smallint);
-- As regras de conflito, veículo disponível e condutor são validadas
-- pelos triggers (script 08).
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_abrir_locacao(
    p_reserva_id         INTEGER,
    p_veiculo_id         INTEGER,
    p_condutor_id        INTEGER,
    p_funcionario_id     INTEGER,
    p_km_saida           INTEGER,
    p_combustivel_saida  SMALLINT,
    p_data_retirada      TIMESTAMP DEFAULT LOCALTIMESTAMP)
LANGUAGE plpgsql AS $$
DECLARE
    r reserva%ROWTYPE;
    v veiculo%ROWTYPE;
    c categoria_veiculo%ROWTYPE;
BEGIN
    SELECT * INTO r FROM reserva WHERE id = p_reserva_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Reserva % não encontrada', p_reserva_id;
    END IF;
    IF r.status <> 'ATIVA' THEN
        RAISE EXCEPTION 'A reserva % não está ativa (situação atual: %)', p_reserva_id, r.status;
    END IF;

    SELECT * INTO v FROM veiculo WHERE id = p_veiculo_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Veículo % não encontrado', p_veiculo_id;
    END IF;
    IF v.categoria_id <> r.categoria_id THEN
        RAISE EXCEPTION 'O veículo % não pertence à categoria reservada', p_veiculo_id;
    END IF;
    IF v.loja_atual_id <> r.loja_retirada_id THEN
        RAISE EXCEPTION 'O veículo % não está na loja de retirada da reserva', p_veiculo_id;
    END IF;
    IF p_km_saida < v.quilometragem THEN
        RAISE EXCEPTION 'Quilometragem de saída (%) menor que a do veículo (%)', p_km_saida, v.quilometragem;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM funcionario WHERE pessoa_id = p_funcionario_id AND ativo) THEN
        RAISE EXCEPTION 'Funcionário % não encontrado ou inativo', p_funcionario_id;
    END IF;

    SELECT * INTO c FROM categoria_veiculo WHERE id = r.categoria_id;

    INSERT INTO locacao (reserva_id, veiculo_id, condutor_id, funcionario_retirada_id, loja_retirada_id,
                         data_retirada, data_devolucao_prevista, km_saida, combustivel_saida,
                         cautela_valor, valor_diaria_contratada)
    VALUES (p_reserva_id, p_veiculo_id, p_condutor_id, p_funcionario_id, r.loja_retirada_id,
            p_data_retirada, r.data_devolucao_prevista, p_km_saida, p_combustivel_saida,
            c.valor_cautela, c.valor_diaria);

    UPDATE reserva SET status = 'EFETIVADA' WHERE id = p_reserva_id;
END;
$$;

-- ---------------------------------------------------------------------
-- RF10: devolução do veículo com cálculo do valor final
--   CALL sp_registrar_devolucao(4, 21500, 60::smallint, 1, 2);
--
-- Regras de cálculo:
--  * diárias (RN07): períodos de 24 h entre retirada e devolução prevista,
--    mínimo de 1, pelo valor da diária contratada na retirada;
--  * atraso (RN08): cada dia completo de atraso cobra uma diária; as horas
--    restantes cobram a hora extra da categoria, limitadas a uma diária;
--  * combustível: se o nível de chegada for menor que o de saída, cobra a
--    diferença (% do tanque x capacidade x preço do litro em parametro_sistema);
--  * taxa de devolução em outra loja, quando a loja de devolução difere da retirada;
--  * desconto informado pelo atendente (não pode superar o total).
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_registrar_devolucao(
    p_locacao_id           INTEGER,
    p_km_chegada           INTEGER,
    p_combustivel_chegada  SMALLINT,
    p_loja_devolucao_id    INTEGER,
    p_funcionario_id       INTEGER,
    p_desconto             NUMERIC DEFAULT 0,
    p_data_devolucao_real  TIMESTAMP DEFAULT LOCALTIMESTAMP)
LANGUAGE plpgsql AS $$
DECLARE
    l              locacao%ROWTYPE;
    v              veiculo%ROWTYPE;
    c              categoria_veiculo%ROWTYPE;
    v_preco_litro  NUMERIC;
    v_dias         INTEGER;
    v_exc_seg      NUMERIC;
    v_dias_extra   INTEGER;
    v_horas_extra  INTEGER;
    v_diarias      NUMERIC(10,2);
    v_extras       NUMERIC(10,2) := 0;
    v_combustivel  NUMERIC(10,2) := 0;
    v_taxa         NUMERIC(10,2) := 0;
    v_subtotal     NUMERIC(10,2);
BEGIN
    SELECT * INTO l FROM locacao WHERE id = p_locacao_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Locação % não encontrada', p_locacao_id;
    END IF;
    IF l.status <> 'ABERTA' THEN
        RAISE EXCEPTION 'A locação % já foi encerrada', p_locacao_id;
    END IF;
    IF p_data_devolucao_real <= l.data_retirada THEN                       -- RN06
        RAISE EXCEPTION 'RN06: a devolução deve ser posterior à retirada';
    END IF;
    IF p_km_chegada < l.km_saida THEN
        RAISE EXCEPTION 'Quilometragem de chegada (%) menor que a de saída (%)', p_km_chegada, l.km_saida;
    END IF;
    IF p_desconto < 0 THEN
        RAISE EXCEPTION 'O desconto não pode ser negativo';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM funcionario WHERE pessoa_id = p_funcionario_id AND ativo) THEN
        RAISE EXCEPTION 'Funcionário % não encontrado ou inativo', p_funcionario_id;
    END IF;

    SELECT * INTO v FROM veiculo WHERE id = l.veiculo_id;
    SELECT * INTO c FROM categoria_veiculo WHERE id = v.categoria_id;
    SELECT valor INTO v_preco_litro FROM parametro_sistema WHERE chave = 'preco_litro_combustivel';

    -- diárias (RN07)
    v_dias := GREATEST(1, CEIL(EXTRACT(EPOCH FROM (l.data_devolucao_prevista - l.data_retirada)) / 86400.0))::INTEGER;
    v_diarias := v_dias * l.valor_diaria_contratada;

    -- atraso (RN08)
    IF p_data_devolucao_real > l.data_devolucao_prevista THEN
        v_exc_seg     := EXTRACT(EPOCH FROM (p_data_devolucao_real - l.data_devolucao_prevista));
        v_dias_extra  := FLOOR(v_exc_seg / 86400)::INTEGER;
        v_horas_extra := CEIL((v_exc_seg - v_dias_extra * 86400) / 3600.0)::INTEGER;
        v_extras := v_dias_extra * l.valor_diaria_contratada
                  + LEAST(v_horas_extra * c.valor_hora_extra, l.valor_diaria_contratada);
    END IF;

    -- diferença de combustível
    IF p_combustivel_chegada < l.combustivel_saida THEN
        v_combustivel := ROUND((l.combustivel_saida - p_combustivel_chegada) / 100.0
                               * v.capacidade_tanque_litros * v_preco_litro, 2);
    END IF;

    -- taxa de devolução em outra loja
    IF p_loja_devolucao_id <> l.loja_retirada_id THEN
        v_taxa := c.taxa_devolucao_outra_loja;
    END IF;

    v_subtotal := v_diarias + v_extras + v_combustivel + v_taxa;
    IF p_desconto > v_subtotal THEN
        RAISE EXCEPTION 'O desconto (%) não pode ser maior que o total da locação (%)', p_desconto, v_subtotal;
    END IF;

    UPDATE locacao
       SET data_devolucao_real       = p_data_devolucao_real,
           km_chegada                = p_km_chegada,
           combustivel_chegada       = p_combustivel_chegada,
           loja_devolucao_id         = p_loja_devolucao_id,
           funcionario_devolucao_id  = p_funcionario_id,
           valor_diarias             = v_diarias,
           valor_horas_extras        = v_extras,
           valor_combustivel         = v_combustivel,
           taxa_devolucao_outra_loja = v_taxa,
           desconto                  = p_desconto,
           valor_final               = v_subtotal - p_desconto,
           status                    = 'ENCERRADA'
     WHERE id = p_locacao_id;

    RAISE NOTICE 'Locação % encerrada. Valor final: R$ %', p_locacao_id, v_subtotal - p_desconto;
END;
$$;

-- ---------------------------------------------------------------------
-- RF11: registro de pagamento (cartão de crédito, débito ou PIX - RN12)
--   CALL sp_registrar_pagamento(4, 500.00, 'PIX', 2, p_txid => 'E123...');
--   CALL sp_registrar_pagamento(4, 300.00, 'CARTAO_CREDITO', 2, 'Visa', '1234', 2::smallint);
-- Quando o total pago iguala o valor final a locação passa a PAGA; a cautela
-- só é liberada se não houver multas pendentes.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_registrar_pagamento(
    p_locacao_id      INTEGER,
    p_valor           NUMERIC,
    p_forma           VARCHAR,
    p_funcionario_id  INTEGER,
    p_bandeira        VARCHAR  DEFAULT NULL,
    p_final_cartao    VARCHAR  DEFAULT NULL,
    p_parcelas        SMALLINT DEFAULT 1,
    p_txid            VARCHAR  DEFAULT NULL,
    p_chave_pix       VARCHAR  DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE
    l             locacao%ROWTYPE;
    v_pagamento   pagamento.id%TYPE;
    v_total_pago  NUMERIC;
BEGIN
    SELECT * INTO l FROM locacao WHERE id = p_locacao_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Locação % não encontrada', p_locacao_id;
    END IF;
    IF l.status <> 'ENCERRADA' THEN
        RAISE EXCEPTION 'Só é possível pagar locações encerradas (situação atual: %)', l.status;
    END IF;
    IF p_forma NOT IN ('CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX') THEN
        RAISE EXCEPTION 'RN12: forma de pagamento inválida (%). Use CARTAO_CREDITO, CARTAO_DEBITO ou PIX', p_forma;
    END IF;
    IF p_valor IS NULL OR p_valor <= 0 THEN
        RAISE EXCEPTION 'O valor do pagamento deve ser maior que zero';
    END IF;

    SELECT COALESCE(SUM(valor), 0) + p_valor INTO v_total_pago
      FROM pagamento WHERE locacao_id = p_locacao_id;
    IF v_total_pago > l.valor_final THEN
        RAISE EXCEPTION 'O pagamento ultrapassa o valor da locação (valor final: %, total com este pagamento: %)',
                        l.valor_final, v_total_pago;
    END IF;

    INSERT INTO pagamento (locacao_id, valor, forma, funcionario_id)
    VALUES (p_locacao_id, p_valor, p_forma, p_funcionario_id)
    RETURNING id INTO v_pagamento;

    IF p_forma = 'PIX' THEN
        IF p_txid IS NULL THEN
            RAISE EXCEPTION 'Pagamento PIX exige o identificador da transação (txid)';
        END IF;
        INSERT INTO pagamento_pix (pagamento_id, txid, chave_pix)
        VALUES (v_pagamento, p_txid, p_chave_pix);
    ELSE
        IF p_bandeira IS NULL OR p_final_cartao IS NULL THEN
            RAISE EXCEPTION 'Pagamento em cartão exige bandeira e os 4 últimos dígitos';
        END IF;
        INSERT INTO pagamento_cartao (pagamento_id, forma, bandeira, final_cartao, parcelas)
        VALUES (v_pagamento, p_forma, p_bandeira, p_final_cartao, p_parcelas);
    END IF;

    -- quitação: libera a cautela se não houver multas pendentes
    IF v_total_pago = l.valor_final THEN
        UPDATE locacao
           SET status = 'PAGA',
               cautela_status = CASE WHEN EXISTS (SELECT 1 FROM multa m
                                                   WHERE m.locacao_id = p_locacao_id
                                                     AND m.status = 'PENDENTE')
                                     THEN 'RETIDA' ELSE 'LIBERADA' END
         WHERE id = p_locacao_id;
    END IF;
END;
$$;

-- ---------------------------------------------------------------------
-- RF02: autenticação por e-mail e senha (senha guardada com bcrypt - RNF05)
--   SELECT * FROM fn_autenticar('juliana.prado@nextdrive.example', 'Senha@123');
-- Devolve uma linha se as credenciais forem válidas, ou nenhuma linha.
-- SECURITY DEFINER: os perfis da aplicação não leem a coluna senha_hash.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_autenticar(p_email VARCHAR, p_senha TEXT)
RETURNS TABLE (pessoa_id INTEGER, nome VARCHAR, perfil VARCHAR)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp AS $$
    SELECT p.id, p.nome,
           CASE WHEN f.pessoa_id IS NOT NULL THEN f.cargo::VARCHAR ELSE 'CLIENTE'::VARCHAR END
      FROM pessoa p
      LEFT JOIN funcionario f ON f.pessoa_id = p.id AND f.ativo
     WHERE lower(p.email) = lower(p_email)
       AND p.senha_hash = crypt(p_senha, p.senha_hash);
$$;

-- ---------------------------------------------------------------------
-- Permissões de execução (perfis criados no script 07)
-- ---------------------------------------------------------------------
REVOKE ALL ON ALL PROCEDURES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON FUNCTION fn_autenticar(VARCHAR, TEXT) FROM PUBLIC;
REVOKE ALL ON FUNCTION fn_veiculos_disponiveis(INTEGER, INTEGER, TIMESTAMP, TIMESTAMP) FROM PUBLIC;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'grp_gerente') THEN
        GRANT EXECUTE ON ALL PROCEDURES IN SCHEMA public TO grp_gerente, grp_atendente;
        GRANT EXECUTE ON FUNCTION fn_autenticar(VARCHAR, TEXT)
              TO grp_gerente, grp_atendente, grp_auditor, grp_site;
        GRANT EXECUTE ON FUNCTION fn_veiculos_disponiveis(INTEGER, INTEGER, TIMESTAMP, TIMESTAMP)
              TO grp_gerente, grp_atendente, grp_site;
    END IF;
END $$;
