# Dicionário de Dados — Next Drive

Banco **PostgreSQL 16**, com 7 tabelas. As seis primeiras são criadas em [`sql/02_create_tables.sql`](../sql/02_create_tables.sql); a `auditoria_pagamento` é criada em [`sql/07_gatilhos.sql`](../sql/07_gatilhos.sql), junto com o trigger que a preenche.

Legenda: **PK** = chave primária · **FK** = chave estrangeira · **UK** = valor único · **Nulo** = se o campo pode ficar vazio.

## categoria

Categorias de carro e o preço da diária de cada uma.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK**, gerado automaticamente |
| `nome` | `VARCHAR(60)` | Não | **UK**: não pode haver duas categorias com o mesmo nome (ex.: Econômico, SUV, Luxo) |
| `valor_diaria` | `NUMERIC(10,2)` | Não | Preço de uma diária; `CHECK (valor_diaria > 0)` |

## cliente

Pessoas que alugam os carros.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `nome` | `VARCHAR(120)` | Não | Nome completo |
| `cpf` | `VARCHAR(11)` | Não | **UK**: só os 11 números, sem pontos |
| `email` | `VARCHAR(120)` | Não | **UK** |
| `telefone` | `VARCHAR(20)` | Sim | Telefone de contato |
| `cnh_numero` | `VARCHAR(11)` | Não | Número da CNH |
| `cnh_validade` | `DATE` | Não | O trigger `trg_validar_cnh` impede reserva se a CNH vencer antes da devolução prevista |

## veiculo

Carros da frota.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `placa` | `VARCHAR(8)` | Não | **UK** |
| `modelo` | `VARCHAR(60)` | Não | Ex.: Fiat Mobi |
| `ano` | `INTEGER` | Não | `CHECK (ano BETWEEN 1990 AND 2100)` |
| `quilometragem` | `INTEGER` | Não | Padrão `0`; `CHECK (quilometragem >= 0)`; atualizada na devolução |
| `status` | `VARCHAR(12)` | Não | Padrão `DISPONIVEL`; só aceita `DISPONIVEL`, `LOCADO` ou `MANUTENCAO` |
| `categoria_id` | `INTEGER` | Não | **FK** → `categoria(id)` |

## reserva

Pedido do cliente para uma categoria em um período.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `cliente_id` | `INTEGER` | Não | **FK** → `cliente(id)` |
| `categoria_id` | `INTEGER` | Não | **FK** → `categoria(id)` |
| `data_retirada_prevista` | `DATE` | Não | Dia previsto para pegar o carro |
| `data_devolucao_prevista` | `DATE` | Não | `ck_reserva_datas`: tem que ser depois da retirada prevista |
| `status` | `VARCHAR(10)` | Não | Padrão `ATIVA`; só aceita `ATIVA`, `CANCELADA` ou `EFETIVADA` |
| `justificativa_cancelamento` | `VARCHAR(200)` | Sim | Preenchida pela procedure `sp_cancelar_reserva`, que exige o motivo |

## locacao

O aluguel em si: começa na retirada do carro e termina na devolução.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `reserva_id` | `INTEGER` | Não | **FK** → `reserva(id)` e **UK**: cada reserva gera no máximo uma locação |
| `veiculo_id` | `INTEGER` | Não | **FK** → `veiculo(id)`. O trigger `trg_validar_locacao` só aceita carro `DISPONIVEL` e sem outra locação aberta |
| `data_retirada` | `DATE` | Não | Dia em que o carro saiu |
| `data_devolucao_real` | `DATE` | Sim | Preenchida na devolução; não pode ser antes da retirada |
| `km_saida` | `INTEGER` | Não | Quilometragem na retirada |
| `km_chegada` | `INTEGER` | Sim | Preenchida na devolução; não pode ser menor que `km_saida` |
| `valor_final` | `NUMERIC(10,2)` | Sim | Calculado na devolução: diárias (mínimo 1) + 30% da diária por dia de atraso |
| `status` | `VARCHAR(10)` | Não | Padrão `ABERTA`; só aceita `ABERTA`, `ENCERRADA` ou `PAGA` |

As regras de devolução (datas, quilometragem e valor) ficam na procedure `sp_devolver_veiculo` e na função `fn_valor_final` ([`sql/08_procedimentos.sql`](../sql/08_procedimentos.sql)).

## pagamento

Pagamentos de uma locação, por cartão ou Pix. Uma locação pode ser paga em várias partes.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `locacao_id` | `INTEGER` | Não | **FK** → `locacao(id)` |
| `tipo` | `VARCHAR(6)` | Não | Só aceita `CARTAO` ou `PIX` |
| `valor` | `NUMERIC(10,2)` | Não | `CHECK (valor > 0)` |
| `data_pagamento` | `TIMESTAMP` | Não | Padrão: data e hora do momento do registro |
| `bandeira` | `VARCHAR(20)` | Sim | Obrigatória quando o tipo é `CARTAO` (`ck_pagamento_tipo`) |
| `txid` | `VARCHAR(35)` | Sim | Código da transação Pix; obrigatório quando o tipo é `PIX` (`ck_pagamento_tipo`) |

## auditoria_pagamento

Histórico de tudo o que acontece com os pagamentos. É preenchida só pelo trigger `trg_auditar_pagamento`.

| Campo | Tipo | Nulo | Regra |
|---|---|:---:|---|
| `id` | `SERIAL` | Não | **PK** |
| `pagamento_id` | `INTEGER` | Não | Código do pagamento. **Sem FK de propósito**, para o histórico continuar existindo se o pagamento for excluído |
| `operacao` | `VARCHAR(6)` | Não | `INSERT`, `UPDATE` ou `DELETE` |
| `valor_antigo` | `NUMERIC(10,2)` | Sim | Valor antes da alteração (vazio na inclusão) |
| `valor_novo` | `NUMERIC(10,2)` | Sim | Valor depois da alteração (vazio na exclusão) |
| `usuario` | `VARCHAR(60)` | Não | Padrão: usuário do banco que fez a operação (`current_user`) |
| `data_hora` | `TIMESTAMP` | Não | Padrão: momento da operação |

## Views

| View | O que mostra |
|---|---|
| `vw_veiculos_disponiveis` | Carros com status `DISPONIVEL`, com a categoria e a diária |
| `vw_locacoes_abertas` | Locações em andamento, com cliente, carro e devolução prevista |
| `vw_faturamento_categoria` | Quantidade e total de pagamentos por categoria |

## Índices

Além dos índices criados automaticamente pelas chaves primárias e pelos campos únicos (`placa`, `cpf`, `email`, `nome` da categoria e `reserva_id` da locação), o script [`sql/03_indices.sql`](../sql/03_indices.sql) cria:

| Índice | Tabela (colunas) | Para que serve |
|---|---|---|
| `idx_cliente_nome` | `cliente (nome)` | Busca de cliente pelo nome |
| `idx_veiculo_status` | `veiculo (status)` | Listar carros por situação |
| `idx_reserva_datas` | `reserva (data_retirada_prevista, data_devolucao_prevista)` | Busca de reservas por período |
| `idx_locacao_veiculo` | `locacao (veiculo_id, status)` | Achar a locação aberta de um carro |
| `idx_pagamento_locacao` | `pagamento (locacao_id)` | Somar os pagamentos de uma locação |
