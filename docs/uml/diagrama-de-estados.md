# Diagramas de Estados — Next Drive

## Classe Veiculo

Mostra os estados por que passa um veículo, do cadastro até a baixa da frota. As transições seguem a notação `evento [condição de guarda] / ação`.

```mermaid
stateDiagram-v2
    direction LR

    state "Disponibilizando Veículo" as Disponivel
    state "Atendendo Locação" as Locado
    state "Realizando Manutenção" as Manutencao

    [*] --> Disponivel : / cadastrar veículo
    Disponivel --> Locado : retirar [sem locação no período] / vincularLocacao()
    Locado --> Disponivel : devolver / liberar()
    Disponivel --> Manutencao : enviar para manutenção / abrirManutencao()
    Manutencao --> Disponivel : concluir manutenção / liberar()
    Disponivel --> [*] : baixar da frota / inativar()
    Manutencao --> [*] : baixar da frota [sem conserto] / inativar()
```

| Estado | Status no banco | Descrição |
|:---|:---|:---|
| Disponibilizando Veículo | `DISPONIVEL` | Veículo na loja, pronto para ser retirado. |
| Atendendo Locação | `LOCADO` | Veículo em uso por um cliente. |
| Realizando Manutenção | `MANUTENCAO` | Veículo em revisão ou conserto; não pode ser locado. |
| Estado final | `INATIVO` | Veículo baixado da frota. |
