-- =====================================================================
-- Next Drive - 08_triggers.sql : gatilhos (triggers) de integridade e auditoria
-- Executar:  psql -U postgres -d next_drive -f sql/08_triggers.sql
--
-- Triggers implementados:
--   1. trg_locacao_conflito          RF15 / RN03 / RN04  impede locações conflitantes
--   2. trg_locacao_valida_condutor   RF16 / RN05 / RF04  idade >= 21, CNH >= 2 anos e válida
--   3. trg_locacao_atualiza_veiculo  RF15                mantém o status do veículo atualizado
--   4. trg_locacao_imutavel          RN09                locação finalizada não é alterada livremente
--   5. trg_auditoria_pagamento       RF17                auditoria de pagamentos
--   6. trg_auditoria_preco           RF17                auditoria de preços das categorias
--   7. trg_manutencao_status_veiculo RN04 / RN11         veículo em manutenção fica indisponível
--   8. trg_multa_libera_cautela      Processo de negócio libera a cautela quando a última multa é paga
--
-- Funções marcadas SECURITY DEFINER escrevem em tabelas que o perfil do usuário
-- não pode alterar diretamente (auditoria, status do veículo), mantendo a
-- trilha de auditoria imutável (RNF04).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Locações conflitantes e veículo indisponível (RF15, RN03, RN04)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_locacao_conflito() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    v_status veiculo.status%TYPE;
BEGIN
    IF NEW.status <> 'ABERTA' THEN
        RETURN NEW;
    END IF;

    -- bloqueia a linha do veículo para evitar duas retiradas simultâneas
    SELECT status INTO v_status FROM veiculo WHERE id = NEW.veiculo_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Veículo % não encontrado', NEW.veiculo_id;
    END IF;

    -- RN03: períodos conflitantes para o mesmo veículo
    IF EXISTS (SELECT 1
                 FROM locacao l
                WHERE l.veiculo_id = NEW.veiculo_id
                  AND l.status = 'ABERTA'
                  AND l.id IS DISTINCT FROM NEW.id
                  AND l.data_retirada < NEW.data_devolucao_prevista
                  AND NEW.data_retirada < l.data_devolucao_prevista) THEN
        RAISE EXCEPTION 'RN03: o veículo % já possui locação em período conflitante', NEW.veiculo_id
              USING ERRCODE = 'P0001';
    END IF;

    -- RN04: só é possível abrir locação de veículo disponível
    IF TG_OP = 'INSERT' AND v_status <> 'DISPONIVEL' THEN
        RAISE EXCEPTION 'RN04: o veículo % não está disponível (situação atual: %)', NEW.veiculo_id, v_status
              USING ERRCODE = 'P0001';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_locacao_conflito
    BEFORE INSERT OR UPDATE OF veiculo_id, data_retirada, data_devolucao_prevista, status
    ON locacao
    FOR EACH ROW EXECUTE FUNCTION fn_locacao_conflito();

-- ---------------------------------------------------------------------
-- 2. Validação do condutor (RF16, RN05, RF04)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_locacao_valida_condutor() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    v_cliente_id   reserva.cliente_id%TYPE;
    v_tipo         cliente.tipo_cliente%TYPE;
    v_condutor_pj  cliente_pj.condutor_id%TYPE;
    v_pf           cliente_pf%ROWTYPE;
    v_data         DATE := NEW.data_retirada::DATE;
    v_idade        INTEGER;
