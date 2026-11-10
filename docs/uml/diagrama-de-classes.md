# Diagrama de Classes — Next Drive (RF20)

Modelo de domínio em Java (pacote `br.edu.unoesc.nextdrive.model`), consistente com o banco de dados
(RNF09). As duas hierarquias de herança pedidas no RF20 são destacadas:

- **Pessoa** → `Cliente` → `ClientePessoaFisica` / `ClientePessoaJuridica`, e **Pessoa** → `Funcionario`;
- **Pagamento** → `PagamentoCartao` / `PagamentoPix`.

```mermaid
classDiagram
    direction TB

    class Pessoa {
        <<abstract>>
        -Integer id
        -String nome
        -String email
        -String senhaHash
        -String telefone
        -LocalDateTime dataCadastro
        +getPerfil() String
    }

    class Cliente {
        <<abstract>>
        +getDocumento() String
        +getCondutor() ClientePessoaFisica
    }

    class ClientePessoaFisica {
        -String cpf
        -LocalDate dataNascimento
        -String cnhNumero
        -LocalDate cnhDataEmissao
        -LocalDate cnhValidade
        +getIdade(LocalDate data) int
        +cnhValidaEm(LocalDate data) boolean
        +habilitadoHaAnos(int anos, LocalDate data) boolean
        +getDocumento() String
        +getCondutor() ClientePessoaFisica
    }

    class ClientePessoaJuridica {
        -String cnpj
        -String razaoSocial
        -ClientePessoaFisica condutorResponsavel
        +getDocumento() String
        +getCondutor() ClientePessoaFisica
    }

    class Funcionario {
        -String cpf
        -Cargo cargo
        -LocalDate dataAdmissao
        -boolean ativo
        +isGerente() boolean
        +getPerfil() String
    }

    class Loja {
        -Integer id
        -String nome
        -String cidade
        -String uf
        -String endereco
        -String telefone
        -boolean ativa
    }

    class CategoriaVeiculo {
        -Integer id
        -String nome
        -String descricao
        -BigDecimal valorDiaria
        -BigDecimal valorHoraExtra
        -BigDecimal taxaDevolucaoOutraLoja
        -BigDecimal valorCautela
        +calcularEstimativa(LocalDateTime inicio, LocalDateTime fim) BigDecimal
    }

    class Veiculo {
        -Integer id
        -String placa
        -String marca
        -String modelo
        -int ano
        -String cor
        -int quilometragem
        -int capacidadeTanqueLitros
        -StatusVeiculo status
        +estaDisponivel() boolean
    }

    class Reserva {
        -Integer id
        -LocalDateTime dataRetiradaPrevista
        -LocalDateTime dataDevolucaoPrevista
        -BigDecimal valorEstimado
        -Canal canal
        -StatusReserva status
        -LocalDateTime dataCriacao
        -LocalDateTime dataCancelamento
        -String justificativaCancelamento
        +cancelar(String justificativa) void
    }

    class Locacao {
        -Integer id
        -LocalDateTime dataRetirada
        -LocalDateTime dataDevolucaoPrevista
        -LocalDateTime dataDevolucaoReal
        -int kmSaida
        -Integer kmChegada
        -int combustivelSaida
        -Integer combustivelChegada
        -BigDecimal cautelaValor
        -boolean cautelaLiberada
        -BigDecimal valorDiariaContratada
        -BigDecimal valorDiarias
        -BigDecimal valorHorasExtras
        -BigDecimal valorCombustivel
        -BigDecimal taxaDevolucaoOutraLoja
        -BigDecimal desconto
        -BigDecimal valorFinal
        -StatusLocacao status
        +totalPago() BigDecimal
        +saldoDevedor() BigDecimal
    }

    class Pagamento {
        <<abstract>>
        -Integer id
        -BigDecimal valor
        -LocalDateTime dataPagamento
        +getForma() FormaPagamento
        +descrever() String
    }

    class PagamentoCartao {
        -FormaPagamento forma
        -String bandeira
        -String finalCartao
        -int parcelas
        +getForma() FormaPagamento
        +descrever() String
    }

    class PagamentoPix {
        -String txid
        -String chavePix
        +getForma() FormaPagamento
        +descrever() String
    }

    class Multa {
        -Integer id
        -String descricao
        -BigDecimal valor
        -LocalDateTime dataInfracao
        -LocalDateTime dataRegistro
        -boolean paga
    }

    class Manutencao {
        -Integer id
        -TipoManutencao tipo
        -String descricao
        -LocalDate dataEntrada
        -LocalDate dataSaida
        -BigDecimal custo
        -boolean concluida
    }

    class Cargo {
        <<enumeration>>
        ATENDENTE
        GERENTE
    }
    class StatusVeiculo {
        <<enumeration>>
        DISPONIVEL
        LOCADO
        MANUTENCAO
        INATIVO
    }
    class StatusReserva {
        <<enumeration>>
        ATIVA
        CANCELADA
        EFETIVADA
    }
    class StatusLocacao {
        <<enumeration>>
        ABERTA
        ENCERRADA
        PAGA
    }
    class FormaPagamento {
        <<enumeration>>
        CARTAO_CREDITO
        CARTAO_DEBITO
        PIX
    }

    Pessoa <|-- Cliente
    Pessoa <|-- Funcionario
    Cliente <|-- ClientePessoaFisica
    Cliente <|-- ClientePessoaJuridica
    Pagamento <|-- PagamentoCartao
    Pagamento <|-- PagamentoPix

    ClientePessoaJuridica --> ClientePessoaFisica : condutor responsável
    Funcionario --> Loja : trabalha em
    Veiculo --> CategoriaVeiculo : pertence a
    Veiculo --> Loja : está em
    Reserva --> Cliente : feita por
    Reserva --> CategoriaVeiculo : categoria desejada
    Reserva --> Loja : retirada e devolução
    Reserva --> Funcionario : registrada por
    Locacao --> Reserva : origina-se de
    Locacao --> Veiculo : veículo
    Locacao --> ClientePessoaFisica : condutor
    Locacao "1" --> "0..*" Pagamento : recebe
    Locacao "1" --> "0..*" Multa : gera
    Veiculo "1" --> "0..*" Manutencao : passa por

    Funcionario --> Cargo
    Veiculo --> StatusVeiculo
    Reserva --> StatusReserva
    Locacao --> StatusLocacao
    Pagamento --> FormaPagamento
```

