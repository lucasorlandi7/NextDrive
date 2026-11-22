# Next Drive — Sistema de Aluguel de Veículos

Sistema web para gerenciamento de aluguel de veículos em uma locadora com várias lojas. O sistema contempla o processo de reserva, retirada, devolução, pagamento e pós-locação, incluindo o registro de multas e a liberação da caução.

**Trabalho Final Integrado — Programação II, Engenharia de Software I e Banco de Dados II**

Ciência da Computação — UNOESC  
4º semestre — 2026/02

**Autores:** Lucas Orlandi, Samuel Gralow, Diogo Rodrigo Nielsson e Felipe Kremer.

**Professores:** Otilia, Fran e Roberson.

---

## 1. Resumo

O Next Drive tem como objetivo informatizar o processo de aluguel de veículos, permitindo o gerenciamento de clientes, funcionários, veículos, categorias, lojas, reservas, locações, pagamentos e multas.

O sistema busca organizar as operações da locadora, controlar a disponibilidade dos veículos e garantir a integridade das informações armazenadas no banco de dados.

O projeto integra três disciplinas:

- **Programação II (PII):** desenvolvimento da aplicação Java, orientação a objetos, persistência e interface.
- **Engenharia de Software I (ESI):** modelagem do sistema por meio de diagramas UML e organização do desenvolvimento.
- **Banco de Dados II (BDII):** modelagem relacional, consultas SQL, índices, views, triggers, procedures, controle de acesso e backup.

## 2. O que o sistema faz

| Perfil | Funcionalidades |
|---|---|
| Cliente | Cadastro, consulta de disponibilidade, realização de reservas e acompanhamento das locações. |
| Atendente | Cadastro de clientes, reservas, cancelamentos, retirada, devolução, pagamentos e multas. |
| Gerente | Gerenciamento de funcionários, veículos, categorias, lojas, preços, manutenções e relatórios. |

### 2.1 Processo de locação

O funcionamento geral do sistema contempla:

1. Reserva de veículos conforme categoria, loja e período.
2. Verificação da disponibilidade.
3. Retirada do veículo, com registro de quilometragem, combustível e caução.
4. Devolução, com registro das condições do veículo.
5. Cálculo do valor final da locação.
6. Registro do pagamento e liberação da caução, quando aplicável.
7. Registro de multas vinculadas às locações.


## 3. Requisitos do sistema

### 3.1 Requisitos Funcionais

| Código | Descrição | Componente |
|---|---|---|
| RF01 | Criar índices para os campos de placa dos veículos, CPF/CNPJ dos clientes e datas das reservas. | BDII |
| RF02 | Permitir autenticação de usuários por e-mail e senha. | PII |
| RF03 | Permitir que o gerente cadastre, consulte, altere e desative funcionários. | PII |
| RF04 | Permitir o gerenciamento de clientes pessoa física e jurídica, incluindo cadastro próprio pelo cliente. | PII |
| RF05 | Permitir o cadastro e gerenciamento de veículos, categorias e lojas. | PII |
| RF06 | Disponibilizar consulta de veículos por categoria, loja e período. | BDII |
| RF07 | Permitir a realização de reservas pelo cliente no sistema e pelo atendente no balcão. | PII |
| RF08 | Permitir o cancelamento de reservas com justificativa, por meio de procedure. | BDII |
| RF09 | Permitir o registro da retirada do veículo, incluindo quilometragem, combustível e caução. | PII |
| RF10 | Registrar a devolução e calcular o valor final da locação por meio de procedure. | BDII |
| RF11 | Permitir o registro manual de pagamentos, informando valor, data e forma de pagamento (crédito, débito ou PIX). | PII |
| RF12 | Permitir o gerenciamento das manutenções dos veículos. | PII |
| RF13 | Disponibilizar o histórico de locações dos clientes. | PII |
| RF14 | Disponibilizar relatórios administrativos por meio de views. | BDII |
| RF15 | Impedir locações conflitantes para o mesmo veículo e atualizar seu status automaticamente por meio de trigger. | BDII |
| RF16 | Impedir locações quando o condutor for menor de 21 anos ou possuir CNH emitida há menos de 2 anos, por meio de trigger. | BDII |
| RF17 | Registrar auditoria das alterações realizadas em pagamentos e preços por meio de trigger. | BDII |
| RF18 | Controlar o acesso ao banco de dados por perfis, usuários e privilégios, além de permitir backup e restauração. | BDII |
| RF19 | Elaborar o diagrama de casos de uso com os atores Cliente, Atendente e Gerente. | ESI |
| RF20 | Elaborar o diagrama de classes, contemplando a herança de Pessoa e as classes relacionadas a pagamentos. | ESI |
| RF21 | Elaborar diagramas de sequência para os processos de reserva, retirada e devolução. | ESI |
| RF22 | Elaborar o diagrama de atividades do processo de locação. | ESI |
| RF23 | Elaborar diagramas de estados para Reserva, Veículo e Locação. | ESI |
| RF24 | Permitir o registro de multas de trânsito vinculadas às locações. | PII |

### 3.2 Requisitos Não Funcionais

