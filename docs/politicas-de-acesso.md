# Políticas de Acesso — Next Drive

Implementadas em `sql/07_roles_privilegios.sql` (RF18) e testadas com logins reais de cada perfil.

## Perfis (grupos) e usuários de exemplo

| Grupo | Usuário de exemplo | Quem usa | Função |
|---|---|---|---|
| `grp_gerente` | `usr_gerente` | Gerente da locadora | Gerencia funcionários, frota, categorias, preços e lojas; consulta todos os relatórios. |
| `grp_atendente` | `usr_atendente` | Atendente de balcão | Cadastra clientes e opera reservas, retiradas, devoluções, pagamentos e multas. |
| `grp_auditor` | `usr_auditor` | Auditoria | Somente leitura da trilha de auditoria e dos relatórios. |
| `grp_site` | `usr_site` | Aplicação web (clientes) | Cadastro do próprio cliente e criação de reservas pelo site. |

> As senhas de exemplo (`Trocar@123`) servem só para testes. Troque-as com
> `ALTER ROLE usr_gerente PASSWORD '...';` antes de qualquer uso real.
> Os usuários do banco são diferentes dos usuários do sistema (tabela `pessoa`), que fazem login pela função `fn_autenticar`.

## Matriz de privilégios

Legenda: **S** = SELECT, **I** = INSERT, **U** = UPDATE, **X** = EXECUTE, — = sem acesso. **Nenhum perfil tem DELETE**.

| Objeto | Gerente | Atendente | Auditor | Site |
|---|---|---|---|---|
| `loja`, `categoria_veiculo`, `veiculo` | S I U | S | — | S (loja e categoria) |
| `parametro_sistema` | S I U | S | — | — |
| `pessoa` (sem `senha_hash`) | S I U* | S I U* | — | S I |
| `funcionario` | S I U | S (apenas `pessoa_id`, `cargo`, `loja_id`, `ativo`) | — | — |
| `cliente`, `cliente_pf`, `cliente_pj` | S I U | S I U | — | I |
| `reserva` | S I U | S I U | — | I |
| `locacao` | S I U | S I U | — | — |
| `pagamento`, `pagamento_cartao`, `pagamento_pix` | S I U | S I U | — | — |
| `multa` | S I U | S I U | — | — |
| `manutencao` | S I U | — | — | — |
| `auditoria` | **S** | — | **S** | — |
| Views de relatório | todas | disponibilidade, reservas, locações, saldo, histórico, multas pendentes | todas | só `vw_disponibilidade_frota` |
| Procedures `sp_*` | X | X | — | — |
| `fn_autenticar` (login) | X | X | X | X |
| `fn_veiculos_disponiveis` | X | X | — | X |

\* Em `pessoa`, o gerente altera nome, e-mail, senha e telefone; o atendente altera apenas nome, e-mail e telefone (cadastro de funcionários é exclusivo do gerente - RN10).

## Princípios aplicados

1. **Privilégio mínimo:** cada grupo recebe só o que o processo de negócio exige; tudo o mais é negado por padrão (`REVOKE ALL ... FROM PUBLIC`).
2. **Sem exclusão:** nenhuma tabela recebe `DELETE`. Locações e reservas mudam de status (`CANCELADA`, `ENCERRADA`) em vez de serem apagadas.
3. **Auditoria imutável:** só os triggers (`SECURITY DEFINER`) escrevem em `auditoria`. Nem o gerente consegue alterá-la.
4. **Senhas protegidas:** `pessoa.senha_hash` guarda bcrypt (`pgcrypto`) e nenhum perfil consegue ler a coluna. O login é feito por `fn_autenticar`, que devolve apenas pessoa, nome e perfil.
5. **Relatórios por views:** perfis consultam as views e não precisam de acesso direto às tabelas base.
6. **Dados só pelas regras:** o status do veículo é alterado exclusivamente por triggers, e não por usuários.

## Testes de verificação realizados

| Teste | Resultado esperado |
|---|---|
| Atendente executa `DELETE` em `reserva` | negado |
| Atendente lê `pessoa.senha_hash` | negado |
| Atendente altera preço de categoria | negado |
| Atendente lê `auditoria` | negado |
| Atendente consulta `vw_faturamento_mensal` | negado |
| Atendente executa o fluxo reserva → retirada → devolução → pagamento | permitido |
| Gerente consulta faturamento | permitido |
| Gerente executa `DELETE` em `veiculo` ou `UPDATE` em `auditoria` | negado |
| Auditor lê `auditoria` | permitido |
| Auditor insere reserva | negado |
| Site lê `veiculo` | negado |
| Site consulta disponibilidade e faz login | permitido |
| Site cancela reserva (`sp_cancelar_reserva`) | negado |

## Consulta dos privilégios no banco

```sql
SELECT grantee, table_name,
       string_agg(privilege_type, ', ' ORDER BY privilege_type) AS privilegios
  FROM information_schema.role_table_grants
 WHERE grantee LIKE 'grp_%' AND table_schema = 'public'
 GROUP BY grantee, table_name
 ORDER BY grantee, table_name;
```
