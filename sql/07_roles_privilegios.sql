-- =====================================================================
-- Next Drive - 07_roles_privilegios.sql : usuários, grupos e privilégios (RF18)
-- Executar com superusuário, DEPOIS dos scripts 02 a 04 e ANTES do 08 e 09:
--   psql -U postgres -d next_drive -f sql/07_roles_privilegios.sql
-- Funciona com qualquer nome de banco (usa current_database()).
--
-- Grupos (roles sem login)   Usuário de exemplo   Perfil
--   grp_gerente              usr_gerente          Gerente da locadora
--   grp_atendente            usr_atendente        Atendente de balcão
--   grp_auditor              usr_auditor          Auditoria (somente leitura)
--   grp_site                 usr_site             Aplicação do site (clientes)
--
-- Regras gerais:
--   * ninguém recebe DELETE em tabelas (RNF03); locações são encerradas, nunca apagadas;
--   * a tabela auditoria é somente leitura para todos (RNF04) - só os triggers escrevem;
--   * a coluna pessoa.senha_hash nunca é legível (o login usa fn_autenticar);
--   * relatórios são acessados por views (o dono das views dá o acesso aos dados).
-- TROQUE as senhas de exemplo antes de usar fora de testes.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Grupos
-- ---------------------------------------------------------------------
DO $$
DECLARE
    r TEXT;
BEGIN
    FOREACH r IN ARRAY ARRAY['grp_gerente', 'grp_atendente', 'grp_auditor', 'grp_site'] LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = r) THEN
            EXECUTE format('CREATE ROLE %I NOLOGIN', r);
        END IF;
    END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- Usuários de exemplo, cada um membro de um grupo
-- ---------------------------------------------------------------------
DO $$
DECLARE
    u RECORD;
BEGIN
    FOR u IN SELECT * FROM (VALUES
                 ('usr_gerente',   'grp_gerente'),
                 ('usr_atendente', 'grp_atendente'),
                 ('usr_auditor',   'grp_auditor'),
                 ('usr_site',      'grp_site')) AS t(usuario, grupo)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = u.usuario) THEN
            EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L IN ROLE %I', u.usuario, 'Trocar@123', u.grupo);
        END IF;
    END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- Banco e esquema
-- ---------------------------------------------------------------------
DO $$
BEGIN
    EXECUTE format('REVOKE ALL ON DATABASE %I FROM PUBLIC', current_database());
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO grp_gerente, grp_atendente, grp_auditor, grp_site',
                   current_database());
END $$;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO grp_gerente, grp_atendente, grp_auditor, grp_site;

-- Parte de zero: nenhum privilégio herdado
REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM PUBLIC, grp_gerente, grp_atendente, grp_auditor, grp_site;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC, grp_gerente, grp_atendente, grp_auditor, grp_site;

-- ---------------------------------------------------------------------
-- GERENTE: cadastra e altera funcionários, frota, categorias, preços e lojas;
-- consulta relatórios. Sem DELETE.
-- ---------------------------------------------------------------------
GRANT SELECT, INSERT, UPDATE ON
      loja, categoria_veiculo, veiculo, funcionario, cliente, cliente_pf, cliente_pj,
      reserva, locacao, pagamento, pagamento_cartao, pagamento_pix, multa, manutencao,
      parametro_sistema
   TO grp_gerente;

GRANT SELECT (id, nome, email, telefone, data_cadastro) ON pessoa TO grp_gerente;
GRANT INSERT ON pessoa TO grp_gerente;
GRANT UPDATE (nome, email, senha_hash, telefone) ON pessoa TO grp_gerente;   -- cadastro de funcionários (RN10)
GRANT SELECT ON auditoria TO grp_gerente;
GRANT SELECT ON vw_disponibilidade_frota, vw_reservas_detalhadas, vw_locacoes_detalhadas,
                vw_saldo_locacao, vw_faturamento_mensal, vw_historico_cliente,
                vw_utilizacao_frota, vw_multas_pendentes
   TO grp_gerente;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO grp_gerente;

-- ---------------------------------------------------------------------
-- ATENDENTE: cadastra clientes e opera reservas, retiradas, devoluções,
-- pagamentos e multas. Não altera frota, preços nem funcionários.
-- ---------------------------------------------------------------------
GRANT SELECT ON loja, categoria_veiculo, veiculo, parametro_sistema TO grp_atendente;
GRANT SELECT (pessoa_id, cargo, loja_id, ativo) ON funcionario TO grp_atendente;
GRANT SELECT, INSERT, UPDATE ON
      cliente, cliente_pf, cliente_pj, reserva, locacao, pagamento, pagamento_cartao,
      pagamento_pix, multa
   TO grp_atendente;
GRANT SELECT (id, nome, email, telefone, data_cadastro) ON pessoa TO grp_atendente;
GRANT INSERT ON pessoa TO grp_atendente;
GRANT UPDATE (nome, email, telefone) ON pessoa TO grp_atendente;
GRANT SELECT ON vw_disponibilidade_frota, vw_reservas_detalhadas, vw_locacoes_detalhadas,
                vw_saldo_locacao, vw_historico_cliente, vw_multas_pendentes
   TO grp_atendente;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO grp_atendente;

-- ---------------------------------------------------------------------
-- AUDITOR: somente leitura da trilha de auditoria e dos relatórios
-- ---------------------------------------------------------------------
GRANT SELECT ON auditoria TO grp_auditor;
GRANT SELECT ON vw_disponibilidade_frota, vw_reservas_detalhadas, vw_locacoes_detalhadas,
                vw_saldo_locacao, vw_faturamento_mensal, vw_historico_cliente,
                vw_utilizacao_frota, vw_multas_pendentes
   TO grp_auditor;

-- ---------------------------------------------------------------------
-- SITE: cadastro do próprio cliente e criação de reservas (RF04, RF07)
-- ---------------------------------------------------------------------
GRANT SELECT ON loja, categoria_veiculo TO grp_site;
GRANT SELECT ON vw_disponibilidade_frota TO grp_site;
GRANT SELECT (id, nome, email, telefone, data_cadastro) ON pessoa TO grp_site;
GRANT INSERT ON pessoa, cliente, cliente_pf, cliente_pj, reserva TO grp_site;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO grp_site;

-- Conferência dos privilégios concedidos:
-- SELECT grantee, table_name, string_agg(privilege_type, ', ' ORDER BY privilege_type) AS privilegios
--   FROM information_schema.role_table_grants
--  WHERE grantee LIKE 'grp_%' AND table_schema = 'public'
--  GROUP BY grantee, table_name ORDER BY grantee, table_name;
