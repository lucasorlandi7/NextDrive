# Diagramas de Sequência — Next Drive (RF21)

Fluxos de **reserva**, **retirada** e **devolução**. As camadas seguem a arquitetura do código Java:
interface web → `Service` (regras de negócio) → `DAO` (JDBC, consultas parametrizadas) → PostgreSQL
(procedures e triggers dos scripts `09` e `08`).

## 1. Reserva (pelo site ou balcão) — RF06, RF07

```mermaid
sequenceDiagram
    actor U as Cliente ou Atendente
    participant W as Interface Web
    participant RS as ReservaService
    participant VD as VeiculoDAO
    participant RD as ReservaDAO
    participant DB as PostgreSQL

    U->>W: informa categoria, lojas e período
    W->>RS: consultarDisponibilidade(categoria, loja, inicio, fim)
    RS->>VD: veiculosDisponiveis(categoria, loja, inicio, fim)
    VD->>DB: SELECT * FROM fn_veiculos_disponiveis(?, ?, ?, ?)
    DB-->>VD: veículos livres (sem locação e sem manutenção)
    RS->>RD: contarReservasAtivas(categoria, loja, inicio, fim)
    RD->>DB: SELECT COUNT(*) FROM reserva WHERE status = 'ATIVA' AND período conflita
    DB-->>RD: quantidade reservada
    alt veículos livres maior que reservas ativas
        RS->>RS: calcularEstimativa(categoria, inicio, fim)
        RS-->>W: disponível com valor estimado
        W-->>U: mostra a estimativa
        U->>W: confirma a reserva
        W->>RS: criarReserva(dados)
        RS->>RD: inserir(reserva)
        RD->>DB: INSERT INTO reserva (...) VALUES (?, ?, ...)
        DB-->>RD: id da reserva
        RS-->>W: reserva confirmada
        W-->>U: exibe o comprovante da reserva
    else sem disponibilidade
        RS-->>W: indisponível no período
        W-->>U: sugere outro período, loja ou categoria
    end
```

## 2. Retirada do veículo (abertura da locação) — RF09, RF15, RF16

```mermaid
sequenceDiagram
    actor A as Atendente
    participant W as Interface Web
    participant LS as LocacaoService
    participant RD as ReservaDAO
    participant VD as VeiculoDAO
    participant LD as LocacaoDAO
    participant DB as PostgreSQL

    A->>W: busca a reserva do cliente
    W->>LS: buscarReservaAtiva(reservaId)
    LS->>RD: buscarPorId(reservaId)
    RD->>DB: SELECT ... FROM reserva WHERE id = ?
    DB-->>RD: reserva ATIVA
    A->>A: confere CNH e documentos, registra a cautela no cartão
    W->>LS: veiculosParaRetirada(reserva)
    LS->>VD: veiculosDisponiveis(categoria, loja, agora, devolucaoPrevista)
    VD->>DB: SELECT * FROM fn_veiculos_disponiveis(?, ?, ?, ?)
    DB-->>VD: veículos livres da categoria
    A->>W: escolhe o veículo, informa km e nível de combustível
    W->>LS: abrirLocacao(reserva, veiculo, condutor, km, combustivel)
    LS->>LD: abrir(...)
    LD->>DB: CALL sp_abrir_locacao(?, ?, ?, ?, ?, ?)
    DB->>DB: trigger valida conflito, veículo disponível e condutor
    alt validação aprovada
        DB->>DB: INSERT em locacao, veículo passa a LOCADO, reserva EFETIVADA
        DB-->>LD: concluído
        LD-->>LS: locação aberta
        LS-->>W: retirada registrada
        W-->>A: exibe o contrato com valor da diária e cautela
    else regra violada (RN03, RN04, RN05, RF16)
        DB-->>LD: erro com a regra violada
        LD-->>LS: exceção de regra de negócio
        LS-->>W: motivo da recusa
        W-->>A: mostra a mensagem (por exemplo, condutor com menos de 21 anos)
    end
```

## 3. Devolução e pagamento — RF10, RF11

```mermaid
sequenceDiagram
    actor A as Atendente
    participant W as Interface Web
    participant LS as LocacaoService
    participant LD as LocacaoDAO
    participant PD as PagamentoDAO
    participant DB as PostgreSQL

    A->>W: informa km de chegada, combustível e loja de devolução
    W->>LS: registrarDevolucao(locacaoId, km, combustivel, loja, desconto)
    LS->>LD: registrarDevolucao(...)
    LD->>DB: CALL sp_registrar_devolucao(?, ?, ?, ?, ?, ?)
    DB->>DB: calcula diárias, horas extras, combustível e taxa de outra loja
    DB->>DB: locação ENCERRADA, trigger devolve o veículo à frota
    DB-->>LD: valor final
    LD-->>LS: locação encerrada
    LS-->>W: valor final detalhado
    W-->>A: exibe o valor a pagar

    loop até quitar o valor final
        A->>W: informa valor e forma (crédito, débito ou PIX)
        W->>LS: registrarPagamento(locacaoId, pagamento)
        LS->>PD: inserir(pagamento)
        PD->>DB: CALL sp_registrar_pagamento(?, ?, ?, ?, ...)
        DB->>DB: grava o pagamento, o trigger de auditoria registra a operação
        DB-->>PD: concluído
    end

    alt total pago igual ao valor final
        DB->>DB: locação PAGA
        alt existem multas pendentes
            DB->>DB: cautela permanece RETIDA
        else sem pendências
            DB->>DB: cautela LIBERADA
        end
        LS-->>W: locação quitada
        W-->>A: exibe o recibo
    end
```

**Observações**
- Regras críticas (conflito de locação, idade e CNH, cálculo do valor, cautela) ficam no banco, por triggers e procedures; o Java apenas chama as procedures e traduz os erros em mensagens (RNF04, consultas parametrizadas).
- Toda comunicação com o banco usa `PreparedStatement` e `CallableStatement`.
