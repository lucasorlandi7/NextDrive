```mermaid
stateDiagram-v2
    direction TB

    state "Disponibilizando Veículo" as Disponivel
    state "Atendendo Locação" as Locado
    state "Realizando Manutenção" as Manutencao
    state avaliacao <<choice>>

    Disponivel : entry / status = DISPONIVEL
    Disponivel : do / aguardarRetirada()
    Disponivel : transferir (loja) / alterarLoja(loja)
    Locado : entry / status = LOCADO
    Locado : prorrogar (data) / alterarDevolucao(data)
    Locado : exit / registrarKmECombustivel()
    Manutencao : entry / status = MANUTENCAO
    Manutencao : do / executarServico()
    Manutencao : exit / registrarCusto()

    [*] --> Disponivel : / cadastrar veículo
    Disponivel --> Locado : retirar (locação) [sem locação no período] / vincularLocacao()
    Manutencao --> Manutencao : incluir serviço (descrição, custo)
    Locado --> avaliacao : devolver (km, combustível)
    avaliacao --> Disponivel : [sem avarias] / liberar()
    avaliacao --> Manutencao : [com avarias] / abrirManutencao()
    Disponivel --> Manutencao : when (km ≥ km da revisão) / agendarRevisao()
    Manutencao --> Disponivel : concluir manutenção (custo) / liberar()
    Disponivel --> [*] : baixar da frota / inativar()
    Manutencao --> [*] : baixar da frota [sem conserto] / inativar()
```


| Estado | Descrição |
|:---|:---|
| Disponível | Veículo na loja, pronto para ser locado. |
| Locado | Veículo em uso por um cliente. |
| Em manutenção | Veículo em revisão ou conserto; não pode ser locado. |
| Inativo | Veículo retirado da frota; não pode ser locado nem voltar a operar. |

| De | Para | Evento | Condição |
|:---|:---|:---|:---|
| (início) | Disponível | cadastrar veículo | |
| Disponível | Locado | registrar retirada | sem locação conflitante |
| Locado | Disponível | registrar devolução | sem avarias |
| Locado | Em manutenção | registrar devolução | com avarias |
| Disponível | Em manutenção | enviar para manutenção | |
| Em manutenção | Disponível | registrar retorno da manutenção | |
| Disponível | Inativo | inativar veículo | |
| Em manutenção | Inativo | inativar veículo | sem conserto |
| Inativo | (fim) | | |
