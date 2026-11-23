# Next Drive — Sistema de Aluguel de Veículos

Sistema web para gerenciamento de aluguel de veículos. O sistema contempla o cadastro de clientes e veículos, reservas, locações, devoluções e pagamentos.


**Trabalho Final Integrado — Programação II, Engenharia de Software I e Banco de Dados II**

Ciência da Computação — UNOESC  
4º semestre — 2026/02

**Autores:** Lucas Orlandi, Samuel Gralow, Diogo Rodrigo Nielsson e Felipe Kremer.

**Professores:** Otilia Barbosa, Franciele Petry e Roberson Alves.

---

## 1. Resumo


O Next Drive tem como objetivo informatizar o processo de aluguel de veículos, permitindo o gerenciamento de clientes, veículos, categorias, reservas, locações e pagamentos.

O sistema busca organizar as operações da locadora, controlar a disponibilidade dos veículos e garantir a integridade das informações armazenadas no banco de dados.

O projeto integra três disciplinas:

- **Programação II (PII):** desenvolvimento da aplicação Java, orientação a objetos, persistência e interface.
- **Engenharia de Software I (ESI):** modelagem do sistema por meio de diagramas UML e organização do desenvolvimento.
- **Banco de Dados II (BDII):** modelagem relacional, consultas SQL, índices, views, triggers, procedures, controle de acesso e backup.


## 2. O que o sistema faz

| Perfil | Funcionalidades |
|---|---|
| Administrador | Cadastrar clientes e carros, realizar aluguéis, registrar devoluções e pagamentos, colocar carros em manutenção e consultar locações. |

### 2.1 Processo de locação

O funcionamento geral do sistema contempla:

1. Cadastro de clientes e carros.
2. Aluguel de um carro disponível.
3. Devolução do veículo com registro da quilometragem.
4. Cálculo do valor final da locação, considerando as diárias e possíveis multas por atraso.
5. Pagamento por cartão ou Pix.


## 3. Requisitos do sistema

### 3.1 Requisitos Funcionais

| Código | Descrição | Componente |
|---|---|---|
| RF01 | Criar índices para a placa dos veículos, o CPF dos clientes e as datas das reservas. | BDII |
| RF02 | Permitir o acesso ao sistema por meio de usuário e senha. | PII |
| RF03 | Permitir cadastrar, consultar e excluir clientes pessoa física, informando nome, CPF, e-mail, CNH e validade da CNH. | PII |
| RF04 | Permitir cadastrar, consultar e excluir carros, vinculados a uma categoria. | PII |
| RF05 | Disponibilizar a consulta de carros disponíveis por meio de view. | BDII |
| RF06 | Permitir alugar um carro disponível, escolhendo o carro, o cliente e as datas de retirada e devolução. | PII |
| RF07 | Permitir o cancelamento de reservas com justificativa, por meio de procedure. | BDII |
| RF08 | Permitir a devolução do carro, informando a quilometragem. | PII |
| RF09 | Calcular o valor final da locação (diárias mais multa por atraso) por meio de função. | BDII |
| RF10 | Permitir o pagamento da locação por cartão ou Pix. | PII |
| RF11 | Permitir colocar um carro em manutenção, retirando-o da lista de disponíveis, e liberá-lo novamente. | PII |
| RF12 | Disponibilizar a lista de locações com cliente, carro, datas, valor e situação. | PII |
| RF13 | Disponibilizar relatórios administrativos por meio de views. | BDII |
| RF14 | Impedir locação de carro indisponível ou já alugado e liberar o carro ao encerrar a locação, por meio de trigger. | BDII |
| RF15 | Impedir reservas para cliente com CNH vencida antes da devolução, por meio de trigger. | BDII |
| RF16 | Registrar auditoria das alterações realizadas em pagamentos, por meio de trigger. | BDII |
| RF17 | Controlar o acesso ao banco por perfis, usuários e privilégios, além de permitir backup e restauração. | BDII |
| RF18 | Elaborar o diagrama de casos de uso, com o ator Administrador. | ESI |
| RF19 | Elaborar o diagrama de classes, contemplando a herança das classes de pagamento. | ESI |
| RF20 | Elaborar diagramas de sequência para o aluguel e para a devolução com pagamento. | ESI |
| RF21 | Elaborar o diagrama de atividades do processo de locação. | ESI |
| RF22 | Elaborar diagramas de estados para Reserva, Veículo e Locação. | ESI |

### 3.2 Requisitos Não Funcionais

