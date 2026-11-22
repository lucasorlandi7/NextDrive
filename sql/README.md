# Scripts do banco de dados — Next Drive

Banco: **PostgreSQL 16**. Os scripts devem ser executados **nesta ordem**.

| Ordem | Script | O que faz |
|---|---|---|
| 1 | `01_create_database.sql` | Cria o banco `next_drive` (apaga e recria se já existir). |
| 2 | `02_create_tables.sql` | Extensão `pgcrypto`, 17 tabelas, chaves e *constraints*. |
| 3 | `03_indices.sql` | Índices. |
| 4 | `04_views.sql` | 8 views e a função `fn_veiculos_disponiveis`. |
| 5 | `05_dados_exemplo.sql` | Lojas, categorias, veículos, pessoas, reservas e locações de exemplo. |
| 6 | `06_consultas_relatorios.sql` | Relatórios R01 a R13 (somente consulta). |
| 7 | `07_roles_privilegios.sql` | Grupos e usuários do banco e seus privilégios. |
| 8 | `08_gatilhos.sql` | 8 *triggers*. |
| 9 | `09_procedimentos.sql` | *Procedures* `sp_*` e função de login `fn_autenticar`. |
| 10 | `10_backup_restauração.sh` | Backup e restauração (Linux, macOS ou Git Bash). |
| 11 | `11_testes_regras.sql` | 34 verificações das regras; roda em transação e desfaz tudo no final. |
