# Roteiro da apresentação oral — Next Drive

A nota oral é **individual** nas três disciplinas: cada integrante precisa conseguir explicar qualquer parte do trabalho, não só a que escreveu. Use este roteiro para dividir a fala e para o ensaio com perguntas.

## Divisão sugerida (cerca de 15 minutos)

| Tempo | Bloco | Quem | O que mostrar |
|---|---|---|---|
| 2 min | Problema e processo de locação | Integrante 1 | Seção 2 do README: as cinco etapas e os três atores |
| 3 min | Modelo de dados e regras no banco | Integrante 2 | Diagrama ER, normalização (por que `locacao` não tem `cliente_id`), herança de `pessoa` e `pagamento` |
| 3 min | Triggers, procedures e segurança do banco | Integrante 2 ou 3 | `trg_locacao_conflito`, `sp_registrar_devolucao`, matriz de privilégios, backup e restore |
| 2 min | UML e planejamento | Integrante 3 | Casos de uso, uma sequência (retirada) e o quadro do GitHub Projects |
| 4 min | Demonstração do sistema | Integrante 4 | Ver roteiro abaixo |
| 1 min | Conclusões e limitações | Todos | Seção 11 |

## Roteiro da demonstração

1. **Cliente:** entrar como `ana.lima@example.com`, consultar a frota, fazer uma reserva e ver a estimativa de valor.
2. **Atendente** (`juliana.prado@nextdrive.example`): abrir o painel, **registrar a retirada** da reserva (escolher o veículo, km, combustível, cautela).
3. Mostrar a **recusa**: tentar retirar para um condutor com menos de 21 anos e ler a mensagem que vem do banco.
4. **Devolução**: registrar km e combustível em outra loja e mostrar o valor final composto (diárias, horas extras, combustível, taxa de outra loja).
5. **Pagamento**: pagar parte em PIX, depois o restante em cartão; mostrar a locação como paga e a cautela.
6. **Multa**: registrar uma multa e mostrar a cautela retida; pagar a multa e mostrar a liberação.
7. **Gerente** (`marcos.almeida@nextdrive.example`): painel com faturamento e exportação CSV, e a tela de auditoria mostrando os pagamentos que acabaram de ser feitos.

Deixe o sistema já iniciado e os dados de exemplo restaurados antes de começar. Se quiser repetir a demonstração, restaure o banco com o backup.

## Perguntas prováveis e como responder

### Banco de Dados II

| Pergunta | Resposta curta |
|---|---|
| Por que as regras estão no banco e não no Java? | Para valerem em qualquer caminho de acesso. A aplicação só mostra mensagens melhores; a garantia é do PostgreSQL. |
| Como o banco impede duas locações para o mesmo veículo? | O trigger `trg_locacao_conflito` verifica, antes de inserir a locação, se há outra que se sobrepõe no período, ou se o veículo está indisponível ou em manutenção. |
| O modelo está na 3FN? Como você sabe? | Sim. Atributos atômicos (1FN), sem dependência parcial (2FN) e sem dependência transitiva (3FN). Exemplo: o cliente da locação vem da reserva, em vez de ser guardado de novo. |
| Existe alguma redundância? Por quê? | O valor final da locação e suas parcelas, e a diária contratada. São uma "foto" do momento da devolução, para que mudanças de preço não alterem contratos antigos. Uma *constraint* garante que o total fecha com as parcelas. |
| Qual a diferença entre *trigger* e *procedure*? | O *trigger* dispara sozinho em um evento (INSERT/UPDATE). A *procedure* é chamada (`CALL`) e executa um processo inteiro, como a devolução. |
| Como as senhas são protegidas? | *bcrypt* via `pgcrypto`. Nenhum perfil lê a coluna `senha_hash`; o login passa por `fn_autenticar`, que só devolve pessoa, nome e perfil. |
| Para que serve `SECURITY DEFINER` em `fn_veiculos_disponiveis`? | O atendente não pode ler `manutencao`, mas precisa saber quais veículos estão livres. A função roda com o privilégio do dono e devolve só o resultado. |
| Como é o backup? | `pg_dump` em formato custom, diário, com 7 dias de retenção e verificação com `pg_restore --list`; roles separadas. Foi testada a restauração em um banco novo. |
| Por que usar índices parciais? | Só reservas ativas entram na disponibilidade; o índice menor é mais rápido de manter e de consultar. |

### Engenharia de Software I

| Pergunta | Resposta curta |
|---|---|
| Qual metodologia usaram? | Scrum simplificado com Kanban no GitHub Projects, em cinco sprints semanais; cada tarefa é uma *issue* ligada a um *pull request*. |
| Como o UML ficou consistente com o código? | Estados do diagrama são os valores de `status` do banco; transições citam a *procedure* ou o *trigger*; as classes do diagrama são as classes Java. |
| Qual a diferença entre o diagrama de sequência e o de atividades? | A sequência mostra a troca de mensagens entre objetos/camadas em um fluxo (reserva, retirada, devolução). As atividades mostram o processo de negócio com decisões e responsáveis (raias). |
| Por que o diagrama de estados do veículo tem MANUTENCAO e INATIVO? | Veículo em manutenção não é oferecido (RN04, RN11); inativo foi desativado pelo gerente. Ambos saem da consulta de disponibilidade. |
| Como o Git foi usado? | Um *branch* por tarefa, *pull request* revisado por um colega, commits descritivos e `Closes #n` para fechar a *issue*. |

### Programação II

| Pergunta | Resposta curta |
|---|---|
| Onde estão herança e polimorfismo? | `Pessoa` → `Cliente` → `ClientePessoaFisica`/`ClientePessoaJuridica`, e `Pagamento` → `PagamentoCartao`/`PagamentoPix`. `getDocumento()` devolve CPF ou CNPJ conforme a subclasse. |
| Por que `Pessoa` é abstrata? | Não existe "pessoa genérica" no sistema: sempre é cliente ou funcionário. |
| Como evitam SQL *injection*? | Só `PreparedStatement` e `CallableStatement`, com parâmetros. Nenhuma consulta concatena dados do usuário. |
| Para que servem os três papéis de conexão? | Cada perfil da aplicação usa um usuário do banco com privilégios diferentes. Se uma tela for liberada por engano, o banco nega a operação. |
| Como tratam dois clientes reservando o último carro? | A reserva roda em transação com `pg_advisory_xact_lock` por categoria e loja; o segundo espera o primeiro terminar e então vê que não há veículo. |
| O que é CSRF e como se defendem? | Um site de terceiros fazendo o navegador enviar um formulário em nome do usuário. Cada formulário carrega um token da sessão, e o servidor recusa `POST` sem ele. |
| Qual é a funcionalidade extra? | Painel gerencial com gráfico de faturamento e exportação CSV. |
| Quais as limitações? | HTTP sem HTTPS, sem limite de tentativas de login, sessões em memória, sem *pool* de conexões. |

## Antes de apresentar

- [ ] Banco restaurado com os dados de exemplo e aplicação rodando em `localhost:8080`.
- [ ] Navegador com abas abertas: site, DBeaver (tabelas e um trigger), GitHub Projects.
- [ ] Cada integrante abriu e leu o trecho do código que vai explicar.
- [ ] Autoavaliação e avaliação dos colegas preenchidas (Programação II).
