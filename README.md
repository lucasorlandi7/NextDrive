# Next Drive

## Sistema de Aluguel de Veículos

O Next Drive é um sistema web desenvolvido para auxiliar no gerenciamento de uma locadora de veículos. O sistema tem como objetivo organizar o cadastro de clientes e veículos, o controle de reservas, as locações, os pagamentos e a consulta de relatórios.

## Objetivo

Desenvolver um sistema que facilite o controle das atividades de uma locadora de veículos, garantindo a organização das informações e o acompanhamento das reservas e locações.

## Processo de negócio

1. **Reserva:** o cliente, pelo site, ou o atendente, no balcão, informa data, hora e loja de retirada e de devolução, e escolhe a categoria desejada. O sistema verifica a disponibilidade e calcula uma estimativa do valor.
2. **Retirada (abertura da locação):** na loja, o atendente confere os documentos do cliente, registra a caução no cartão de crédito, escolhe um veículo disponível da categoria e registra a quilometragem e o nível de combustível de saída.
3. **Devolução (encerramento da locação):** o atendente registra a quilometragem e o nível de combustível de chegada. O sistema calcula o valor final, incluindo horas extras, diferença de combustível, taxa de devolução em outra loja e descontos.
4. **Pagamento:** o valor final é pago e a caução é liberada, caso não existam pendências.
5. **Pós-locação:** multas de trânsito recebidas depois são vinculadas à locação e cobradas do cliente. Veículos podem ser enviados para manutenção.

## Atores do Sistema

| Ator | Descrição |
|:---|:---|
| Cliente | Pessoa física ou jurídica que faz reservas e consulta suas locações pelo site. |
| Atendente | Funcionário da loja que cadastra clientes e realiza reservas, retiradas, devoluções, pagamentos e registro de multas. |
| Gerente | Funcionário que gerencia funcionários, frota, categorias, preços e lojas, e consulta relatórios. |

## Requisitos Funcionais
| Código | Descrição | Componente |
|:---|:---|:---|
| RF01 | O sistema deverá utilizar índices nas colunas mais consultadas, como placa do veículo, CPF/CNPJ do cliente e datas das reservas. | BDII |
| RF02 | O sistema deverá permitir o login de usuários com e-mail e senha. | PII |
| RF03 | O gerente deverá poder cadastrar, consultar, alterar e desativar funcionários. | PII |
| RF04 | O sistema deverá permitir o cadastro e gerenciamento de clientes pessoa física (CPF e CNH) ou jurídica (CNPJ e condutor responsável), inclusive pelo próprio cliente no site. | PII |
| RF05 | O sistema deverá permitir o cadastro e gerenciamento de veículos, categorias e lojas. | PII |
| RF06 | O sistema deverá consultar, por meio de uma view, a disponibilidade dos veículos por categoria, loja e período.| BDII |
| RF07 | O sistema deverá permitir que clientes (pelo site) e funcionários (no balcão) realizem reservas de veículos. | PII |
| RF08 | O sistema deverá permitir o cancelamento de reservas, com justificativa, por meio de uma procedure. | BDII |
| RF09 | O sistema deverá permitir registrar a retirada dos veículos, informando veículo, quilometragem, nível de combustível e caução. | PII |
| RF10 | O sistema deverá permitir registrar a devolução dos veículos, calculando o valor da locação por meio de uma procedure. | BDII |
| RF11 | O sistema deverá permitir o registro manual de pagamentos, informando o valor, a data e a forma de pagamento (cartão de crédito, débito ou PIX). | PII |
| RF12 | O sistema deverá permitir registrar e gerenciar a manutenção dos veículos. | PII |
| RF13 | O sistema deverá permitir consultar o histórico de locações dos clientes. | PII |
| RF14 | O sistema deverá gerar relatórios administrativos a partir de views. | BDII |
| RF15 | O sistema deverá impedir, via trigger, locações conflitantes para o mesmo veículo, mantendo seu status atualizado. | BDII |
| RF16 | O sistema deverá impedir, via trigger, locações para contratantes com menos de 21 anos ou com CNH emitida há menos de 2 anos. | BDII |
| RF17 | O sistema deverá registrar, via trigger, a auditoria das alterações em pagamentos e preços. | BDII |
| RF18 | O sistema deverá controlar o acesso ao banco por perfis de usuário e prover rotina de backup e restauração. | BDII |
| RF19 | O sistema deverá ter um diagrama de casos de uso com os atores Cliente, Atendente e Gerente. | ESI |
| RF20 | O sistema deverá ter um diagrama de classes com as heranças de Pessoa e de Pagamento. | ESI |
| RF21 | Os fluxos de reserva, retirada e devolução deverão ter diagramas de sequência. | ESI |
| RF22 | O processo de locação deverá ter um diagrama de atividades. | ESI |
| RF23 | As entidades Reserva, Veículo e Locação deverão ter diagramas de estados. | ESI |
| RF24 | O sistema deverá permitir registrar multas de trânsito vinculadas a uma locação. | PII |


