-- Next Drive - controle de acesso (grupos, usuários e privilégios).
-- Rode depois dos scripts de tabelas, gatilhos e procedimentos.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'grupo_atendente') THEN
        CREATE ROLE grupo_atendente NOLOGIN;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'grupo_gerente') THEN
        CREATE ROLE grupo_gerente NOLOGIN;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'atendente_nextdrive') THEN
        CREATE ROLE atendente_nextdrive LOGIN PASSWORD 'Atendente@123' IN ROLE grupo_atendente;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'gerente_nextdrive') THEN
        CREATE ROLE gerente_nextdrive LOGIN PASSWORD 'Gerente@123' IN ROLE grupo_gerente;
    END IF;
END
$$;

REVOKE ALL ON ALL TABLES IN SCHEMA public FROM grupo_atendente, grupo_gerente;
GRANT USAGE ON SCHEMA public TO grupo_atendente, grupo_gerente;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO grupo_atendente, grupo_gerente;

-- Atendente: consulta tudo (menos a auditoria), cadastra e altera, mas não exclui nada.
GRANT SELECT ON categoria, cliente, veiculo, reserva, locacao, pagamento TO grupo_atendente;
GRANT INSERT, UPDATE ON cliente, reserva, locacao, pagamento TO grupo_atendente;
GRANT UPDATE ON veiculo TO grupo_atendente;

-- Gerente: tudo do atendente, mais gerenciar categorias e carros, excluir cadastros e ver a auditoria.
GRANT SELECT, INSERT, UPDATE, DELETE ON categoria, cliente, veiculo, reserva TO grupo_gerente;
GRANT SELECT, INSERT, UPDATE ON locacao, pagamento TO grupo_gerente;
GRANT SELECT ON auditoria_pagamento TO grupo_gerente;

-- Views dos relatórios: o atendente vê os carros disponíveis e as locações abertas;
-- o faturamento fica só para o gerente.
GRANT SELECT ON vw_veiculos_disponiveis, vw_locacoes_abertas TO grupo_atendente;
GRANT SELECT ON vw_veiculos_disponiveis, vw_locacoes_abertas, vw_faturamento_categoria TO grupo_gerente;
