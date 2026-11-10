# Plano de Atividades e Sprints — Next Drive (RNF08)

Metodologia: **Scrum simplificado com quadro Kanban no GitHub Projects**. O trabalho é dividido em sprints curtas
(uma semana), cada uma com um objetivo entregável. O quadro tem as colunas **Backlog → Sprint atual → Em andamento →
Em revisão → Concluído**, e cada cartão é uma *issue* do repositório, ligada ao commit ou *pull request* que a resolve.

> As datas abaixo são relativas (Semana 1, 2…). Ajuste-as para o calendário real e para o prazo de entrega combinado
> com os professores antes de criar o quadro.

## Papéis

| Papel | Responsabilidade | Quem |
|---|---|---|
| Product Owner | Priorizar o backlog conforme o enunciado e o professor | A definir pela equipe |
| Scrum Master | Conduzir as reuniões, remover bloqueios, cuidar do quadro | A definir pela equipe |
| Desenvolvedores | Banco, Java, documentação, testes | Todos |

## Regras de trabalho

- Toda tarefa vira uma *issue* com responsável e sprint.
- Ninguém faz commit direto na `main`: cada tarefa usa um *branch* (`feature/nome-da-tarefa`) e um *pull request* revisado por pelo menos um colega.
- Mensagens de commit descrevem a mudança (`Adiciona trigger de conflito de locação`).
- Reunião rápida (15 min) no início e revisão da sprint no final, com atualização do quadro.
- Definição de pronto: funciona, passou nos testes (`11_testes_regras.sql` para o banco), está documentado e foi revisado.

## Backlog por sprint

### Sprint 1 — Fundamentos e banco de dados
Objetivo: banco criado e documentado.

| Tarefa | Disciplina | Status |
|---|---|---|
| Levantamento de requisitos e regras de negócio | ESI | Concluído |
| Modelo relacional normalizado (3FN) e diagrama | BDII | Concluído |
| Script de criação das tabelas, constraints e índices | BDII | Concluído |
| Views e dados de exemplo | BDII | Concluído |
| Consultas dos relatórios (junções e views) | BDII | Concluído |

### Sprint 2 — Regras no banco e segurança
Objetivo: regras de negócio garantidas pelo banco.

| Tarefa | Disciplina | Status |
|---|---|---|
| Triggers (conflito de locação, condutor, auditoria, manutenção, cautela) | BDII | Concluído |
| Procedures (cancelar reserva, abrir locação, devolução, pagamento) | BDII | Concluído |
| Roles e políticas de acesso | BDII | Concluído |
| Backup e restore | BDII | Concluído |
| Testes das regras (`11_testes_regras.sql`) | BDII | Concluído |

### Sprint 3 — Modelagem de software
Objetivo: diagramas UML completos.

| Tarefa | Disciplina | Status |
|---|---|---|
| Diagrama de casos de uso | ESI | Concluído |
| Diagrama de classes | ESI | Concluído |
| Diagramas de sequência (reserva, retirada, devolução) | ESI | Concluído |
| Diagrama de atividades | ESI | Concluído |
| Diagramas de estados | ESI | Concluído |
| Plano de sprints e quadro no GitHub Projects | ESI | Em andamento |

### Sprint 4 — Sistema em Java
Objetivo: aplicação web funcionando sobre o banco.

| Tarefa | Disciplina | Status |
|---|---|---|
| Classes de domínio com herança, enums e JavaDoc | PII | A fazer |
| Camada de acesso (DAO com JDBC e procedures) | PII | A fazer |
| Login com perfis (cliente, atendente, gerente) | PII | A fazer |
| Telas: disponibilidade, reserva, retirada, devolução, pagamento | PII | A fazer |
| Cadastros: clientes, veículos, categorias, lojas, funcionários | PII | A fazer |
| Relatórios do gerente | PII | A fazer |
| Funcionalidade extra | PII | A fazer |

### Sprint 5 — Entrega
Objetivo: README/artigo, ensaio e entrega.

| Tarefa | Disciplina | Status |
|---|---|---|
| Prints das telas e do banco | Todas | A fazer |
| README no formato UNOESC/ABNT | Todas | A fazer |
| Revisão cruzada entre os integrantes | Todas | A fazer |
| Roteiro e ensaio da apresentação oral | Todas | A fazer |
| Autoavaliação e avaliação dos colegas | PII | A fazer |

## Como criar o quadro no GitHub (passo a passo)

1. No repositório, abra **Projects → New project → Board** e dê o nome *Next Drive*.
2. Crie as colunas: Backlog, Sprint atual, Em andamento, Em revisão, Concluído.
3. Em **Issues**, crie uma issue para cada linha das tabelas acima, com o responsável e o *label* da disciplina (`PII`, `ESI`, `BDII`).
4. Crie os *milestones* "Sprint 1" a "Sprint 5" e associe cada issue ao seu.
5. Mova os cartões no quadro conforme o andamento e feche a issue no *pull request* (`Closes #número`).
6. Ao final de cada sprint, tire um print do quadro: ele vai para o README como evidência da metodologia.
