# Next Drive

Sistema web de **locação de veículos** para uma locadora com várias lojas. Cobre o processo completo: reserva, retirada, devolução, pagamento e pós-locação (multas e liberação da cautela).

Trabalho Final Integrado de **Programação II**, **Engenharia de Software I** e **Banco de Dados II** — Ciência da Computação, Unoesc, 4º semestre, 2026/02.

**Autores:** Lucas Orlandi, Samuel Gralow, Diogo Rodrigo Nielsson, Felipe Kremer

**Professores:** Otilia, Fran, Roberson

## O que o sistema faz

| Perfil | O que pode fazer |
|---|---|
| **Cliente** (pessoa física ou jurídica) | Cadastrar-se, consultar disponibilidade, reservar e acompanhar suas locações |
| **Atendente** | Cadastrar clientes, reservar e cancelar, registrar retirada, devolução, pagamentos e multas |
| **Gerente** | Tudo do atendente, mais funcionários, frota, lojas, categorias, manutenções, relatórios e auditoria |

As regras de negócio (locação conflitante, idade e CNH do condutor, locação encerrada imutável, auditoria) estão no **banco de dados**, em triggers e procedures. O Java mostra as telas e chama o banco.

## Tecnologias

Java 17 · JDBC · PostgreSQL 16 · HTML/CSS · UML · Git/GitHub · DBeaver · Eclipse

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| [`sql/`](sql/) | Scripts do banco, em ordem de execução (tabelas, índices, views, dados, relatórios, perfis, triggers, procedures, backup e testes). [Como executar](sql/README.md) |
| [`java/`](java/) | Aplicação web em Java (pacotes `model`, `dao`, `service`, `web`). [Como executar](java/README.md) |
| [`docs/`](docs/) | Artigo completo, diagramas, dicionário de dados, políticas de acesso, backup, plano de sprints e prints |

## Itens do trabalho

| Item | Onde está |
|---|---|
| Descrição do modelo de negócio | [Artigo, seção 2](docs/artigo.md#2-modelo-de-negócio) |
| Diagrama relacional | [Artigo, seção 4](docs/artigo.md#4-modelagem-do-banco-de-dados) · [imagem](docs/diagrama-relacional.png) |
| Dicionário de dados | [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md) |
| Scripts de criação da base | [`sql/`](sql/) · [Artigo, seção 5](docs/artigo.md#5-scripts-do-banco-de-dados) |
| Scripts dos SELECTs dos relatórios | [`sql/06_consultas_relatorios.sql`](sql/06_consultas_relatorios.sql) · [Artigo, seção 5.5](docs/artigo.md#55-relatórios-selects) |
| Diagramas UML | [`docs/uml/`](docs/uml/) · [Artigo, seção 6](docs/artigo.md#6-modelagem-uml) |
| Telas do sistema | [Artigo, seção 8](docs/artigo.md#8-telas-do-sistema) |
| Políticas de acesso | [`docs/politicas-de-acesso.md`](docs/politicas-de-acesso.md) |
| Backup e restauração | [`docs/backup-restore.md`](docs/backup-restore.md) · [`sql/10_backup_restore.sh`](sql/10_backup_restore.sh) |
| Planejamento em sprints | [`docs/plano-de-sprints.md`](docs/plano-de-sprints.md) |
| Conclusões | [Artigo, seção 11](docs/artigo.md#11-conclusões) |

**Artigo completo (formato UNOESC/ABNT):** [`docs/artigo.md`](docs/artigo.md)

## Como executar

1. **Banco:** crie o banco `nextdrive` no PostgreSQL e execute os scripts `sql/02` a `sql/09`, na ordem. Detalhes em [`sql/README.md`](sql/README.md).
2. **Aplicação:** importe a pasta `java/` como projeto Maven e execute `br.edu.unoesc.nextdrive.App`, ou use `mvn package` e `java -jar target/nextdrive-jar-with-dependencies.jar`.
3. Abra <http://localhost:8080>.

Usuários de exemplo (senha `Senha@123`): cliente `ana.lima@example.com`, atendente `juliana.prado@nextdrive.example`, gerente `marcos.almeida@nextdrive.example`.

## Telas

As capturas de tela ficam em [`docs/telas/`](docs/telas/) e estão no [artigo, seção 8](docs/artigo.md#8-telas-do-sistema).
