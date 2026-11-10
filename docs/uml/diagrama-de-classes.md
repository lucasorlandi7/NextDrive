# Diagrama de Classes — Next Drive

## Módulo de Frota

Classes responsáveis pelos veículos, categorias, lojas e manutenções. Os métodos de `Veiculo` correspondem às ações do diagrama de estados do veículo.

```mermaid
classDiagram
    direction LR

    class Categoria {
        -int idCategoria
        -String nome
        -String descricao
        -BigDecimal valorDiaria
        -BigDecimal valorCaucao
        -BigDecimal taxaOutraLoja
        +calcularDiarias(int dias) BigDecimal
    }

    class Loja {
        -int idLoja
        -String nome
        -String endereco
        -String cidade
        -String uf
        -String telefone
        -boolean ativa
    }

    class Veiculo {
        -int idVeiculo
        -String placa
        -String marca
        -String modelo
        -int ano
        -String cor
        -int quilometragem
        -StatusVeiculo status
        -Categoria categoria
        -Loja loja
        +estaDisponivel() boolean
        +vincularLocacao() void
        +liberar() void
        +abrirManutencao() void
        +inativar() void
    }

    class Manutencao {
        -int idManutencao
        -Veiculo veiculo
        -String descricao
        -LocalDate dataEntrada
        -LocalDate dataSaida
        -BigDecimal custo
        +concluir(LocalDate dataSaida, BigDecimal custo) void
        +estaConcluida() boolean
    }

    class StatusVeiculo {
        <<enumeration>>
        DISPONIVEL
        LOCADO
        MANUTENCAO
        INATIVO
    }

    Categoria "1" <-- "0..*" Veiculo : pertence a
    Loja "1" <-- "0..*" Veiculo : está em
    Veiculo "1" <-- "0..*" Manutencao : recebe
    Veiculo --> StatusVeiculo : status
```