BEGIN
    SELECT cliente_id INTO v_cliente_id FROM reserva WHERE id = NEW.reserva_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Reserva % não encontrada', NEW.reserva_id;
    END IF;

    SELECT tipo_cliente INTO v_tipo FROM cliente WHERE pessoa_id = v_cliente_id;

    -- RF04: PF dirige o próprio carro; PJ usa o condutor responsável cadastrado
    IF v_tipo = 'PF' AND NEW.condutor_id <> v_cliente_id THEN
        RAISE EXCEPTION 'RF04: para cliente pessoa física o condutor deve ser o próprio cliente'
              USING ERRCODE = 'P0001';
    ELSIF v_tipo = 'PJ' THEN
        SELECT condutor_id INTO v_condutor_pj FROM cliente_pj WHERE pessoa_id = v_cliente_id;
        IF NEW.condutor_id <> v_condutor_pj THEN
            RAISE EXCEPTION 'RF04: o condutor deve ser o condutor responsável da pessoa jurídica'
                  USING ERRCODE = 'P0001';
        END IF;
    END IF;

    SELECT * INTO v_pf FROM cliente_pf WHERE pessoa_id = NEW.condutor_id;

    -- RF16: idade mínima de 21 anos
    v_idade := EXTRACT(YEAR FROM age(v_data, v_pf.data_nascimento))::INTEGER;
    IF v_idade < 21 THEN
        RAISE EXCEPTION 'RF16: condutor com menos de 21 anos (idade: %)', v_idade
              USING ERRCODE = 'P0001';
    END IF;

    -- RF16: CNH emitida há pelo menos 2 anos
    IF v_pf.cnh_data_emissao > (v_data - INTERVAL '2 years')::DATE THEN
        RAISE EXCEPTION 'RF16: CNH emitida há menos de 2 anos (emissão: %)', v_pf.cnh_data_emissao
              USING ERRCODE = 'P0001';
    END IF;

    -- RN05: CNH válida na retirada
    IF v_pf.cnh_validade < v_data THEN
        RAISE EXCEPTION 'RN05: CNH vencida em %', v_pf.cnh_validade
              USING ERRCODE = 'P0001';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_locacao_valida_condutor
    BEFORE INSERT ON locacao
    FOR EACH ROW EXECUTE FUNCTION fn_locacao_valida_condutor();

-- ---------------------------------------------------------------------
-- 3. Status do veículo acompanha o ciclo da locação (RF15)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_locacao_atualiza_veiculo() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.status = 'ABERTA' THEN
        UPDATE veiculo SET status = 'LOCADO' WHERE id = NEW.veiculo_id;

    ELSIF TG_OP = 'UPDATE' AND OLD.status = 'ABERTA' AND NEW.status = 'ENCERRADA' THEN
        -- devolução: veículo volta à frota na loja de devolução, com a nova quilometragem
        UPDATE veiculo
           SET status        = 'DISPONIVEL',
               quilometragem = NEW.km_chegada,
               loja_atual_id = NEW.loja_devolucao_id
         WHERE id = NEW.veiculo_id;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_locacao_atualiza_veiculo
    AFTER INSERT OR UPDATE OF status ON locacao
    FOR EACH ROW EXECUTE FUNCTION fn_locacao_atualiza_veiculo();

-- ---------------------------------------------------------------------
-- 4. Locação finalizada não pode ser alterada livremente (RN09)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_locacao_imutavel() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.status <> 'ABERTA' THEN
            RAISE EXCEPTION 'RN09: locação % já finalizada não pode ser excluída', OLD.id
                  USING ERRCODE = 'P0001';
        END IF;
        RETURN OLD;
    END IF;

    IF OLD.status = 'PAGA' THEN
        -- única mudança permitida: liberar a cautela depois de quitadas as multas
        IF (to_jsonb(NEW) - 'cautela_status') IS DISTINCT FROM (to_jsonb(OLD) - 'cautela_status')
           OR (OLD.cautela_status = 'LIBERADA' AND NEW.cautela_status <> 'LIBERADA') THEN
            RAISE EXCEPTION 'RN09: locação % está paga e não pode ser alterada', OLD.id
                  USING ERRCODE = 'P0001';
        END IF;

    ELSIF OLD.status = 'ENCERRADA' THEN
        -- permitido apenas quitar (status -> PAGA) e tratar a cautela
        IF NEW.status NOT IN ('ENCERRADA', 'PAGA')
           OR (to_jsonb(NEW) - 'status' - 'cautela_status')
              IS DISTINCT FROM (to_jsonb(OLD) - 'status' - 'cautela_status') THEN
            RAISE EXCEPTION 'RN09: locação % encerrada só pode ser alterada para paga', OLD.id
                  USING ERRCODE = 'P0001';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_locacao_imutavel
    BEFORE UPDATE OR DELETE ON locacao
    FOR EACH ROW EXECUTE FUNCTION fn_locacao_imutavel();

