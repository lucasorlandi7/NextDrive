# Dicionário de Dados — Next Drive

Gerado a partir do banco PostgreSQL criado pelos scripts de `sql/` (tipos, nulidade, chaves e valores padrão refletem o banco real). Legenda de chaves: **PK** chave primária, **FK** chave estrangeira, **UK** valor único.

## Índice

- [`loja`](#loja)  Lojas (pontos de retirada e devolução)
- [`categoria_veiculo`](#categoria-veiculo)  Categorias de veículos com preços e taxas
- [`veiculo`](#veiculo)  Frota de veículos
- [`pessoa`](#pessoa)  Superclasse de clientes e funcionários (login por e-mail e senha)
- [`funcionario`](#funcionario)  Funcionários: atendentes e gerentes
- [`cliente`](#cliente)  Generalização de clientes PF e PJ
- [`cliente_pf`](#cliente-pf)  Cliente pessoa física (CPF e CNH)
- [`cliente_pj`](#cliente-pj)  Cliente pessoa jurídica (CNPJ e condutor responsável)
- [`reserva`](#reserva)  Reservas de veículos por categoria
- [`locacao`](#locacao)  Locações: retirada, devolução e valores calculados
- [`pagamento`](#pagamento)  Pagamentos de locações (superclasse)
- [`pagamento_cartao`](#pagamento-cartao)  Detalhes de pagamentos em cartão de crédito ou débito
- [`pagamento_pix`](#pagamento-pix)  Detalhes de pagamentos via PIX
- [`multa`](#multa)  Multas de trânsito vinculadas a uma locação
- [`manutencao`](#manutencao)  Manutenções de veículos
- [`auditoria`](#auditoria)  Trilha de auditoria de pagamentos e preços (preenchida por triggers)
- [`parametro_sistema`](#parametro-sistema)  Parâmetros usados nos cálculos de devolução

## loja

Lojas (pontos de retirada e devolução).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `nome` | VARCHAR(80) | sim | UK |  | Nome da loja (único). |
| `cidade` | VARCHAR(80) | sim |  |  | Cidade onde a loja está localizada. |
| `uf` | CHAR(2) | sim |  |  | Sigla do estado, com 2 letras maiúsculas. |
| `endereco` | VARCHAR(150) | sim |  |  | Endereço da loja. |
| `telefone` | VARCHAR(20) | não |  |  | Telefone de contato. |
| `ativa` | BOOLEAN | sim |  | true | Indica se a loja está em funcionamento. |

**Regras (CHECK):**

- `ck_loja_uf`: `((uf ~ '^[A-Z]{2}$'::text))`

## categoria_veiculo

Categorias de veículos com preços e taxas.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `nome` | VARCHAR(40) | sim | UK |  | Nome da categoria (Econômico, SUV etc.), único. |
| `descricao` | VARCHAR(200) | não |  |  | Descrição resumida da categoria. |
| `valor_diaria` | NUMERIC(10,2) | sim |  |  | Valor da diária em reais (RN07). Maior que zero. |
| `valor_hora_extra` | NUMERIC(10,2) | sim |  |  | Valor cobrado por hora de atraso na devolução (RN08). |
| `taxa_devolucao_outra_loja` | NUMERIC(10,2) | sim |  | 0 | Taxa cobrada quando o veículo é devolvido em loja diferente da retirada. |
| `valor_cautela` | NUMERIC(10,2) | sim |  | 0 | Valor retido no cartão de crédito como caução na retirada. |

**Regras (CHECK):**

- `ck_categoria_cautela`: `((valor_cautela >= (0)::numeric))`
- `ck_categoria_diaria`: `((valor_diaria > (0)::numeric))`
- `ck_categoria_hora`: `((valor_hora_extra >= (0)::numeric))`
- `ck_categoria_taxa`: `((taxa_devolucao_outra_loja >= (0)::numeric))`

## veiculo

Frota de veículos.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `placa` | VARCHAR(7) | sim | UK |  | Placa do veículo, padrão Mercosul (ex.: ABC1D23). Única (RN02). |
| `marca` | VARCHAR(40) | sim |  |  | Fabricante do veículo. |
| `modelo` | VARCHAR(60) | sim |  |  | Modelo do veículo. |
| `ano` | SMALLINT | sim |  |  | Ano de fabricação. |
| `cor` | VARCHAR(30) | sim |  |  | Cor do veículo. |
| `categoria_id` | INTEGER | sim | FK → categoria_veiculo |  | Categoria do veículo (define preços). |
| `loja_atual_id` | INTEGER | sim | FK → loja |  | Loja onde o veículo se encontra no momento. |
| `quilometragem` | INTEGER | sim |  | 0 | Quilometragem atual do odômetro. |
| `capacidade_tanque_litros` | SMALLINT | sim |  |  | Capacidade do tanque, usada no cálculo da diferença de combustível. |
| `status` | VARCHAR(12) | sim |  | 'DISPONIVEL'::character varying | DISPONIVEL, LOCADO, MANUTENCAO ou INATIVO. Mantido por triggers (RF15). |

**Regras (CHECK):**

- `ck_veiculo_ano`: `(((ano >= 1990) AND (ano <= 2100)))`
- `ck_veiculo_km`: `((quilometragem >= 0))`
- `ck_veiculo_placa`: `(((placa)::text ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$'::text))`
- `ck_veiculo_status`: `(((status)::text = ANY ((ARRAY['DISPONIVEL'::character varying, 'LOCADO'::character varying, 'MANUTENCAO'::character varying, 'INATIVO'::character varying])::text[])))`
- `ck_veiculo_tanque`: `((capacidade_tanque_litros > 0))`

## pessoa

Superclasse de clientes e funcionários (login por e-mail e senha).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `nome` | VARCHAR(120) | sim |  |  | Nome completo da pessoa ou nome fantasia da empresa. |
| `email` | VARCHAR(120) | sim | UK |  | E-mail, usado como login (RF02). Único. |
| `senha_hash` | TEXT | sim |  |  | Hash bcrypt da senha (RNF05). Nunca texto puro. |
| `telefone` | VARCHAR(20) | não |  |  | Telefone de contato. |
| `data_cadastro` | TIMESTAMP | sim |  | now() | Data e hora do cadastro. |

**Regras (CHECK):**

- `ck_pessoa_email`: `(((email)::text ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'::text))`

## funcionario

Funcionários: atendentes e gerentes.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pessoa_id` | INTEGER | sim | FK → pessoa, PK |  | Pessoa que é funcionário (herança de pessoa). |
| `cpf` | CHAR(11) | sim | UK |  | CPF do funcionário (11 dígitos, sem pontuação). Único. |
| `cargo` | VARCHAR(10) | sim |  |  | ATENDENTE ou GERENTE (RN10). |
| `loja_id` | INTEGER | sim | FK → loja |  | Loja onde o funcionário trabalha. |
| `data_admissao` | DATE | sim |  | CURRENT_DATE | Data de admissão. |
| `ativo` | BOOLEAN | sim |  | true | Indica se o funcionário está ativo. |

**Regras (CHECK):**

- `ck_funcionario_cargo`: `(((cargo)::text = ANY ((ARRAY['ATENDENTE'::character varying, 'GERENTE'::character varying])::text[])))`
- `ck_funcionario_cpf`: `((cpf ~ '^[0-9]{11}$'::text))`

## cliente

Generalização de clientes PF e PJ.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pessoa_id` | INTEGER | sim | FK → pessoa, PK |  | Pessoa que é cliente (herança de pessoa). |
| `tipo_cliente` | CHAR(2) | sim |  |  | PF (pessoa física) ou PJ (pessoa jurídica). |

**Regras (CHECK):**

- `ck_cliente_tipo`: `((tipo_cliente = ANY (ARRAY['PF'::bpchar, 'PJ'::bpchar])))`

## cliente_pf

Cliente pessoa física (CPF e CNH).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pessoa_id` | INTEGER | sim | PK |  | Cliente pessoa física. |
| `tipo_cliente` | CHAR(2) | sim | FK → cliente (composta) | 'PF'::bpchar | Sempre 'PF'; garante a consistência com a tabela cliente. |
| `cpf` | CHAR(11) | sim | UK |  | CPF (11 dígitos, sem pontuação). Único (RN01). |
| `data_nascimento` | DATE | sim |  |  | Data de nascimento; usada para exigir idade mínima de 21 anos (RF16). |
| `cnh_numero` | VARCHAR(11) | sim | UK |  | Número da CNH (11 dígitos). Único. |
| `cnh_data_emissao` | DATE | sim |  |  | Data de emissão da CNH; exige 2 anos de habilitação (RF16). |
| `cnh_validade` | DATE | sim |  |  | Validade da CNH; deve estar válida na retirada (RN05). |

**Regras (CHECK):**

- `ck_cliente_pf_cnh`: `(((cnh_numero)::text ~ '^[0-9]{11}$'::text))`
- `ck_cliente_pf_cpf`: `((cpf ~ '^[0-9]{11}$'::text))`
- `ck_cliente_pf_datas`: `(((cnh_data_emissao > data_nascimento) AND (cnh_validade > cnh_data_emissao)))`
- `ck_cliente_pf_tipo`: `((tipo_cliente = 'PF'::bpchar))`

## cliente_pj

Cliente pessoa jurídica (CNPJ e condutor responsável).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pessoa_id` | INTEGER | sim | PK |  | Cliente pessoa jurídica. |
| `tipo_cliente` | CHAR(2) | sim | FK → cliente (composta) | 'PJ'::bpchar | Sempre 'PJ'; garante a consistência com a tabela cliente. |
| `cnpj` | CHAR(14) | sim | UK |  | CNPJ (14 dígitos, sem pontuação). Único (RN01). |
| `razao_social` | VARCHAR(150) | sim |  |  | Razão social da empresa. |
| `condutor_id` | INTEGER | sim | FK → cliente_pf |  | Pessoa física responsável por dirigir os veículos da empresa (RF04). |

**Regras (CHECK):**

- `ck_cliente_pj_cnpj`: `((cnpj ~ '^[0-9]{14}$'::text))`
- `ck_cliente_pj_tipo`: `((tipo_cliente = 'PJ'::bpchar))`

## reserva

Reservas de veículos por categoria.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `cliente_id` | INTEGER | sim | FK → cliente |  | Cliente que fez a reserva. |
| `categoria_id` | INTEGER | sim | FK → categoria_veiculo |  | Categoria de veículo desejada. |
| `loja_retirada_id` | INTEGER | sim | FK → loja |  | Loja de retirada. |
| `loja_devolucao_id` | INTEGER | sim | FK → loja |  | Loja de devolução prevista. |
| `data_retirada_prevista` | TIMESTAMP | sim |  |  | Data e hora previstas para a retirada. |
| `data_devolucao_prevista` | TIMESTAMP | sim |  |  | Data e hora previstas para a devolução; posterior à retirada (RN06). |
| `valor_estimado` | NUMERIC(10,2) | sim |  |  | Estimativa de valor calculada na reserva. |
| `canal` | VARCHAR(6) | sim |  |  | SITE (cliente) ou BALCAO (atendente). BALCAO exige funcionário. |
| `funcionario_id` | INTEGER | não | FK → funcionario |  | Atendente que registrou a reserva no balcão; nulo para reservas pelo site. |
| `status` | VARCHAR(10) | sim |  | 'ATIVA'::character varying | ATIVA, CANCELADA ou EFETIVADA (virou locação). |
| `data_criacao` | TIMESTAMP | sim |  | now() | Data e hora em que a reserva foi criada. |
| `data_cancelamento` | TIMESTAMP | não |  |  | Data e hora do cancelamento (RF08). |
| `justificativa_cancelamento` | TEXT | não |  |  | Motivo do cancelamento; obrigatório quando CANCELADA (RF08). |

**Regras (CHECK):**

- `ck_reserva_canal`: `(((canal)::text = ANY ((ARRAY['SITE'::character varying, 'BALCAO'::character varying])::text[])))`
- `ck_reserva_canal_func`: `((((canal)::text = 'BALCAO'::text) = (funcionario_id IS NOT NULL)))`
- `ck_reserva_cancelamento`: `((((status)::text <> 'CANCELADA'::text) OR ((justificativa_cancelamento IS NOT NULL) AND (btrim(justificativa_cancelamento) <> ''::text) AND (data_cancelamento IS NOT NULL))))`
- `ck_reserva_periodo`: `((data_devolucao_prevista > data_retirada_prevista))`
- `ck_reserva_status`: `(((status)::text = ANY ((ARRAY['ATIVA'::character varying, 'CANCELADA'::character varying, 'EFETIVADA'::character varying])::text[])))`
- `ck_reserva_valor`: `((valor_estimado >= (0)::numeric))`

## locacao

Locações: retirada, devolução e valores calculados.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `reserva_id` | INTEGER | sim | FK → reserva, UK |  | Reserva que originou a locação (uma reserva gera no máximo uma locação). O cliente vem da reserva. |
| `veiculo_id` | INTEGER | sim | FK → veiculo |  | Veículo entregue ao cliente. |
| `condutor_id` | INTEGER | sim | FK → cliente_pf |  | Pessoa física que dirige o veículo (validada por trigger, RF16). |
| `funcionario_retirada_id` | INTEGER | sim | FK → funcionario |  | Atendente que registrou a retirada. |
| `funcionario_devolucao_id` | INTEGER | não | FK → funcionario |  | Atendente que registrou a devolução; nulo enquanto aberta. |
| `loja_retirada_id` | INTEGER | sim | FK → loja |  | Loja onde o veículo foi retirado. |
| `loja_devolucao_id` | INTEGER | não | FK → loja |  | Loja onde o veículo foi devolvido; nulo enquanto aberta. |
| `data_retirada` | TIMESTAMP | sim |  |  | Data e hora reais da retirada. |
| `data_devolucao_prevista` | TIMESTAMP | sim |  |  | Data e hora combinadas para a devolução. |
| `data_devolucao_real` | TIMESTAMP | não |  |  | Data e hora reais da devolução; posterior à retirada (RN06). |
| `km_saida` | INTEGER | sim |  |  | Quilometragem do veículo na retirada. |
| `km_chegada` | INTEGER | não |  |  | Quilometragem do veículo na devolução. |
| `combustivel_saida` | SMALLINT | sim |  |  | Nível de combustível na retirada, de 0 a 100 (% do tanque). |
| `combustivel_chegada` | SMALLINT | não |  |  | Nível de combustível na devolução, de 0 a 100 (% do tanque). |
| `cautela_valor` | NUMERIC(10,2) | sim |  | 0 | Valor retido no cartão de crédito (cautela). |
| `cautela_status` | VARCHAR(9) | sim |  | 'RETIDA'::character varying | RETIDA ou LIBERADA. |
| `valor_diaria_contratada` | NUMERIC(10,2) | sim |  |  | Valor da diária no momento da retirada (não muda se o preço da categoria mudar). |
| `valor_diarias` | NUMERIC(10,2) | não |  |  | Total das diárias contratadas (calculado na devolução). |
| `valor_horas_extras` | NUMERIC(10,2) | não |  |  | Cobrança por atraso na devolução (RN08). |
| `valor_combustivel` | NUMERIC(10,2) | não |  |  | Cobrança pela diferença de combustível. |
| `taxa_devolucao_outra_loja` | NUMERIC(10,2) | não |  |  | Taxa por devolução em loja diferente da retirada. |
| `desconto` | NUMERIC(10,2) | não |  |  | Desconto concedido pelo atendente. |
| `valor_final` | NUMERIC(10,2) | não |  |  | Valor final = diárias + horas extras + combustível + taxa - desconto. |
| `status` | VARCHAR(9) | sim |  | 'ABERTA'::character varying | ABERTA, ENCERRADA (devolvida) ou PAGA. Locação finalizada não é alterada livremente (RN09). |

**Regras (CHECK):**

- `ck_locacao_cautela`: `(((cautela_valor >= (0)::numeric) AND ((cautela_status)::text = ANY ((ARRAY['RETIDA'::character varying, 'LIBERADA'::character varying])::text[]))))`
- `ck_locacao_comb`: `((((combustivel_saida >= 0) AND (combustivel_saida <= 100)) AND ((combustivel_chegada IS NULL) OR ((combustivel_chegada >= 0) AND (combustivel_chegada <= 100)))))`
- `ck_locacao_encerrada`: `((((status)::text = 'ABERTA'::text) OR ((data_devolucao_real IS NOT NULL) AND (km_chegada IS NOT NULL) AND (combustivel_chegada IS NOT NULL) AND (loja_devolucao_id IS NOT NULL) AND (funcionario_devolucao_id IS NOT NULL) AND (valor_final IS NOT NULL))))`
- `ck_locacao_km`: `(((km_saida >= 0) AND ((km_chegada IS NULL) OR (km_chegada >= km_saida))))`
- `ck_locacao_prevista`: `((data_devolucao_prevista > data_retirada))`
- `ck_locacao_real`: `(((data_devolucao_real IS NULL) OR (data_devolucao_real > data_retirada)))`
- `ck_locacao_status`: `(((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'ENCERRADA'::character varying, 'PAGA'::character varying])::text[])))`
- `ck_locacao_total`: `(((valor_final IS NULL) OR (valor_final = ((((valor_diarias + valor_horas_extras) + valor_combustivel) + taxa_devolucao_outra_loja) - desconto))))`
- `ck_locacao_valores`: `(((valor_diaria_contratada > (0)::numeric) AND (COALESCE(valor_diarias, (0)::numeric) >= (0)::numeric) AND (COALESCE(valor_horas_extras, (0)::numeric) >= (0)::numeric) AND (COALESCE(valor_combustivel, (0)::numeric) >= (0)::numeric) AND (COALESCE(taxa_devolucao_outra_loja, (0)::numeric) >= (0)::numeric) AND (COALESCE(desconto, (0)::numeric) >= (0)::numeric)))`

## pagamento

Pagamentos de locações (superclasse).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `locacao_id` | INTEGER | sim | FK → locacao |  | Locação paga. |
| `valor` | NUMERIC(10,2) | sim |  |  | Valor pago, maior que zero. |
| `forma` | VARCHAR(14) | sim |  |  | CARTAO_CREDITO, CARTAO_DEBITO ou PIX (RN12). |
| `data_pagamento` | TIMESTAMP | sim |  | now() | Data e hora do pagamento. |
| `funcionario_id` | INTEGER | sim | FK → funcionario |  | Atendente que registrou o pagamento. |

**Regras (CHECK):**

- `ck_pagamento_forma`: `(((forma)::text = ANY ((ARRAY['CARTAO_CREDITO'::character varying, 'CARTAO_DEBITO'::character varying, 'PIX'::character varying])::text[])))`
- `ck_pagamento_valor`: `((valor > (0)::numeric))`

## pagamento_cartao

Detalhes de pagamentos em cartão de crédito ou débito.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pagamento_id` | INTEGER | sim | PK |  | Pagamento em cartão (herança de pagamento). |
| `forma` | VARCHAR(14) | sim | FK → pagamento (composta) |  | CARTAO_CREDITO ou CARTAO_DEBITO; igual à forma do pagamento. |
| `bandeira` | VARCHAR(20) | sim |  |  | Bandeira do cartão (Visa, Mastercard, Elo...). |
| `final_cartao` | CHAR(4) | sim |  |  | Últimos 4 dígitos do cartão (o número completo nunca é guardado). |
| `parcelas` | SMALLINT | sim |  | 1 | Número de parcelas (1 a 12); débito é sempre 1. |

**Regras (CHECK):**

- `ck_pag_cartao_final`: `((final_cartao ~ '^[0-9]{4}$'::text))`
- `ck_pag_cartao_forma`: `(((forma)::text = ANY ((ARRAY['CARTAO_CREDITO'::character varying, 'CARTAO_DEBITO'::character varying])::text[])))`
- `ck_pag_cartao_parcelas`: `((((parcelas >= 1) AND (parcelas <= 12)) AND (((forma)::text = 'CARTAO_CREDITO'::text) OR (parcelas = 1))))`

## pagamento_pix

Detalhes de pagamentos via PIX.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `pagamento_id` | INTEGER | sim | PK |  | Pagamento via PIX (herança de pagamento). |
| `forma` | VARCHAR(14) | sim | FK → pagamento (composta) | 'PIX'::character varying | Sempre 'PIX'. |
| `txid` | VARCHAR(35) | sim | UK |  | Identificador da transação PIX. Único. |
| `chave_pix` | VARCHAR(100) | não |  |  | Chave PIX que recebeu o valor. |

**Regras (CHECK):**

- `ck_pag_pix_forma`: `(((forma)::text = 'PIX'::text))`

## multa

Multas de trânsito vinculadas a uma locação.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `locacao_id` | INTEGER | sim | FK → locacao |  | Locação em que a infração ocorreu (RF24). |
| `descricao` | VARCHAR(200) | sim |  |  | Descrição da infração. |
| `valor` | NUMERIC(10,2) | sim |  |  | Valor da multa. |
| `data_infracao` | TIMESTAMP | sim |  |  | Data e hora da infração. |
| `data_registro` | TIMESTAMP | sim |  | now() | Data e hora em que a multa foi registrada no sistema. |
| `funcionario_id` | INTEGER | sim | FK → funcionario |  | Atendente que registrou a multa. |
| `status` | VARCHAR(8) | sim |  | 'PENDENTE'::character varying | PENDENTE ou PAGA. |

**Regras (CHECK):**

- `ck_multa_datas`: `((data_registro >= data_infracao))`
- `ck_multa_status`: `(((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'PAGA'::character varying])::text[])))`
- `ck_multa_valor`: `((valor > (0)::numeric))`

## manutencao

Manutenções de veículos.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | INTEGER | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `veiculo_id` | INTEGER | sim | FK → veiculo |  | Veículo em manutenção. |
| `funcionario_id` | INTEGER | sim | FK → funcionario |  | Funcionário que registrou a manutenção. |
| `tipo` | VARCHAR(10) | sim |  |  | PREVENTIVA ou CORRETIVA. |
| `descricao` | VARCHAR(200) | sim |  |  | Serviço realizado ou a realizar. |
| `data_entrada` | DATE | sim |  |  | Data em que o veículo entrou na oficina. |
| `data_saida` | DATE | não |  |  | Data em que o veículo saiu; nula enquanto em andamento. |
| `custo` | NUMERIC(10,2) | não |  |  | Custo da manutenção; informado ao concluir. |
| `status` | VARCHAR(12) | sim |  | 'EM_ANDAMENTO'::character varying | EM_ANDAMENTO ou CONCLUIDA. |

**Regras (CHECK):**

- `ck_manutencao_conclusao`: `((((status)::text = 'CONCLUIDA'::text) = ((data_saida IS NOT NULL) AND (custo IS NOT NULL))))`
- `ck_manutencao_custo`: `(((custo IS NULL) OR (custo >= (0)::numeric)))`
- `ck_manutencao_datas`: `(((data_saida IS NULL) OR (data_saida >= data_entrada)))`
- `ck_manutencao_status`: `(((status)::text = ANY ((ARRAY['EM_ANDAMENTO'::character varying, 'CONCLUIDA'::character varying])::text[])))`
- `ck_manutencao_tipo`: `(((tipo)::text = ANY ((ARRAY['PREVENTIVA'::character varying, 'CORRETIVA'::character varying])::text[])))`

## auditoria

Trilha de auditoria de pagamentos e preços (preenchida por triggers).

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `id` | BIGINT | sim | PK | gerado (identity) | Identificador único do registro (gerado automaticamente). |
| `tabela` | VARCHAR(40) | sim |  |  | Tabela auditada (pagamento ou categoria_veiculo). |
| `operacao` | CHAR(1) | sim |  |  | I (inserção), U (alteração) ou D (exclusão). |
| `registro_id` | INTEGER | sim |  |  | Identificador do registro alterado na tabela auditada. |
| `dados_antigos` | JSONB | não |  |  | Registro antes da alteração, em JSON. |
| `dados_novos` | JSONB | não |  |  | Registro depois da alteração, em JSON. |
| `usuario_db` | NAME | sim |  | SESSION_USER | Usuário do banco que fez a alteração (RF17). |
| `data_hora` | TIMESTAMP | sim |  | now() | Data e hora da alteração. |

**Regras (CHECK):**

- `ck_auditoria_oper`: `((operacao = ANY (ARRAY['I'::bpchar, 'U'::bpchar, 'D'::bpchar])))`

## parametro_sistema

Parâmetros usados nos cálculos de devolução.

| Coluna | Tipo | Obrigatório | Chave | Padrão | Descrição |
|---|---|---|---|---|---|
| `chave` | VARCHAR(40) | sim | PK |  | Nome do parâmetro. |
| `valor` | NUMERIC(12,2) | sim |  |  | Valor numérico do parâmetro. |
| `descricao` | VARCHAR(150) | sim |  |  | Para que o parâmetro é usado. |