## Conceitos de POO aplicados

| Conceito | Onde aparece |
|---|---|
| **Encapsulamento** | Todos os atributos são `private`, acessados por getters e setters. Regras como `Reserva.cancelar` e `Locacao.saldoDevedor` ficam dentro das classes. |
| **Herança** | `Pessoa` → `Cliente` → `ClientePessoaFisica`/`ClientePessoaJuridica`; `Pessoa` → `Funcionario`; `Pagamento` → `PagamentoCartao`/`PagamentoPix`. |
| **Polimorfismo** | `Cliente.getDocumento()` devolve CPF ou CNPJ conforme a subclasse; `Cliente.getCondutor()` devolve a própria pessoa física ou o condutor responsável da empresa; `Pagamento.descrever()` e `getForma()` mudam em cada tipo de pagamento. |
| **Classe abstrata** | `Pessoa`, `Cliente` e `Pagamento` não são instanciadas diretamente. |
| **Enumerações** | `Cargo`, `StatusVeiculo`, `StatusReserva`, `StatusLocacao`, `FormaPagamento` (além de `Canal`, `TipoManutencao` etc.). |

## Correspondência com o banco de dados

| Classe | Tabela |
|---|---|
| `Pessoa` + `Funcionario` | `pessoa` + `funcionario` |
| `Cliente` + `ClientePessoaFisica` / `ClientePessoaJuridica` | `pessoa` + `cliente` + `cliente_pf` / `cliente_pj` |
| `Pagamento` + `PagamentoCartao` / `PagamentoPix` | `pagamento` + `pagamento_cartao` / `pagamento_pix` |
| `Loja`, `CategoriaVeiculo`, `Veiculo`, `Reserva`, `Locacao`, `Multa`, `Manutencao` | `loja`, `categoria_veiculo`, `veiculo`, `reserva`, `locacao`, `multa`, `manutencao` |

Os métodos abstratos (`getPerfil`, `getDocumento`, `getCondutor`, `getForma`, `descrever`) são implementados nas
subclasses indicadas no diagrama. As classes de acesso a dados (DAO) e de serviço aparecem no diagrama de sequência.