| Código | Descrição |
|---|---|
| RNF01 | O sistema deve ser desenvolvido utilizando a linguagem Java. |
| RNF02 | O sistema deve disponibilizar uma interface acessível por navegador web. |
| RNF03 | O banco de dados utilizado deve ser PostgreSQL. |
| RNF04 | As consultas ao banco de dados devem utilizar comandos parametrizados. |
| RNF05 | | RNF05 | O acesso ao banco de dados deve ser controlado por perfis e privilégios. |
| RNF06 | O código Java deve seguir padrões de organização, nomenclatura e legibilidade. |
| RNF07 | As classes e métodos do projeto devem possuir documentação JavaDOC. |
| RNF08 | O desenvolvimento deve utilizar Git e GitHub para versionamento. |
| RNF09 | Os diagramas UML devem permanecer consistentes com o código-fonte e o banco de dados. |
| RNF10 | O desenvolvimento deve utilizar GitHub Projects para organização das atividades. |

## 4. Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 17 | Desenvolvimento da aplicação |
| JDBC | Comunicação entre Java e PostgreSQL |
| PostgreSQL 16 | Banco de dados relacional |
| SQL / PLpgSQL | Consultas, funções, triggers e procedures |
| HTML / CSS | Interface web |
| Git / GitHub | Versionamento e colaboração |
| DBeaver | Administração do banco de dados |
| Eclipse | Ambiente de desenvolvimento |
| UML | Modelagem do sistema |
| Spring Boot 3 | Desenvolvimento da aplicação web |
| Maven | Gerenciamento de dependências e execução do projeto |
| JUnit 4 | Testes automatizados |
| Bootstrap / JavaScript | Interface e funcionalidades do sistema |


## 5. Estrutura do repositório

O projeto está organizado da seguinte forma:

```text
NextDrive/
├── docs/
│   ├── telas/
│   ├── uml/
│   │   ├── casos-de-uso.png
│   │   ├── classes.png
│   │   ├── sequencia-aluguel.png
│   │   ├── sequencia-devolucao-pagamento.png
│   │   ├── atividades.png
│   │   ├── estados-veiculo.png
│   │   ├── estados-reserva.png
│   │   ├── estados-locacao.png
│   │   └── relacional.png
│   ├── artigo.md
│   ├── backup-restore.md
│   ├── dicionario-de-dados.md
│   └── politicas-de-acesso.md
├── java/
├── sql/
├── Enunciado_Trabalho_A1.pdf
├── Levantamento de Requisitos.docx
└── README.md
```


## 6. Itens do trabalho