-- ---------------------------------------------------------------------
-- 5 e 6. Auditoria de pagamentos e de preços (RF17)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_auditar() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria (tabela, operacao, registro_id, dados_novos)
        VALUES (TG_TABLE_NAME, 'I', NEW.id, to_jsonb(NEW));

    ELSIF TG_OP = 'UPDATE' THEN
        IF to_jsonb(OLD) = to_jsonb(NEW) THEN
            RETURN NULL;                       -- nada mudou de fato
        END IF;
        INSERT INTO auditoria (tabela, operacao, registro_id, dados_antigos, dados_novos)
        VALUES (TG_TABLE_NAME, 'U', NEW.id, to_jsonb(OLD), to_jsonb(NEW));

    ELSE
        INSERT INTO auditoria (tabela, operacao, registro_id, dados_antigos)
        VALUES (TG_TABLE_NAME, 'D', OLD.id, to_jsonb(OLD));
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_auditoria_pagamento
    AFTER INSERT OR UPDATE OR DELETE ON pagamento
    FOR EACH ROW EXECUTE FUNCTION fn_auditar();

CREATE TRIGGER trg_auditoria_preco
    AFTER INSERT OR DELETE
       OR UPDATE OF valor_diaria, valor_hora_extra, taxa_devolucao_outra_loja, valor_cautela
    ON categoria_veiculo
    FOR EACH ROW EXECUTE FUNCTION fn_auditar();

-- ---------------------------------------------------------------------
-- 7. Manutenção torna o veículo indisponível (RN04, RN11)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_manutencao_status_veiculo() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    IF NEW.status = 'EM_ANDAMENTO' AND (TG_OP = 'INSERT' OR OLD.status <> 'EM_ANDAMENTO') THEN
        PERFORM 1 FROM veiculo WHERE id = NEW.veiculo_id AND status = 'LOCADO';
        IF FOUND THEN
            RAISE EXCEPTION 'RN04: veículo % está locado e não pode entrar em manutenção', NEW.veiculo_id
                  USING ERRCODE = 'P0001';
        END IF;
        UPDATE veiculo SET status = 'MANUTENCAO' WHERE id = NEW.veiculo_id;

    ELSIF TG_OP = 'UPDATE' AND OLD.status = 'EM_ANDAMENTO' AND NEW.status = 'CONCLUIDA' THEN
        UPDATE veiculo SET status = 'DISPONIVEL'
         WHERE id = NEW.veiculo_id AND status = 'MANUTENCAO';
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_manutencao_status_veiculo
    AFTER INSERT OR UPDATE OF status ON manutencao
    FOR EACH ROW EXECUTE FUNCTION fn_manutencao_status_veiculo();

-- ---------------------------------------------------------------------
-- 8. Cautela liberada quando a última multa pendente é paga
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_multa_libera_cautela() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    IF OLD.status = 'PENDENTE' AND NEW.status = 'PAGA'
       AND NOT EXISTS (SELECT 1 FROM multa m
                        WHERE m.locacao_id = NEW.locacao_id AND m.status = 'PENDENTE') THEN
        UPDATE locacao
           SET cautela_status = 'LIBERADA'
         WHERE id = NEW.locacao_id AND status = 'PAGA' AND cautela_status = 'RETIDA';
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_multa_libera_cautela
    AFTER UPDATE OF status ON multa
    FOR EACH ROW EXECUTE FUNCTION fn_multa_libera_cautela();

-- Funções de trigger não devem ser chamadas diretamente por usuários
REVOKE ALL ON FUNCTION fn_locacao_conflito(), fn_locacao_valida_condutor(), fn_locacao_atualiza_veiculo(),
                       fn_locacao_imutavel(), fn_auditar(), fn_manutencao_status_veiculo(),
                       fn_multa_libera_cautela() FROM PUBLIC;
