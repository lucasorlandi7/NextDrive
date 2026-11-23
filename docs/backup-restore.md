# Política e Configuração de Backup e Restauração — Next Drive

A rotina está no script [`sql/10_backup_restore.sh`](../sql/10_backup_restore.sh). Ele usa as ferramentas que já vêm com o PostgreSQL: `pg_dump` para gerar o backup e `pg_restore` para restaurar.

## Política

| Item | Definição |
|---|---|
| **O que é copiado** | O banco `nextdrive` inteiro: tabelas, dados, índices, views, triggers, procedures e permissões dos grupos. |
| **Formato** | `pg_dump -Fc` (formato *custom*): o arquivo sai comprimido e permite restaurar só algumas tabelas, se for preciso. |
| **Nome do arquivo** | `backups/nextdrive_AAAAMMDD_HHMMSS.backup` (data e hora em que foi gerado). |
| **Frequência** | Um backup **por dia**, de madrugada, fora do horário de atendimento. |
| **Retenção** | Manter os backups dos **últimos 7 dias** e uma cópia por semana fora do computador do banco (pendrive ou nuvem). O script não apaga arquivos antigos; a limpeza é manual. |
| **Restauração** | Sempre em um banco **separado**, o `nextdrive_restaurado`, para nunca sobrescrever o banco em uso por engano. |
| **Teste** | Uma vez por mês, restaurar o backup mais recente e conferir as contagens (veja "Como conferir"). |
| **Senha** | Nunca fica escrita no script. É informada antes, pela variável `PGPASSWORD`. |
| **Usuários do servidor** | O `pg_dump` não copia os usuários (`atendente_nextdrive`, `gerente_nextdrive`). Em um servidor novo, rode o `09_roles_privilegios.sql` depois de restaurar. |

## Configuração do script

No começo do script ficam as configurações:

```bash
BANCO=nextdrive                  # banco que vai para o backup
RESTAURADO=nextdrive_restaurado  # banco criado na restauração
USUARIO=postgres
HOST=localhost
PORTA=${PORTA:-5432}             # pode ser trocada sem editar o script: PORTA=5433
PASTA=./backups                  # pasta onde os arquivos são salvos
```

Se o seu PostgreSQL usa outro usuário, host ou porta, ajuste essas linhas.

## Linux, macOS ou Git Bash (Windows)

Abra o terminal **na pasta `sql`** do projeto e informe a senha do PostgreSQL:

```bash
cd sql
export PGPASSWORD=sua_senha
```

No **Git Bash do Windows**, coloque antes as ferramentas do PostgreSQL no caminho (troque o 16 pela sua versão):

```bash
export PATH="/c/Program Files/PostgreSQL/16/bin:$PATH"
```

**Fazer o backup:**

```bash
bash 10_backup_restore.sh backup
```

Resposta esperada: `Backup salvo em ./backups/nextdrive_20261123_194551.backup`

**Restaurar** (cria o banco `nextdrive_restaurado`; se ele já existir, é apagado e criado de novo):

```bash
bash 10_backup_restore.sh restaurar backups/nextdrive_20261123_194551.backup
```

Resposta esperada: `Banco nextdrive_restaurado restaurado a partir de backups/nextdrive_20261123_194551.backup`

Use o nome do arquivo que o seu backup gerou. Rodar o script sem nada mostra como usar.

## Windows sem Git Bash

### Pelo DBeaver

1. **Backup:** botão direito no banco `nextdrive` → **Ferramentas** → **Backup**. Marque o esquema `public`, escolha o formato **Custom**, a pasta de saída e clique em **Iniciar**.
2. **Restaurar:** crie um banco novo e vazio chamado `nextdrive_restaurado`. Botão direito nele → **Ferramentas** → **Restaurar**, escolha o formato **Custom**, selecione o arquivo de backup e clique em **Iniciar**.

### Pelo Prompt de Comando (cmd)

```bat
set PGPASSWORD=sua_senha
cd "C:\Program Files\PostgreSQL\16\bin"

:: backup
pg_dump -h localhost -p 5432 -U postgres -Fc -f C:\backups\nextdrive.backup nextdrive

:: restauração em um banco novo
createdb -h localhost -p 5432 -U postgres nextdrive_restaurado
pg_restore -h localhost -p 5432 -U postgres -d nextdrive_restaurado C:\backups\nextdrive.backup
```

A pasta `C:\backups` precisa existir antes do backup.

## Agendamento automático

- **Linux:** uma linha no `crontab -e` faz o backup todo dia às 2h:
  ```
  0 2 * * *  cd /caminho/do/NextDrive/sql && PGPASSWORD=sua_senha bash 10_backup_restore.sh backup >> backups/backup.log 2>&1
  ```
- **Windows:** no **Agendador de Tarefas**, crie uma tarefa diária que execute o comando `pg_dump` acima.

## Se o banco principal for perdido

1. Instale o PostgreSQL e crie o banco vazio: `createdb -U postgres nextdrive`.
2. Restaure o backup mais recente direto nele: `pg_restore -U postgres -d nextdrive backups/nextdrive_AAAAMMDD_HHMMSS.backup`. Se o servidor for novo, podem aparecer avisos de que `grupo_atendente` e `grupo_gerente` não existem; os dados são restaurados mesmo assim.
3. Conectado no `nextdrive`, rode o `09_roles_privilegios.sql` para recriar os usuários e as permissões.
4. Confira as contagens abaixo e só então volte a usar o sistema.

## Restaurar só uma tabela

O formato custom permite recuperar, por exemplo, só os pagamentos, sem mexer no resto:

```bash
pg_restore -U postgres -d nextdrive_restaurado -t pagamento backups/nextdrive_AAAAMMDD_HHMMSS.backup
```

## Como conferir

Rode a consulta abaixo no banco original e no restaurado. Os números devem ser iguais:

```sql
SELECT (SELECT COUNT(*) FROM cliente)   AS clientes,
       (SELECT COUNT(*) FROM veiculo)   AS veiculos,
       (SELECT COUNT(*) FROM reserva)   AS reservas,
       (SELECT COUNT(*) FROM locacao)   AS locacoes,
       (SELECT COUNT(*) FROM pagamento) AS pagamentos,
       (SELECT COUNT(*) FROM pg_trigger WHERE NOT tgisinternal) AS triggers;
```

## Resultado do teste realizado

Teste feito com os dados de exemplo: backup do banco `nextdrive` e restauração no `nextdrive_restaurado`.

| Conferência | Banco original | Banco restaurado |
|---|:---:|:---:|
| Clientes | 3 | 3 |
| Veículos | 5 | 5 |
| Reservas | 5 | 5 |
| Locações | 3 | 3 |
| Pagamentos | 2 | 2 |
| Triggers | 4 | 4 |
| Views | 3 | 3 |
| Funções e procedures | 7 | 7 |

As permissões também voltaram: o usuário `gerente_nextdrive` conseguiu consultar a view de faturamento no banco restaurado.
