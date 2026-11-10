# Política e Configuração de Backup e Restauração — Next Drive

Rotina implementada em `sql/10_backup_restore.sh` (RF18), testada com backup completo e restauração em um banco novo.

## Política

| Item | Definição |
|---|---|
| **O que é copiado** | Banco `next_drive` completo (tabelas, dados, views, índices, triggers, procedures e privilégios) e, separadamente, os usuários/grupos do servidor (roles). |
| **Frequência** | Backup completo **diário** (de madrugada, fora do horário de balcão). Backup de roles **semanal** e sempre que um perfil mudar. |
| **Formato** | `pg_dump -Fc` (custom): comprimido, e permite restaurar só algumas tabelas. |
| **Retenção** | 7 dias no disco (o script apaga os mais antigos). Recomenda-se uma cópia semanal fora da máquina do banco. |
| **Verificação** | Todo backup é conferido com `pg_restore --list` logo após ser gerado. |
| **Teste de restauração** | **Mensal**: restaurar o backup mais recente em um banco `next_drive_restore` e comparar contagens. |
| **Segurança** | Senha via `PGPASSWORD` ou `~/.pgpass`; nunca dentro do script. Arquivos de backup com acesso restrito ao administrador. |
| **Perda máxima aceita (RPO)** | Até 24 horas (backup diário). Para menos, habilitar arquivamento de WAL (melhoria futura). |

## Uso do script (Linux, macOS ou Git Bash)

```bash
# backup completo -> backups/next_drive_AAAAMMDD_HHMMSS.dump
./sql/10_backup_restore.sh backup

# usuários e grupos do servidor -> backups/roles_AAAAMMDD_HHMMSS.sql
./sql/10_backup_restore.sh globais

# conferir se um backup está íntegro
./sql/10_backup_restore.sh verificar backups/next_drive_20261109_231945.dump

# restaurar em um banco NOVO (padrão: next_drive_restore)
./sql/10_backup_restore.sh restore backups/next_drive_20261109_231945.dump

# listar backups
./sql/10_backup_restore.sh listar
```

Variáveis opcionais: `DB_NAME` (padrão `next_drive`), `DB_USER` (`postgres`), `DB_HOST` (`localhost`),
`DB_PORT` (`5432`), `BACKUP_DIR` (`./backups`) e `RETENCAO_DIAS` (`7`). Se o seu banco tem outro nome:
`DB_NAME=nextdrive ./sql/10_backup_restore.sh backup`.

### Agendamento diário (cron)

```cron
0 2 * * *  cd /caminho/do/projeto && PGPASSWORD=... DB_USER=postgres ./sql/10_backup_restore.sh backup >> backups/backup.log 2>&1
```

## Backup e restauração no Windows

**Pelo DBeaver:** clique com o botão direito no banco → **Ferramentas** → **Backup** (escolha formato *Custom*) → informe a pasta
de saída. Para restaurar: botão direito em um banco **novo e vazio** → **Ferramentas** → **Restaurar**.

**Pela linha de comando (cmd ou PowerShell):**

```bat
:: backup
"C:\Program Files\PostgreSQL\16\bin\pg_dump.exe" -h localhost -U postgres -Fc -f C:\backups\next_drive.dump next_drive

:: usuários e grupos
"C:\Program Files\PostgreSQL\16\bin\pg_dumpall.exe" -h localhost -U postgres --roles-only -f C:\backups\roles.sql

:: restauração em banco novo
"C:\Program Files\PostgreSQL\16\bin\createdb.exe" -h localhost -U postgres next_drive_restore
"C:\Program Files\PostgreSQL\16\bin\pg_restore.exe" -h localhost -U postgres -d next_drive_restore C:\backups\next_drive.dump
```

Para automatizar no Windows, use o **Agendador de Tarefas** executando o comando de backup todos os dias.

## Restauração seletiva (uma ou poucas tabelas)

O formato custom permite recuperar, por exemplo, só os pagamentos apagados por engano, sem mexer no resto:

```bash
pg_restore -h localhost -U postgres -d next_drive_restore -t pagamento -t pagamento_cartao -t pagamento_pix arquivo.dump
```

## Procedimento de recuperação de desastre

1. Reinstalar o PostgreSQL e restaurar os usuários: `psql -U postgres -f backups/roles_AAAAMMDD_HHMMSS.sql`.
2. Restaurar o banco: `./sql/10_backup_restore.sh restore backups/next_drive_AAAAMMDD_HHMMSS.dump next_drive`.
3. Conferir: número de tabelas (17), contagem de locações e pagamentos, e os 8 triggers (`SELECT count(*) FROM pg_trigger WHERE NOT tgisinternal;`).
4. Apontar a aplicação para o banco restaurado.

## Resultado do teste realizado

| Conferência | Banco original | Banco restaurado |
|---|---|---|
| Tabelas | 17 | 17 |
| Locações | 6 | 6 |
| Pagamentos | 5 | 5 |
| Veículos | 10 | 10 |
| Triggers | 8 | 8 |

O script também recusa restaurar sobre um banco que já existe, evitando sobrescrever dados por engano.
