# Next Drive

Sistema de locação de veículos feito com Java, Spring Boot, JDBC e PostgreSQL.
Trabalho Final Integrado de PII, ESI e BDII (Unoesc).

## Funcionalidades
- Cadastro de clientes e de carros (com exclusão e envio do carro para manutenção)
- Aluguel de um carro disponível para um cliente, conferindo a validade da CNH
- Devolução com cálculo do valor (multa por atraso) e pagamento por cartão ou Pix

## Como rodar
1. Crie o banco `nextdrive` no PostgreSQL (pelo DBeaver ou com `sql/01_create_database.sql`).
2. Conectado nesse banco, rode os scripts da pasta `sql/` com Execute Script (Alt+X), nesta ordem:

| Ordem | Script | O que faz |
|---|---|---|
| 1 | `02_create_tables.sql` | Cria as tabelas |
| 2 | `03_indices.sql` | Índices |
| 3 | `04_views.sql` | Views (consultas salvas) |
| 4 | `05_dados_exemplo.sql` | Dados de exemplo |
| 5 | `06_consultas_relatorios.sql` | Consultas de relatórios (opcional) |
| 6 | `07_gatilhos.sql` | Triggers e tabela de auditoria |
| 7 | `08_procedimentos.sql` | Função e procedures |
| 8 | `09_roles_privilegios.sql` | Grupos, usuários e permissões |
| 9 | `11_testes_regras.sql` | Testes das regras (opcional, desfaz tudo no final) |

   O `10_backup_restore.sh` é um script de terminal, executado à parte (veja abaixo).
3. Se a sua senha ou porta do PostgreSQL forem diferentes, ajuste em `src/main/java/br/edu/unoesc/nextdrive/dao/SingleConnection.java`.
4. Execute a classe `br.edu.unoesc.nextdrive.Application`. O site abre sozinho em http://localhost:8080.
5. Login: usuário `admin`, senha `1234`.

## Regras no banco (BDII)
- **Índices:** placa e CPF (pelo UNIQUE) e índices extras em status, datas da reserva, locação e pagamento.
- **Views:** carros disponíveis, locações abertas e faturamento por categoria.
- **Triggers:** não deixa alugar carro indisponível ou já alugado; devolve o carro ao ficar disponível quando a locação é encerrada; bloqueia reserva com CNH vencida; audita os pagamentos.
- **Procedures:** `sp_devolver_veiculo` (calcula o valor final e encerra a locação) e `sp_cancelar_reserva` (com justificativa).
- **Controle de acesso:** grupos `grupo_atendente` e `grupo_gerente`, com os usuários `atendente_nextdrive` (senha `Atendente@123`) e `gerente_nextdrive` (senha `Gerente@123`). O atendente não exclui nada.
- **Backup:** `./10_backup_restore.sh backup` gera o arquivo; `./10_backup_restore.sh restaurar arquivo.backup` restaura em um banco novo chamado `nextdrive_restaurado`. No Windows, use o Git Bash ou rode direto `pg_dump -U postgres -Fc -f nextdrive.backup nextdrive`.

## Estrutura
- `model`: classes do negócio (Cliente, Veiculo, Reserva, Locacao, Pagamento e seus tipos)
- `dao`: acesso ao banco com JDBC
- `sql`: scripts do banco (tabelas, índices, views, triggers, procedures, acesso, backup e testes)
- `controller`: endpoints da API (Spring Boot)
- `src/main/resources/static`: as páginas do sistema (login, menu, clientes, carros, alugar e locações), com Bootstrap e jQuery