| Item | Onde está |
|---|---|
| Enunciado do trabalho | [`Enunciado_Trabalho_A1.pdf`](Enunciado_Trabalho_A1.pdf) |
| Levantamento de requisitos | [`Levantamento de Requisitos.docx`](Levantamento%20de%20Requisitos.docx) |
| Artigo completo | [`docs/artigo.md`](docs/artigo.md) |
| Descrição do modelo de negócio | [Artigo, seção 2](docs/artigo.md#2-modelo-de-negócio) |
| Diagrama relacional | [Artigo, seção 4](docs/artigo.md#4-modelagem-do-banco-de-dados) · [imagem](docs/uml/diagrama-relacional.jpeg) |
| Dicionário de dados | [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md) |
| Scripts do banco de dados | [`sql/`](sql/) |
| Políticas de acesso | [`docs/politicas-de-acesso.md`](docs/politicas-de-acesso.md) |
| Backup e restauração | [`docs/backup-restore.md`](docs/backup-restore.md) |
| Diagramas UML | [`docs/uml/`](docs/uml/) · [seção 8 deste README](#8-diagramas-uml) |
| Aplicação Java | [`java/`](java/) |
| Capturas de tela | [`docs/telas/`](docs/telas/) |

## 7. Banco de dados

O banco de dados do Next Drive utiliza PostgreSQL 16 e armazena as informações necessárias para o funcionamento do sistema de aluguel de veículos.

A estrutura contempla clientes, funcionários, veículos, categorias, lojas, reservas, locações, pagamentos e multas, utilizando chaves primárias, chaves estrangeiras e restrições de integridade.

### 7.1 Scripts do banco de dados

Os scripts estão disponíveis na pasta [`sql/`](sql/) e devem ser executados na ordem apresentada abaixo.


| Ordem | Arquivo | Finalidade |
|---|---|---|
| 1 | `01_create_database.sql` | Criação do banco (opcional). |
| 2 | `02_create_tables.sql` | Criação das tabelas, chaves e restrições. |
| 3 | `03_indices.sql` | Criação dos índices. |
| 4 | `04_views.sql` | Criação das views. |
| 5 | `05_dados_exemplo.sql` | Inserção de dados de exemplo. |
| 6 | `06_consultas_relatorios.sql` | Consultas dos relatórios. |
| 7 | `07_gatilhos.sql` | Criação dos triggers. |
| 8 | `08_procedimentos.sql` | Criação da função e das procedures. |
| 9 | `09_roles_privilegios.sql` | Criação de grupos, usuários e privilégios. |
| 10 | `10_backup_restore.sh` | Rotinas de backup e restauração. |
| 11 | `11_testes_regras.sql` | 13 verificações das regras de negócio. |


### 7.2 Recursos implementados no banco

O banco de dados utiliza os seguintes recursos:

- **Índices:** melhoram o desempenho das consultas.
- **Visualizações:** disponibilizam dados organizados para consultas e relatórios.
- **Gatilhos:** automatizam validações e auditorias.
- **Procedures:** executam operações e regras de negócio.
- **Funções:** permitem executar operações específicas do sistema.
- **Controle de acesso:** define usuários, grupos e privilégios.
- **Backup e restauração:** permitem gerar cópias de segurança e recuperar os dados.
- **Testes:** verificam o comportamento das regras implementadas.

### 7.3 Documentação complementar

- Modelo relacional: [`docs/uml/diagrama-relacional.jpeg`](docs/uml/diagrama-relacional.jpeg) e [artigo, seção 4](docs/artigo.md#4-modelagem-do-banco-de-dados).
- Dicionário de dados: [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md).
- Políticas de acesso: [`docs/politicas-de-acesso.md`](docs/politicas-de-acesso.md).
- Procedimentos de backup: [`docs/backup-restore.md`](docs/backup-restore.md).

## 8. Diagramas UML

Os diagramas UML documentam a estrutura, o comportamento e os principais processos do sistema Next Drive. Clique no link para abrir a imagem do diagrama.

| Diagrama | Abrir | Explicação no artigo |
|---|---|---|
| Casos de uso | [Ver diagrama de casos de uso](docs/uml/diagrama-de-casos-de-uso.md) | [Seção 6.1](docs/artigo.md#61-casos-de-uso-rf19) |
| Classes | [Ver imagem do diagrama de classes](https://raw.githubusercontent.com/lucasorlandi7/NextDrive/main/docs/uml/diagrama-de-classe.jpeg) | [Seção 6.2](docs/artigo.md#62-classes-rf20) |
| Relacional | [Ver imagem do diagrama relacional](https://raw.githubusercontent.com/lucasorlandi7/NextDrive/main/docs/uml/diagrama-relacional.jpeg) | [Seção 4.1](docs/artigo.md#41-diagrama-entidade-relacionamento) |
| Sequência — reserva | [Ver imagem da sequência de reserva](https://raw.githubusercontent.com/lucasorlandi7/NextDrive/main/docs/uml/diagrama-sequencia-reserva.jpeg) | [Seção 6.3](docs/artigo.md#63-sequência-rf21) |
| Sequência — retirada | [Ver imagem da sequência de retirada](https://raw.githubusercontent.com/lucasorlandi7/NextDrive/main/docs/uml/diagrama-sequencia-retirada.jpeg) | [Seção 6.3](docs/artigo.md#63-sequência-rf21) |
| Sequência — devolução e pagamento | [Ver imagem da sequência de devolução](https://raw.githubusercontent.com/lucasorlandi7/NextDrive/main/docs/uml/diagrama-sequencia-devolucao-pagamento.jpeg) | [Seção 6.3](docs/artigo.md#63-sequência-rf21) |
| Atividades | [Ver diagrama de atividades](docs/uml/diagrama-de-atividades.md) | [Seção 6.4](docs/artigo.md#64-atividades-rf22) |
| Estados | [Ver diagramas de estados](docs/uml/diagrama-de-estados.md) | [Seção 6.5](docs/artigo.md#65-estados-rf23) |


## 9. Telas do sistema

As capturas de tela apresentam as principais funcionalidades desenvolvidas no sistema Next Drive.

Os arquivos de imagem estão organizados na pasta [`docs/telas/`](docs/telas/).

As telas documentam as seguintes operações:

- Login.
- Cadastro e gerenciamento de clientes.
- Cadastro e gerenciamento de carros.
- Aluguel de veículos.
- Consulta de locações.


## 10. Como executar

### 10.1 Banco de dados

**Requisitos:** PostgreSQL 16 ou superior e DBeaver.

1. Instale e configure o PostgreSQL.
2. Abra o DBeaver, conecte-se ao servidor PostgreSQL e crie um banco chamado **`nextdrive`** (sem sublinhado), que é o nome que a aplicação Java usa.
3. Abra uma conexão nesse banco e execute os scripts da pasta [`sql/`](sql/) na ordem abaixo, usando *Execute Script* (`Alt+X`):

| Ordem | Script |
|---|---|
| 1 | `02_create_tables.sql` |
| 2 | `03_indices.sql` |
| 3 | `04_views.sql` |
| 4 | `05_dados_exemplo.sql` |
| 5 | `06_consultas_relatorios.sql` |
| 6 | `07_roles_privilegios.sql` |
| 7 | `08_gatilhos.sql` |
| 8 | `09_procedimentos.sql` |

### 10.1 Banco de dados

**Requisitos:** PostgreSQL 16 ou superior e DBeaver.

1. Instale e configure o PostgreSQL.
2. Abra o DBeaver e conecte-se ao servidor PostgreSQL.
3. Acesse a pasta `sql/` do repositório.
4. Execute os scripts na seguinte ordem:

| Ordem | Script |
|---|---|
| 1 | `02_create_tables.sql` |
| 2 | `03_indices.sql` |
| 3 | `04_views.sql` |
| 4 | `05_dados_exemplo.sql` |
| 5 | `07_gatilhos.sql` |
| 6 | `08_procedimentos.sql` |
| 7 | `09_roles_privilegios.sql` |
| 8 | `06_consultas_relatorios.sql` (opcional) |
| 9 | `11_testes_regras.sql` |

**Atenção:** o script `01_create_database.sql` é opcional. O arquivo `10_backup_restore.sh` deve ser executado separadamente pelo terminal, e não pelo DBeaver.

**Usuários de exemplo:** Login no site: `admin` / `1234`. Usuários do banco: `atendente_nextdrive` e `gerente_nextdrive`.


### 10.2 Aplicação Java

**Requisitos:** JDK 17 e Eclipse ou Maven.

1. Instale o JDK 17 e o Eclipse (ou o Maven).
2. Importe a pasta `java/` como **Existing Maven Project** no Eclipse.
3. Abra o arquivo `SingleConnection.java` e confira a senha de conexão com o PostgreSQL.
4. Execute a classe `Application` ou, pelo terminal dentro da pasta `java/`, execute:

```bash
mvn spring-boot:run
```

5. Abra o endereço no navegador:

http://localhost:8080

6. Entre no sistema utilizando:

**Usuário:** `admin`

**Senha:** `1234`

## 11. Organização do desenvolvimento

O desenvolvimento do Next Drive foi organizado de forma colaborativa, com divisão das atividades entre os integrantes da equipe e acompanhamento das tarefas durante o projeto.

### 11.1 Planejamento das atividades

As atividades foram distribuídas considerando os módulos do sistema e os requisitos das disciplinas de Programação II, Engenharia de Software I e Banco de Dados II.

O planejamento e o acompanhamento das tarefas foram realizados por meio do GitHub Projects, permitindo organizar as atividades, acompanhar seu andamento e identificar as responsabilidades de cada integrante.

### 11.2 Versionamento

O código-fonte e a documentação do projeto foram mantidos em um repositório GitHub, utilizando Git para controle de versão.

O versionamento permitiu registrar as alterações realizadas, integrar as contribuições dos integrantes e manter o histórico de desenvolvimento do sistema.

**Repositório do projeto:**  
https://github.com/lucasorlandi7/NextDrive


## 12. Desenvolvedores

| Integrante | Responsabilidades principais |
|---|---|
| Lucas Orlandi | Reservas e controle de locações |
| Samuel Gralow | Pagamentos, banco de dados e documentação |
| Diogo Rodrigo Nielsson | Cadastro e gerenciamento de carros |
| Felipe Kremer | Cadastro e gerenciamento de clientes |


## 13. Considerações finais

O Next Drive integra os conhecimentos desenvolvidos nas disciplinas de Programação II, Engenharia de Software I e Banco de Dados II, aplicando conceitos de orientação a objetos, modelagem UML, persistência de dados e regras de negócio.

O projeto busca oferecer uma solução organizada para o gerenciamento de uma locadora de veículos, contemplando as principais etapas do processo de aluguel.
