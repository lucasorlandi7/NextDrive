# Diagrama de Atividades — Processo de Contratação (RF22)

Fluxo completo da contratação de um veículo, da reserva ao pós-locação, com as responsabilidades divididas em
raias: **Cliente**, **Atendente** e **Sistema**. Os losangos são decisões; as regras citadas (RN e RF) estão nos
triggers e procedures do banco.

```mermaid
flowchart TD
    Inicio(("Início"))
    Fim(("Fim"))

    subgraph RAIA_CLIENTE["Cliente"]
        C1["Solicita a reserva: categoria, lojas e período"]
        C2["Comparece à loja de retirada"]
        C3["Utiliza o veículo"]
        C4["Devolve o veículo"]
        C5["Paga o valor final"]
    end

    subgraph RAIA_ATENDENTE["Atendente"]
        A1["Confere documentos e CNH"]
        A2["Registra a cautela no cartão de crédito"]
        A3["Escolhe o veículo da categoria e registra km e combustível de saída"]
        A4["Registra km, combustível e loja de devolução"]
        A5["Registra o pagamento: crédito, débito ou PIX"]
        A6["Registra multa de trânsito recebida depois"]
    end

    subgraph RAIA_SISTEMA["Sistema"]
        S1["Verifica a disponibilidade e calcula a estimativa de valor"]
        D1{"Há veículo disponível no período?"}
        S2["Informa a indisponibilidade"]
        S3["Registra a reserva como ATIVA"]
        D2{"Condutor com 21 anos ou mais, CNH com 2 anos ou mais e válida, veículo livre?"}
        S4["Recusa a retirada e mostra o motivo"]
        S5["Abre a locação: veículo LOCADO e reserva EFETIVADA"]
        S6["Calcula o valor final: diárias, horas extras, combustível, taxa de outra loja e desconto"]
        S7["Encerra a locação e devolve o veículo à frota"]
        D3{"Total pago é igual ao valor final?"}
        D4{"Há multas pendentes?"}
        S8["Locação PAGA e cautela RETIDA"]
        S9["Locação PAGA e cautela LIBERADA"]
        S10["Multa fica PENDENTE; ao pagar a última, a cautela é liberada"]
    end

    Inicio --> C1
    C1 --> S1
    S1 --> D1
    D1 -->|Não| S2
    S2 --> Fim
    D1 -->|Sim| S3
    S3 --> C2
    C2 --> A1
    A1 --> A2
    A2 --> A3
    A3 --> D2
    D2 -->|Não| S4
    S4 --> Fim
    D2 -->|Sim| S5
    S5 --> C3
    C3 --> C4
    C4 --> A4
    A4 --> S6
    S6 --> S7
    S7 --> C5
    C5 --> A5
    A5 --> D3
    D3 -->|Não, pagamento parcial| C5
    D3 -->|Sim| D4
    D4 -->|Sim| S8
    D4 -->|Não| S9
    S8 --> A6
    A6 --> S10
    S10 --> Fim
    S9 --> Fim
```

## Pontos de decisão e regras associadas

| Decisão | Regra | Onde é garantida |
|---|---|---|
| Há veículo disponível? | RN03, RN04, RN11, RF06 | `fn_veiculos_disponiveis` e reservas ativas na consulta de disponibilidade |
| Condutor e veículo válidos? | RF16, RN05, RN03, RN04 | Triggers `trg_locacao_valida_condutor` e `trg_locacao_conflito` |
| Valor final | RN06, RN07, RN08 | Procedure `sp_registrar_devolucao` |
| Pagamento quita a locação? | RN12 | Procedure `sp_registrar_pagamento` |
| Multas pendentes? | Processo de negócio (pós-locação) | Procedure de pagamento e trigger `trg_multa_libera_cautela` |
