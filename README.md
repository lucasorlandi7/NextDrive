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
| RNF05 | O acesso ao banco de dados deve ser controlado por perfis e privilégios. |
| RNF06 | O código Java deve seguir padrões de organização, nomenclatura e legibilidade. |
| RNF07 | As classes e métodos do projeto devem possuir documentação JavaDOC. |
| RNF08 | O desenvolvimento deve utilizar Git e GitHub para versionamento. |
| RNF09 | Os diagramas UML devem permanecer consistentes com o código-fonte e o banco de dados. |
| RNF10 | O desenvolvimento deve utilizar GitHub Projects para organização das atividades. |

## 4. Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 17 | Desenvolvimento da aplicação |
| Spring Boot 3 | Desenvolvimento da aplicação web |
| Maven | Gerenciamento de dependências e execução do projeto |
| JDBC | Comunicação entre Java e PostgreSQL |
| JUnit 4 | Testes automatizados |
| PostgreSQL 16 | Banco de dados relacional |
| SQL / PLpgSQL | Consultas, funções, triggers e procedures |
| HTML / CSS | Interface web |
| Bootstrap / JavaScript | Interface e funcionalidades do sistema |
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
│   │   ├── casos-de-uso.jpg
│   │   ├── diagrama-de-classe.jpeg
│   │   ├── diagrama-sequencia-aluguel.jpeg
│   │   ├── diagrama-sequencia-devolucao-pagamento.jpeg
│   │   ├── diagrama-de-atividades.jpeg
│   │   ├── estados-veiculo.jpeg
│   │   ├── estados-reserva.jpeg
│   │   ├── estados-locação.jpeg
│   │   └── diagrama-relacional.jpeg
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
| Descrição do modelo de negócio | [Artigo](docs/artigo.md) (seção 2) |
| Diagrama relacional | [Imagem](docs/uml/diagrama-relacional.jpeg) · [Artigo](docs/artigo.md) (seção 4) |
| Dicionário de dados | [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md) |
| Scripts do banco de dados | [`sql/`](sql/) |
| Políticas de acesso | [`docs/politicas-de-acesso.md`](docs/politicas-de-acesso.md) |
| Backup e restauração | [`docs/backup-restore.md`](docs/backup-restore.md) |
| Diagramas UML | [`docs/uml/`](docs/uml/) · [seção 8 deste README](#8-diagramas-uml) |
| Aplicação Java | [`java/`](java/) |
| Capturas de tela | [`docs/telas/`](docs/telas/) |

## 7. Banco de dados

O banco de dados do Next Drive utiliza PostgreSQL 16 e armazena as informações necessárias para o funcionamento do sistema de aluguel de veículos.

A estrutura contempla clientes, veículos, categorias, reservas, locações e pagamentos, além de uma tabela de auditoria de pagamentos, utilizando chaves primárias, chaves estrangeiras e restrições de integridade.

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

- Modelo relacional: [`docs/uml/diagrama-relacional.jpeg`](docs/uml/diagrama-relacional.jpeg).
- Dicionário de dados: [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md).
- Políticas de acesso: [`docs/politicas-de-acesso.md`](docs/politicas-de-acesso.md).
- Procedimentos de backup: [`docs/backup-restore.md`](docs/backup-restore.md).

## 8. Diagramas UML

Os diagramas UML documentam a estrutura, o comportamento e os principais processos do sistema Next Drive. Clique no link para abrir a imagem do diagrama.

| Diagrama | Abrir | Requisito |
|---|---|---|
| Casos de uso | [Ver diagrama de casos de uso](docs/uml/casos-de-uso.jpg) | RF18 |
| Classes | [Ver diagrama de classes](docs/uml/diagrama-de-classe.jpeg) | RF19 |
| Sequência — aluguel | [Ver sequência do aluguel](docs/uml/diagrama-sequencia-aluguel.jpeg) | RF20 |
| Sequência — devolução e pagamento | [Ver sequência da devolução e pagamento](docs/uml/diagrama-sequencia-devolucao-pagamento.jpeg) | RF20 |
| Atividades — processo de locação | [Ver diagrama de atividades](docs/uml/diagrama-de-atividades.jpeg) | RF21 |
| Estados — veículo | [Ver estados do veículo](docs/uml/estados-veiculo.jpeg) | RF22 |
| Estados — reserva | [Ver estados da reserva](docs/uml/estados-reserva.jpeg) | RF22 |
| Estados — locação | [Ver estados da locação](docs/uml/estados-loca%C3%A7%C3%A3o.jpeg) | RF22 |
| Relacional | [Ver diagrama relacional](docs/uml/diagrama-relacional.jpeg) | BDII |

A explicação de cada diagrama está no [artigo](docs/artigo.md).

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
2. Abra o DBeaver e conecte-se ao servidor PostgreSQL.
3. Crie um banco chamado **`nextdrive`** (sem sublinhado), que é o nome usado pela aplicação Java. Você pode criá-lo pelo DBeaver ou executando o script `01_create_database.sql`.
4. Abra uma conexão no banco `nextdrive` e execute os scripts da pasta [`sql/`](sql/) na seguinte ordem, usando *Execute Script* (`Alt+X`):

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
| 9 | `11_testes_regras.sql` (opcional: 13 verificações das regras; desfaz tudo no final) |

**Atenção:** o arquivo `10_backup_restore.sh` é um script de terminal e deve ser executado separadamente, não pelo DBeaver. Mais detalhes em [`docs/backup-restore.md`](docs/backup-restore.md).

**Usuários de exemplo:** login no site: `admin` / `1234`. Usuários do banco: `atendente_nextdrive` e `gerente_nextdrive`.

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

Para mais informações, consulte [`java/README.md`](java/README.md).

## 11. Organização do desenvolvimento

O desenvolvimento do Next Drive foi organizado de forma colaborativa, com divisão das atividades entre os integrantes da equipe e acompanhamento das tarefas durante o projeto.

### 11.1 Planejamento das atividades

As atividades foram distribuídas considerando os módulos do sistema e os requisitos das disciplinas de Programação II, Engenharia de Software I e Banco de Dados II.

O planejamento e o acompanhamento das tarefas foram realizados por meio do GitHub Projects, permitindo organizar as atividades, acompanhar seu andamento e identificar as responsabilidades de cada integrante.


*Quadro Kanban do projeto:* [Next Drive - Planejamento](https://github.com/users/lucasorlandi7/projects/1)

### 11.2 Versionamento

O código-fonte e a documentação do projeto foram mantidos em um repositório GitHub, utilizando Git para controle de versão.

O versionamento permitiu registrar as alterações realizadas, integrar as contribuições dos integrantes e manter o histórico de desenvolvimento do sistema.

**Repositório do projeto:**  
https://github.com/lucasorlandi7/NextDrive

## 12. Desenvolvedores

| Integrante | Responsabilidades principais |
|---|---|
| Lucas Orlandi | Aplicação Java (classes de domínio, DAOs, controllers e telas), scripts SQL do banco, testes, diagramas de casos de uso e de estados e simplificação do escopo do sistema |
| Diogo Rodrigo Nielsson | Modelo relacional, dicionário de dados, políticas de acesso, backup e restore, relatórios (SELECTs), diagramas de classes, sequência e atividades, artigo, prints das telas e organização das tarefas no quadro Kanban |
| Samuel Gralow | Escrita, formatação e organização do README |
| Felipe Kremer | Link do quadro Kanban no README |

## 13. Considerações finais

O Next Drive integra os conhecimentos desenvolvidos nas disciplinas de Programação II, Engenharia de Software I e Banco de Dados II, aplicando conceitos de orientação a objetos, modelagem UML, persistência de dados e regras de negócio.

O projeto busca oferecer uma solução organizada para o gerenciamento de uma locadora de veículos, contemplando as principais etapas do processo de aluguel.
