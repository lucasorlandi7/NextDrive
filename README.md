
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
| RF16 | Impedir locações quando o contratante for menor de 21 anos ou possuir CNH emitida há menos de 2 anos, por meio de trigger. | BDII |
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
├── documentos/
│   ├── telas/
│   ├── uml/
│   │   ├── diagrama-de-atividades.md
│   │   ├── diagrama-de-casos-de-uso.md
│   │   ├── diagrama-de-classes.md
│   │   ├── diagrama-de-estados.md
│   │   ├── diagrama-de-sequencia.md
│   │   ├── diagrama-sequencia-reserva.jpeg
│   │   ├── diagrama-sequencia-retirada.jpeg
│   │   └── diagrama-sequencia-devolucao-pagamento.jpeg
│   ├── artigo.md
│   ├── backup-restauração.md
│   ├── diagrama-relacional.md
│   ├── diagrama-relacional.png
│   ├── dicionario-de-dados.md
│   └── politicas-de-acesso.md
├── Java/
├── SQL/
├── Enunciado_Trabalho_A1.pdf
├── Levantamento de Requisitos.docx
└── README.md
```


## 6. Itens do trabalho

| Item | Onde está |
|---|---|
| Enunciado do trabalho | `Enunciado_Trabalho_A1.pdf` |
| Levantamento de requisitos | `Levantamento de Requisitos.docx` |
| Descrição do modelo de negócio | `documentos/artigo.md` |
| Diagrama entidade-relacionamento | `documentos/diagrama-relacional.png` |
| Documentação do modelo relacional | `documentos/diagrama-relacional.md` |
| Diagramas UML | `documentos/uml/` |
| Diagrama de casos de uso | `documentos/uml/diagrama-de-casos-de-uso.md` |
| Diagramas de sequência | `documentos/uml/` |
| Capturas de tela | `documentos/telas/` |
| Dicionário de dados | `documentos/dicionario-de-dados.md` |
| Políticas de acesso | `documentos/politicas-de-acesso.md` |
| Scripts do banco de dados | `SQL/` |
| Aplicação Java | `Java/` |
| Artigo completo | `documentos/artigo.md` |



## 7. Banco de dados

O banco de dados do Next Drive utiliza PostgreSQL e armazena as informações necessárias para o funcionamento do sistema.

A estrutura contempla entidades relacionadas a clientes, funcionários, veículos, categorias, lojas, reservas, locações, pagamentos e multas.

Também são utilizados recursos de banco de dados para garantir a integridade e implementar regras de negócio:

- Chaves primárias e estrangeiras.
- Restrições de integridade.
- Índices.
- Views para consultas e relatórios.
- Triggers para validações e auditoria.
- Procedures para operações de negócio.
- Perfis de acesso e privilégios.
- Backup e restauração.

Os scripts de criação e configuração estão disponíveis na pasta `SQL/`.

## 8. Diagramas UML

Os diagramas UML documentam a estrutura e o comportamento do Next Drive.

Estão organizados na pasta `documentos/uml/`:

- Diagrama de casos de uso.
- Diagrama de classes.
- Diagramas de sequência.
- Diagrama de atividades.
- Diagramas de estados.

## 9. Telas do sistema

As capturas de tela da aplicação são armazenadas em `documentos/telas/`.

Elas demonstram as principais funcionalidades e interfaces desenvolvidas para o Next Drive.

## 10. Como executar

### 10.1 Banco de dados

1. Instale e configure o PostgreSQL.
2. Crie o banco de dados do projeto.
3. Abra o DBeaver e conecte-se ao banco.
4. Execute os scripts disponíveis na pasta `SQL/`, respeitando a ordem de execução definida nos próprios arquivos.

### 10.2 Aplicação Java

1. Configure o ambiente Java.
2. Abra o projeto localizado na pasta `Java/` utilizando o ambiente de desenvolvimento.
3. Configure a conexão com o PostgreSQL.
4. Execute a aplicação conforme as instruções do projeto.

**Observação:** os parâmetros de conexão devem corresponder à configuração do banco de dados utilizado no ambiente local.

## 11. Organização do desenvolvimento

O desenvolvimento é realizado de forma colaborativa, utilizando GitHub para versionamento e organização das atividades.

As disciplinas estão integradas no desenvolvimento:

- PII: implementação e orientação a objetos.
- ESI: análise, modelagem e documentação UML.
- BDII: modelagem, implementação e administração do banco de dados.

## 12. Desenvolvedores

| Integrante | Responsabilidades principais |
|---|---|
| Lucas Orlandi | Reservas e controle de locações |
| Samuel Garlow | Pagamentos, multas, relatórios e documentação |
| Diogo Rodrigo Nielsson | Veículos, lojas e manutenção |
| Felipe Kremer | Clientes, funcionários e autenticação |

## 13. Considerações finais

O Next Drive integra os conhecimentos desenvolvidos nas disciplinas de Programação II, Engenharia de Software I e Banco de Dados II, aplicando conceitos de orientação a objetos, modelagem UML, persistência de dados e regras de negócio.

O projeto busca oferecer uma solução organizada para o gerenciamento de uma locadora de veículos, contemplando as principais etapas do processo de aluguel.