| Código | Descrição |
|---|---|
| RNF01 | O sistema deve ser desenvolvido utilizando a linguagem Java. |
| RNF02 | O sistema deve disponibilizar uma interface acessível por navegador web. |
| RNF03 | O banco de dados utilizado deve ser PostgreSQL. |
| RNF04 | As consultas ao banco de dados devem utilizar comandos parametrizados. |
| RNF05 | As senhas dos usuários devem ser armazenadas de forma protegida. |
| RNF06 | O código Java deve seguir padrões de organização, nomenclatura e legibilidade. |
| RNF07 | As classes e métodos do projeto devem possuir documentação JavaDOC. |
| RNF08 | O desenvolvimento deve utilizar Git e GitHub para versionamento e GitHub Projects para organização das sprints. |
| RNF09 | Os diagramas UML devem permanecer consistentes com o código-fonte e o banco de dados. |


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

## 5. Estrutura do repositório

O projeto está organizado da seguinte forma:

```text
NextDrive/
├── docs/
│   ├── telas/
│   ├── uml/
│   │   ├── diagrama-de-atividades.md
│   │   ├── diagrama-de-casos-de-uso.md
│   │   ├── diagrama-de-classe.jpeg
│   │   ├── diagrama-de-estados.md
│   │   ├── diagrama-relacional.jpeg
│   │   ├── diagrama-sequencia-devolucao-pagamento.jpeg
│   │   ├── diagrama-sequencia-reserva.jpeg
│   │   └── diagrama-sequencia-retirada.jpeg
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
| 1 | `01_create_database.sql` | Criação do banco de dados (opcional; veja a seção 10.1). |
| 2 | `02_create_tables.sql` | Criação das tabelas, extensão `pgcrypto`, chaves e restrições. |
| 3 | `03_indices.sql` | Criação dos índices para otimização das consultas. |
| 4 | `04_views.sql` | Criação de views e da função `fn_veiculos_disponiveis`. |
| 5 | `05_dados_exemplo.sql` | Inserção de dados de exemplo. |
| 6 | `06_consultas_relatorios.sql` | Consultas SQL para relatórios administrativos (R01 a R13). |
| 7 | `07_roles_privilegios.sql` | Criação de grupos, usuários e privilégios de acesso. |
| 8 | `08_gatilhos.sql` | Criação de triggers para validações e auditoria. |
| 9 | `09_procedimentos.sql` | Criação de procedures e da função `fn_autenticar`. |
| 10 | `10_backup_restauração.sh` | Rotinas de backup e restauração do banco de dados. |
| 11 | `11_testes_regras.sql` | Execução de 34 verificações das regras de negócio. |

### 7.2 Recursos implementados no banco

O banco de dados utiliza os seguintes recursos:

- **Índices:** melhoram o desempenho das consultas.
- **Visualizações:** disponibilizam dados organizados para consultas e relatórios.
- **Gatilhos:** automatizam validações e auditorias.
- **Procedures:** executam operações e regras de negócio.
- **Funções:** permitem executar operações específicas, como consulta de disponibilidade e autenticação.
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

As capturas de tela apresentam as principais interfaces e funcionalidades desenvolvidas no sistema Next Drive.

Os arquivos de imagem estão organizados na pasta [`docs/telas/`](docs/telas/).

As telas documentam as principais operações do sistema, incluindo:

- Acesso e autenticação de usuários.
- Cadastro e gerenciamento de clientes.
- Gerenciamento de veículos e categorias.
- Consulta de disponibilidade e reservas.
- Retirada e devolução de veículos.
- Registro de pagamentos e multas.
- Consultas e relatórios administrativos.

As capturas também são utilizadas para ilustrar o funcionamento do sistema no artigo do projeto.

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
| 9 | `11_testes_regras.sql` (opcional: 34 verificações das regras; desfaz tudo no final) |

**Atenção:** o script `01_create_database.sql` cria um banco com outro nome (`next_drive`) e apaga o banco se ele já existir, por isso não é usado neste passo a passo. O arquivo `10_backup_restauração.sh` é um script de terminal e deve ser executado separadamente, não pelo DBeaver. Mais detalhes em [`sql/README.md`](sql/README.md) e [`docs/backup-restore.md`](docs/backup-restore.md).

**Usuários de exemplo** (senha `Senha@123`): cliente `ana.lima@example.com`, atendente `juliana.prado@nextdrive.example` e gerente `marcos.almeida@nextdrive.example`.

### 10.2 Aplicação Java

**Requisitos:** JDK 17 ou superior e Maven 3.8 ou superior.

1. Acesse a pasta `java/` do repositório.
2. Configure a conexão com o PostgreSQL conforme as instruções do arquivo [`java/README.md`](java/README.md).
3. Abra o terminal dentro da pasta `java/`.
4. Execute o comando para compilar e iniciar a aplicação:

```bash
mvn compile exec:java
```

5. Após iniciar o servidor, acesse pelo navegador:

http://localhost:8080

**Gerar o arquivo JAR:**

```bash
mvn package
```

**Executar o JAR:**

```bash
java -jar target/nextdrive-jar-with-dependencies.jar
```

Para mais informações sobre configuração e utilização, consulte `java/README.md`.

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
| Samuel Gralow | Pagamentos, multas, relatórios e documentação |
| Diogo Rodrigo Nielsson | Veículos, lojas e manutenção |
| Felipe Kremer | Clientes, funcionários e autenticação |

## 13. Considerações finais

O Next Drive integra os conhecimentos desenvolvidos nas disciplinas de Programação II, Engenharia de Software I e Banco de Dados II, aplicando conceitos de orientação a objetos, modelagem UML, persistência de dados e regras de negócio.

O projeto busca oferecer uma solução organizada para o gerenciamento de uma locadora de veículos, contemplando as principais etapas do processo de aluguel.
