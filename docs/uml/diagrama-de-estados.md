# Diagramas de Estados — Reserva, Veículo e Locação (RF23)

Cada transição indica o evento que a provoca e, entre parênteses, a procedure ou o trigger responsável.
Os estados correspondem exatamente aos valores das colunas `status` do banco.

## Reserva (`reserva.status`)

```mermaid
stateDiagram-v2
    [*] --> ATIVA : reserva criada no site ou no balcão
    ATIVA --> CANCELADA : cancelamento com justificativa (sp_cancelar_reserva)
    ATIVA --> EFETIVADA : retirada do veículo (sp_abrir_locacao)
    CANCELADA --> [*]
    EFETIVADA --> [*]

    note right of CANCELADA
        Justificativa e data do cancelamento
        são obrigatórias (RF08).
    end note
```

## Veículo (`veiculo.status`)

```mermaid
stateDiagram-v2
    [*] --> DISPONIVEL : veículo cadastrado pelo gerente
    DISPONIVEL --> LOCADO : retirada (trg_locacao_atualiza_veiculo)
    LOCADO --> DISPONIVEL : devolução (trg_locacao_atualiza_veiculo)
    DISPONIVEL --> MANUTENCAO : manutenção aberta (trg_manutencao_status_veiculo)
    MANUTENCAO --> DISPONIVEL : manutenção concluída (trg_manutencao_status_veiculo)
    DISPONIVEL --> INATIVO : desativado pelo gerente
    INATIVO --> DISPONIVEL : reativado pelo gerente

    note right of LOCADO
        Veículo locado não entra em manutenção
        e não pode ser alugado de novo (RN03, RN04).
    end note

    note right of MANUTENCAO
        Não aparece na consulta de
        disponibilidade (RN04, RN11).
    end note
```

## Locação (`locacao.status`)

```mermaid
stateDiagram-v2
    [*] --> ABERTA : retirada registrada (sp_abrir_locacao)
    ABERTA --> ENCERRADA : devolução com cálculo do valor (sp_registrar_devolucao)
    ENCERRADA --> ENCERRADA : pagamento parcial (sp_registrar_pagamento)
    ENCERRADA --> PAGA : total pago igual ao valor final (sp_registrar_pagamento)
    PAGA --> [*]

    note right of ENCERRADA
        Dados do contrato não podem mais
        ser alterados livremente (RN09).
    end note

    note right of PAGA
        Cautela LIBERADA se não houver multas pendentes;
        senão permanece RETIDA até a última multa ser paga.
    end note
```

## Resumo das transições permitidas

| Entidade | De | Para | Evento |
|---|---|---|---|
| Reserva | ATIVA | CANCELADA | Cancelamento com justificativa |
| Reserva | ATIVA | EFETIVADA | Retirada do veículo |
| Veículo | DISPONIVEL | LOCADO | Abertura da locação |
| Veículo | LOCADO | DISPONIVEL | Devolução |
| Veículo | DISPONIVEL | MANUTENCAO | Manutenção aberta |
| Veículo | MANUTENCAO | DISPONIVEL | Manutenção concluída |
| Veículo | DISPONIVEL, INATIVO | INATIVO, DISPONIVEL | Desativação e reativação pelo gerente |
| Locação | ABERTA | ENCERRADA | Devolução |
| Locação | ENCERRADA | PAGA | Quitação do valor final |

Qualquer outra transição é bloqueada pelo banco (por exemplo, alterar uma locação paga, ou colocar em manutenção um veículo locado).
