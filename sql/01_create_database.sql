-- =====================================================================
-- Next Drive - Sistema de Aluguel de Veículos
-- 01_create_database.sql : criação do banco de dados
-- Executar conectado ao banco "postgres" com um usuário administrador:
--   psql -U postgres -f sql/01_create_database.sql
-- =====================================================================

-- Encerra conexões abertas para permitir recriar o banco do zero (RNF06)
SELECT pg_terminate_backend(pid)
  FROM pg_stat_activity
 WHERE datname = 'next_drive'
   AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS next_drive;

CREATE DATABASE next_drive
    ENCODING 'UTF8'
    TEMPLATE template0;

COMMENT ON DATABASE next_drive IS
    'Next Drive - sistema de gerenciamento de locadora de veículos (Unoesc 2026/02)';