## Requisitos Não Funcionais
| Código | Descrição | Componente |
|:---|:---|:---|
| RNF01 | O sistema deverá ser desenvolvido utilizando Java. | PII |
| RNF02 | A interface deverá ser acessível por navegador web. | PII |
| RNF03 | Os dados deverão ser armazenados no PostgreSQL. | BDII |
| RNF04 | O sistema deverá utilizar consultas parametrizadas para acesso ao banco de dados. | BDII |
| RNF05 | As senhas deverão ser armazenadas de forma protegida. | PII |
| RNF06 |O código deverá seguir padrões de organização e nomenclatura Java.| PII |
| RNF07 | O código deverá possuir documentação JavaDOC. | PII |
| RNF08 | O desenvolvimento deverá ser planejado em sprints no GitHub Projects, com versionamento no GitHub. | ESI |
| RNF09 | O sistema deverá ser modelado com diagramas UML (casos de uso, classes, sequência, atividades e estados), mantidos consistentes com o código e o banco. | ESI |

## Regras de Negócio

As regras de negócio definem as condições e restrições que deverão ser respeitadas durante a utilização do sistema.

| Código | Regra | Componente |
|:---|:---|:---|
| RN01 | Cada cliente deverá possuir um CPF (pessoa física) ou CNPJ (pessoa jurídica) único cadastrado. | BDII |
| RN02 | Cada veículo deverá possuir uma placa única. | BDII |
| RN03 | Um veículo não poderá estar vinculado a duas locações em períodos conflitantes. | BDII |
| RN04 | Um veículo não poderá ser alugado se estiver indisponível ou em manutenção. | BDII |
| RN05 | O cliente deverá possuir CNH válida para realizar a retirada do veículo. | BDII |
| RN06 | A data de devolução deverá ser posterior à data de retirada. | BDII |
| RN07 | O valor da locação será calculado com base no valor da diária e no período contratado. | BDII |
| RN08 | A devolução com atraso poderá gerar cobrança adicional. | BDII |
| RN09 | Uma locação finalizada não poderá ser alterada livremente. | BDII |
| RN10 | Apenas gerentes poderão cadastrar e gerenciar funcionários do sistema. | PII |
| RN11 | Veículos em manutenção não serão considerados na consulta de disponibilidade durante o período de manutenção. | BDII |
| RN12 | O registro de pagamento deverá informar a forma utilizada, podendo ser cartão de crédito, cartão de débito ou PIX. | PII |


## Tecnologias

- Java
- HTML, CSS e JavaScript
- PostgreSQL
- Git e GitHub

## Funcionalidades previstas

- Cadastro e consulta de clientes.
- Cadastro e consulta de veículos.
- Consulta de disponibilidade.
- Registro e cancelamento de reservas.
- Controle de locações e devoluções.
- Registro de pagamentos.
- Relatórios administrativos.

## Organização do projeto

O projeto será organizado em pastas para o código-fonte, scripts SQL, diagramas e documentação.

## Equipe

Projeto acadêmico desenvolvido em grupo para as disciplinas de Programação II, Engenharia de Software I e Banco de Dados II, do curso de Ciência da Computação da Unoesc.

## Status

Em desenvolvimento.
