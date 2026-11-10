# Scripts do banco de dados — Next Drive

Banco: **PostgreSQL 16** (funciona do 11 em diante). Os scripts devem ser executados **nesta ordem**.

| Ordem | Script | O que faz |
|---|---|---|
| 1 | `01_create_database.sql` | Cria o banco `next_drive` (apaga e recria se já existir). |
| 2 | `02_create_tables.sql` | Extensão `pgcrypto`, 17 tabelas, chaves e *constraints*. |
| 3 | `03_indexes.sql` | Índices. |
| 4 | `04_views.sql` | 8 views e a função `fn_veiculos_disponiveis`. |
| 5 | `05_dados_exemplo.sql` | Lojas, categorias, veículos, pessoas, reservas e locações de exemplo. |
| 6 | `06_consultas_relatorios.sql` | Relatórios R01 a R13 (somente consulta). |
| 7 | `07_roles_privilegios.sql` | Grupos e usuários do banco e seus privilégios. |
| 8 | `08_triggers.sql` | 8 *triggers*. |
| 9 | `09_procedures.sql` | *Procedures* `sp_*` e função de login `fn_autenticar`. |
| 10 | `10_backup_restore.sh` | Backup e restauração (Linux, macOS ou Git Bash). |
| 11 | `11_testes_regras.sql` | 34 verificações das regras; roda em transação e desfaz tudo no final. |

Para recriar o banco do zero, rode 01 a 09 em sequência; para só conferir as regras, rode o 11.

## Usando o DBeaver

1. Crie a conexão com o PostgreSQL (`localhost:5432`, usuário `postgres`).
2. **Nome do banco:** o script 01 cria `next_drive`, mas a aplicação Java espera por padrão **`nextdrive`** (sem o sublinhado). Escolha um dos dois caminhos:
   - criar o banco `nextdrive` pelo DBeaver (botão direito em *Databases > Create New Database*) e **pular o script 01**, pois os demais scripts não dependem do nome; ou
   - usar `next_drive` e iniciar a aplicação com a variável `NEXTDRIVE_DB_URL=jdbc:postgresql://localhost:5432/next_drive`.
3. Abra uma conexão **no banco criado**, e execute cada script de 02 a 09 com *Execute Script* (`Alt+X`), não *Execute Statement* (`Ctrl+Enter`), porque os arquivos têm vários comandos.
4. Se alterar o `04_views.sql` depois, basta rodá-lo de novo: usa `CREATE OR REPLACE` e mantém os privilégios.

## Usuários

- **Usuários do banco** (script 07): `usr_gerente`, `usr_atendente`, `usr_auditor` e `usr_site`, todos com a senha de exemplo `Trocar@123`. São usados pela aplicação Java, um por perfil. Troque as senhas antes de qualquer uso real.
- **Usuários do sistema** (tabela `pessoa`, script 05), senha `Senha@123`: por exemplo `ana.lima@example.com` (cliente), `juliana.prado@nextdrive.example` (atendente) e `marcos.almeida@nextdrive.example` (gerente).

## Documentação relacionada

[`docs/diagrama-relacional.md`](../docs/diagrama-relacional.md) · [`docs/dicionario-de-dados.md`](../docs/dicionario-de-dados.md) · [`docs/politicas-de-acesso.md`](../docs/politicas-de-acesso.md) · [`docs/backup-restore.md`](../docs/backup-restore.md)
