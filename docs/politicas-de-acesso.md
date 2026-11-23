# Políticas de Acesso — Next Drive

O controle de acesso ao banco é feito em [`sql/09_roles_privilegios.sql`](../sql/09_roles_privilegios.sql), que deve ser executado depois dos scripts de tabelas, views, gatilhos e procedimentos.

A ideia é a mesma de uma locadora de verdade: o **atendente** faz o dia a dia do balcão, mas não apaga nada; o **gerente** também cuida dos cadastros da frota, pode excluir cadastros e é o único que vê a auditoria e o faturamento.

## Grupos e usuários

As permissões são dadas aos **grupos**. Cada **usuário** entra em um grupo e herda o que o grupo pode fazer. Assim, para contratar um novo atendente basta criar o usuário dentro de `grupo_atendente`.

| Grupo | Usuário de exemplo | Senha de exemplo | Quem é |
|---|---|---|---|
| `grupo_atendente` | `atendente_nextdrive` | `Atendente@123` | Funcionário do balcão: cadastra clientes, faz aluguéis, devoluções e pagamentos |
| `grupo_gerente` | `gerente_nextdrive` | `Gerente@123` | Responsável pela loja: cuida da frota e dos preços, corrige cadastros e acompanha pagamentos |

Os grupos são criados com `NOLOGIN` (ninguém entra no banco "como grupo"); só os usuários têm `LOGIN`. O script verifica se cada um já existe antes de criar, então pode ser rodado mais de uma vez.

## O que cada um pode fazer

S = consultar (`SELECT`) · I = incluir (`INSERT`) · A = alterar (`UPDATE`) · E = excluir (`DELETE`) · — = sem acesso

| Tabela ou view | Atendente | Gerente |
|---|:---:|:---:|
| `categoria` | S | S I A E |
| `veiculo` | S A | S I A E |
| `cliente` | S I A | S I A E |
| `reserva` | S I A | S I A E |
| `locacao` | S I A | S I A |
| `pagamento` | S I A | S I A |
| `auditoria_pagamento` | — | S |
| `vw_veiculos_disponiveis` | S | S |
| `vw_locacoes_abertas` | S | S |
| `vw_faturamento_categoria` | — | S |

Os dois grupos também podem usar as sequências dos `id` (`USAGE, SELECT`), sem o que não conseguiriam incluir registros.

## Regras que essa tabela garante

- **O atendente não exclui nada.** Ele só pode alterar a situação de um carro (por exemplo, mandar para manutenção), mas não cadastra nem apaga carros e categorias.
- **Ninguém exclui locações e pagamentos**, nem o gerente. São o histórico financeiro da locadora; um pagamento errado é corrigido com alteração, e a alteração fica registrada na auditoria.
- **A auditoria é só leitura e só para o gerente.** Nenhum grupo pode incluir ou alterar `auditoria_pagamento` diretamente. Ela é preenchida pelo trigger `trg_auditar_pagamento`, cuja função roda com `SECURITY DEFINER` (com as permissões do dono do banco). Por isso o pagamento feito pelo atendente também é auditado, mesmo ele não tendo acesso à tabela.
- **O faturamento é só do gerente**: o atendente não lê a view `vw_faturamento_categoria`.
- **Procedures:** `sp_devolver_veiculo` e `sp_cancelar_reserva` podem ser chamadas pelos dois grupos e rodam com as permissões de quem chama. Funcionam para ambos porque os dois podem alterar `locacao` e `reserva`.

## Como conferir no DBeaver

1. Crie uma conexão nova com o usuário `atendente_nextdrive` (senha `Atendente@123`) no banco `nextdrive`.
2. Rode os comandos abaixo:

```sql
SELECT * FROM cliente;                    -- funciona
DELETE FROM cliente WHERE id = 3;         -- ERRO: permission denied for table cliente
SELECT * FROM auditoria_pagamento;        -- ERRO: permission denied for table auditoria_pagamento
SELECT * FROM vw_faturamento_categoria;   -- ERRO: permission denied for view vw_faturamento_categoria
```

3. Faça o mesmo com `gerente_nextdrive` (senha `Gerente@123`): as consultas funcionam, mas `DELETE FROM pagamento WHERE id = 1;` dá erro de permissão.

## Cuidados

- As senhas acima são **só de exemplo**. Em uso real, troque com `ALTER ROLE atendente_nextdrive PASSWORD 'nova_senha';`.
- A aplicação Java conecta com o usuário `postgres` (arquivo `SingleConnection.java`). Para a aplicação também respeitar estas regras, basta trocar o usuário e a senha nesse arquivo por `atendente_nextdrive` ou `gerente_nextdrive`.
- O backup (`pg_dump`) não leva os usuários do servidor. Ao restaurar em outro computador, rode o `09_roles_privilegios.sql` depois da restauração (veja [`backup-restore.md`](backup-restore.md)).
